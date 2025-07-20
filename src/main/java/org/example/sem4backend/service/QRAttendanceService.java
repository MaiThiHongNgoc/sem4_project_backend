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
import org.example.sem4backend.util.FacePlusPlusUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

        // 🔍 1. Tìm tất cả ca làm hợp lệ: Normal hoặc OT đã duyệt
        List<WorkSchedule> schedules = workScheduleRepository.findValidSchedulesForAttendance(empId, today);
        if (schedules.isEmpty()) {
            throw new RuntimeException("Không tìm thấy ca làm hợp lệ để chấm công.");
        }

        // ⏰ 2. Tính giờ bắt đầu sớm nhất và giờ kết thúc muộn nhất (ưu tiên default)
        LocalTime earliestStart = schedules.stream()
                .map(s -> {
                    Time defaultStart = s.getScheduleInfo() != null ? (Time) s.getScheduleInfo().getDefaultStartTime() : null;
                    return defaultStart != null ? defaultStart.toLocalTime() : ((Time) s.getStartTime()).toLocalTime();
                })
                .min(LocalTime::compareTo)
                .orElseThrow();

        LocalTime latestEnd = schedules.stream()
                .map(s -> {
                    Time defaultEnd = s.getScheduleInfo() != null ? (Time) s.getScheduleInfo().getDefaultEndTime() : null;
                    return defaultEnd != null ? defaultEnd.toLocalTime() : ((Time) s.getEndTime()).toLocalTime();
                })
                .max(LocalTime::compareTo)
                .orElseThrow();


        // 📋 3. Kiểm tra chấm công hôm nay
        List<QRAttendance> todayRecords = qrAttendanceRepository.findByEmployeeAndAttendanceDate(
                employee, java.sql.Date.valueOf(today)
        );

        boolean hasCheckIn = todayRecords.stream().anyMatch(r ->
                r.getStatus() == QRAttendance.Status.CheckIn || r.getStatus() == QRAttendance.Status.Late);
        boolean hasCheckOut = todayRecords.stream().anyMatch(r ->
                r.getStatus() == QRAttendance.Status.CheckOut);

        // ✅ 4. Xác định trạng thái chấm công
        if (!hasCheckIn) {
            LocalTime lateThreshold = earliestStart.plusMinutes(15);
            qrAttendance.setStatus(now.isBefore(lateThreshold)
                    ? QRAttendance.Status.CheckIn
                    : QRAttendance.Status.Late);
        } else if (!hasCheckOut) {
            qrAttendance.setStatus(QRAttendance.Status.CheckOut);
        } else {
            throw new RuntimeException("Already checked in and out for today.");
        }

        // 🛠️ 5. Xác định phương thức chấm công
        boolean hasQR = qrAttendance.getQrInfo() != null;
        boolean hasFace = qrAttendance.getFaceRecognitionImage() != null && !qrAttendance.getFaceRecognitionImage().isEmpty();
        boolean hasGPS = qrAttendance.getLatitude() != null && qrAttendance.getLongitude() != null;

        if (hasQR) {
            qrAttendance.setAttendanceMethod(QRAttendance.AttendanceMethod.QR);
        } else if (hasFace && hasGPS) {
            // Ảnh base64 từ bảng employee và từ client
            String employeeImage = cleanBase64(employee.getImg());
            String submittedImage = cleanBase64(qrAttendance.getFaceRecognitionImage());

            if (employeeImage == null || submittedImage == null) {
                throw new RuntimeException("Missing employee image or submitted image for face match.");
            }

            try {
                double confidence = FacePlusPlusUtil.getConfidence(employeeImage, submittedImage);
                System.out.println("✅ Độ chính xác nhận diện khuôn mặt (confidence): " + confidence + "%");

                if (confidence < 85.0) {
                    throw new RuntimeException("Nhận diện khuôn mặt thất bại: Ảnh không khớp (độ chính xác: " + confidence + "%)");
                }

                qrAttendance.setAttendanceMethod(QRAttendance.AttendanceMethod.FaceGPS);
            } catch (IOException e) {
                System.err.println("❌ Lỗi khi gọi API Face++: " + e.getMessage());
                throw new RuntimeException("Không thể nhận diện khuôn mặt: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Invalid attendance method.");
        }

        qrAttendance.setActiveStatus(QRAttendance.ActiveStatus.Active);

        QRAttendance saved = qrAttendanceRepository.save(qrAttendance);

        // 📊 Tổng hợp chấm công
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


    private String cleanBase64(String base64) {
        if (base64 == null) return null;
        if (base64.contains(",")) {
            return base64.substring(base64.indexOf(",") + 1).trim();
        }
        return base64.trim();
    }

    public List<QRAttendance> filterByStatusAndDate(QRAttendance.ActiveStatus status, Date startDate, Date endDate) {
        return qrAttendanceRepository.findByFilters(status, startDate, endDate);
    }


}