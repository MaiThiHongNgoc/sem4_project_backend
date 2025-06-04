package org.example.sem4backend.service;

import jakarta.transaction.Transactional;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.entity.QRInfo;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.QRAttendanceRepository;
import org.example.sem4backend.repository.QRInfoRepository;
import org.springframework.stereotype.Service;

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

    // Lấy danh sách QR Attendance đang active
    public List<QRAttendance> getAllActive() {
        return qrAttendanceRepository.findByActiveStatus(QRAttendance.ActiveStatus.Active);
    }

    // Lấy theo id
    public Optional<QRAttendance> getById(String qrId) {
        return qrAttendanceRepository.findById(qrId);
    }

    // Tạo mới bản ghi QR Attendance
    public QRAttendance create(QRAttendance qrAttendance) {
        // Kiểm tra employee có tồn tại không
        if (qrAttendance.getEmployee() == null || qrAttendance.getEmployee().getEmployeeId() == null) {
            throw new RuntimeException("Employee or Employee ID is missing in the request");
        }
        String empId = String.valueOf(qrAttendance.getEmployee().getEmployeeId());
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));

        // Nếu qrInfo id khác null thì lấy entity, ngược lại set null
        if (qrAttendance.getQrInfo() != null && qrAttendance.getQrInfo().getQrInfoId() != null) {
            QRInfo qrInfo = qrInfoRepository.findById(qrAttendance.getQrInfo().getQrInfoId())
                    .orElse(null);
            qrAttendance.setQrInfo(qrInfo);
        } else {
            qrAttendance.setQrInfo(null);
        }

        // Các trường scanTime, attendanceDate, attendanceMethod, qrId sẽ được set trong prePersist
        // Active status mặc định
        qrAttendance.setActiveStatus(QRAttendance.ActiveStatus.Active);

        return qrAttendanceRepository.save(qrAttendance);
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
}
