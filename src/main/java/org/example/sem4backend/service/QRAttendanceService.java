package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.QRAttendanceRequest;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.entity.QRInfo;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.QRAttendanceRepository;
import org.example.sem4backend.repository.QRInfoRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QRAttendanceService {

    private final QRInfoRepository qrInfoRepository;
    private final QRAttendanceRepository qrAttendanceRepository;
    private final EmployeeRepository employeeRepository;

    public QRAttendance markAttendance(QRAttendanceRequest request) {
        QRInfo qrInfo = qrInfoRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new RuntimeException("QR code không hợp lệ."));

        if (!Boolean.TRUE.equals(qrInfo.getActive())) {
            throw new RuntimeException("QR code không hoạt động.");
        }

        if (qrInfo.getExpiredAt() != null && qrInfo.getExpiredAt().before(new Date())) {
            throw new RuntimeException("QR code đã hết hạn.");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại."));

        Date now = new Date();
        QRAttendance.Status checkStatus = QRAttendance.Status.valueOf(request.getStatus());

        // Logic check late
        boolean isLate = false;
        if (checkStatus == QRAttendance.Status.CheckIn) {
            // ví dụ ca làm bắt đầu lúc 08:00
            Date workStart = Date.from(now.toInstant().truncatedTo(java.time.temporal.ChronoUnit.DAYS).plus(java.time.Duration.ofHours(8)));
            isLate = now.after(workStart);
        }

        QRAttendance attendance = new QRAttendance();
        attendance.setEmployee(employee);
        attendance.setQrInfo(qrInfo);
        attendance.setScanTime(now);
        attendance.setAttendanceDate(new java.sql.Date(now.getTime()));
        attendance.setStatus(
                checkStatus == QRAttendance.Status.CheckIn ?
                        (isLate ? QRAttendance.Status.Late : QRAttendance.Status.Present)
                        : QRAttendance.Status.CheckOut
        );
        attendance.setActiveStatus(QRAttendance.ActiveStatus.Active);

        return qrAttendanceRepository.save(attendance);
    }


    public List<QRAttendance> getAll() {
        return qrAttendanceRepository.findAll();
    }

    public List<QRAttendance> getActiveOnly() {
        return qrAttendanceRepository.findByActiveStatus(QRAttendance.ActiveStatus.Active);
    }

    public QRAttendance getById(String id) {
        return qrAttendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi chấm công."));
    }

    public QRAttendance update(String id, QRAttendanceRequest request) {
        QRAttendance attendance = getById(id);
        attendance.setStatus(QRAttendance.Status.valueOf(request.getStatus()));
        attendance.setScanTime(new Date());
        return qrAttendanceRepository.save(attendance);
    }

    public void softDelete(String id) {
        QRAttendance attendance = getById(id);
        attendance.setActiveStatus(QRAttendance.ActiveStatus.Inactive);
        qrAttendanceRepository.save(attendance);
    }
}

