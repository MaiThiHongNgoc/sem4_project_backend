package org.example.sem4backend.service;

import jakarta.transaction.Transactional;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.entity.QRInfo;
import org.example.sem4backend.entity.WorkSchedule;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.QRAttendanceRepository;
import org.example.sem4backend.repository.QRInfoRepository;
import org.example.sem4backend.repository.WorkScheduleRepository;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Service
@Transactional
public class QRAttendanceService {

    @Autowired
    private QRAttendanceRepository qrAttendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private QRInfoRepository qrInfoRepository;

    @Autowired
    private AttendanceCalculationService attendanceCalculationService;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;


    // Lấy danh sách QR Attendance đang active
    public List<QRAttendance> getAllActive() {
        return qrAttendanceRepository.findByActiveStatus(QRAttendance.ActiveStatus.Active);
    }

    // Lấy theo id
    public Optional<QRAttendance> getById(String qrId) {
        return qrAttendanceRepository.findById(qrId);
    }

    // Tạo mới bản ghi QR Attendance
    // service/QRAttendanceService.java (phần create)
    public QRAttendance create(QRAttendance qrAttendance) {
        if (qrAttendance.getEmployee() == null || qrAttendance.getEmployee().getEmployeeId() == null) {
            throw new RuntimeException("Employee or Employee ID is missing in the request");
        }

        String empId = qrAttendance.getEmployee().getEmployeeId();
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        qrAttendance.setEmployee(employee);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        Date scanTime = new Date();
        qrAttendance.setScanTime(scanTime);
        qrAttendance.setAttendanceDate(java.sql.Date.valueOf(today));

        // 🔍 1. Tìm ca làm của nhân viên trong hôm nay
        WorkSchedule schedule = workScheduleRepository.findByEmployeeAndWorkDay(empId, today)
                .orElseThrow(() -> new RuntimeException("No work schedule found for today"));

        // 🔄 2. Lấy chấm công hôm nay
        List<QRAttendance> todayRecords = qrAttendanceRepository.findByEmployeeAndAttendanceDate(
                employee, java.sql.Date.valueOf(today)
        );

        boolean hasCheckIn = todayRecords.stream().anyMatch(r -> r.getStatus() == QRAttendance.Status.CheckIn || r.getStatus() == QRAttendance.Status.Late);
        boolean hasCheckOut = todayRecords.stream().anyMatch(r -> r.getStatus() == QRAttendance.Status.CheckOut);

        // ⏱ 3. Kiểm tra giờ chấm công

        Date startDate = schedule.getStartTime(); // java.util.Date nhưng thực tế là java.sql.Time
        Date endDate = schedule.getEndTime();

        LocalTime startTime = ((Time) startDate).toLocalTime(); // ⚠ cast sang java.sql.Time
        LocalTime endTime = ((Time) endDate).toLocalTime();

        now = LocalTime.now(); // lấy giờ hiện tại theo hệ thống



        if (!hasCheckIn) {
            LocalTime lateThreshold = startTime.plusMinutes(15);
            if (now.isBefore(lateThreshold)) {
                qrAttendance.setStatus(QRAttendance.Status.CheckIn);
            } else {
                qrAttendance.setStatus(QRAttendance.Status.Late);
            }
        } else if (!hasCheckOut) {
            qrAttendance.setStatus(QRAttendance.Status.CheckOut);
        } else {
            throw new RuntimeException("Already checked in and out for today.");
        }


        // 📍 4. Kiểm tra phương thức chấm công
        boolean hasQR = qrAttendance.getQrInfo() != null;
        boolean hasFace = qrAttendance.getFaceRecognitionImage() != null && !qrAttendance.getFaceRecognitionImage().isEmpty();
        boolean hasGPS = qrAttendance.getLatitude() != null && qrAttendance.getLongitude() != null;

        if (hasQR) {
            qrAttendance.setAttendanceMethod(QRAttendance.AttendanceMethod.QR);
        } else if (hasFace && hasGPS) {
            qrAttendance.setAttendanceMethod(QRAttendance.AttendanceMethod.FaceGPS);
        } else {
            throw new RuntimeException("Invalid attendance method.");
        }

        qrAttendance.setActiveStatus(QRAttendance.ActiveStatus.Active);

        QRAttendance saved = qrAttendanceRepository.save(qrAttendance);

        // 📊 5. Gọi tổng hợp chấm công
        attendanceCalculationService.generateDailyAttendanceSummary(saved.getAttendanceDate());

        return saved;
    }

    // Cập nhật bản ghi QR Attendance theo qrId
    public QRAttendance update(String qrId, QRAttendance updateData) {
        QRAttendance entity = qrAttendanceRepository.findById(qrId)
                .orElseThrow(() -> new RuntimeException("QRAttendance not found"));

        if (updateData.getEmployee() != null && updateData.getEmployee().getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(String.valueOf(updateData.getEmployee().getEmployeeId()))
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            entity.setEmployee(employee);
        }

        if (updateData.getQrInfo() != null && updateData.getQrInfo().getQrInfoId() != null) {
            QRInfo qrInfo = qrInfoRepository.findById(updateData.getQrInfo().getQrInfoId())
                    .orElse(null);
            entity.setQrInfo(qrInfo);
        } else {
            entity.setQrInfo(null);
        }

        if (updateData.getScanTime() != null) {
            entity.setScanTime(updateData.getScanTime());
        }

        if (updateData.getStatus() != null) {
            entity.setStatus(updateData.getStatus());
        }

        if (updateData.getAttendanceDate() != null) {
            entity.setAttendanceDate(updateData.getAttendanceDate());
        }

        if (updateData.getFaceRecognitionImage() != null) {
            entity.setFaceRecognitionImage(updateData.getFaceRecognitionImage());
        }

        if (updateData.getLatitude() != null) {
            entity.setLatitude(updateData.getLatitude());
        }

        if (updateData.getLongitude() != null) {
            entity.setLongitude(updateData.getLongitude());
        }

        // Cập nhật lại attendanceMethod dựa trên các trường đã cập nhật
        if (entity.getQrInfo() != null) {
            entity.setAttendanceMethod(QRAttendance.AttendanceMethod.QR);
        } else if (entity.getFaceRecognitionImage() != null
                || (entity.getLatitude() != null && entity.getLongitude() != null)) {
            entity.setAttendanceMethod(QRAttendance.AttendanceMethod.FaceGPS);
        } else {
            entity.setAttendanceMethod(QRAttendance.AttendanceMethod.Unknown);
        }

        return qrAttendanceRepository.save(entity);
    }

    // Soft delete: đặt activeStatus thành Inactive
    public void softDelete(String qrId) {
        QRAttendance entity = qrAttendanceRepository.findById(qrId)
                .orElseThrow(() -> new RuntimeException("QRAttendance not found"));
        entity.setActiveStatus(QRAttendance.ActiveStatus.Inactive);
        qrAttendanceRepository.save(entity);
    }

    public List<QRAttendance> getAllWithEmployees() {
        return qrAttendanceRepository.findAllWithEmployee();
    }
    public List<QRAttendance> getAttendanceByEmployeeId(String employeeId) {
        return qrAttendanceRepository.findByEmployeeIdWithEmployee(employeeId);
    }



}