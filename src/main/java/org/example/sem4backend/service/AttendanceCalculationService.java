package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Attendance;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.repository.AttendanceRepository;
import org.example.sem4backend.repository.QRAttendanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceCalculationService {

    private final QRAttendanceRepository qrAttendanceRepository;
    private final AttendanceRepository attendanceRepository;

    public void generateDailyAttendanceSummary(Date date) {
        List<QRAttendance> qrList = qrAttendanceRepository.findAllByDateRange(date, date);

        Map<String, List<QRAttendance>> groupedByEmployee = new HashMap<>();
        for (QRAttendance qr : qrList) {
            String empId = qr.getEmployee().getEmployeeId();
            groupedByEmployee.computeIfAbsent(empId, k -> new ArrayList<>()).add(qr);
        }

        for (Map.Entry<String, List<QRAttendance>> entry : groupedByEmployee.entrySet()) {
            String empId = entry.getKey();
            List<QRAttendance> logs = entry.getValue();

            Date attendanceDate = logs.get(0).getAttendanceDate();
            QRAttendance checkIn = null;
            QRAttendance checkOut = null;
            Attendance.Status finalStatus = Attendance.Status.Absent;
            BigDecimal totalHours = BigDecimal.ZERO;

            for (QRAttendance qr : logs) {
                switch (qr.getStatus()) {
                    case CheckIn -> checkIn = qr;
                    case CheckOut -> checkOut = qr;
                    case OnLeave -> finalStatus = Attendance.Status.OnLeave;
                }
            }

            if (checkIn != null && checkOut != null) {
                long millis = checkOut.getScanTime().getTime() - checkIn.getScanTime().getTime();
                long hours = millis / (1000 * 60 * 60);
                totalHours = BigDecimal.valueOf(hours);
                finalStatus = hours >= 8 ? Attendance.Status.Present : Attendance.Status.Late;

            } else if (checkIn != null) {
                finalStatus = Attendance.Status.Late;

            } else if (finalStatus != Attendance.Status.OnLeave) {
                finalStatus = Attendance.Status.Absent;
            }

            Attendance attendance = new Attendance();
            attendance.setAttendanceId(UUID.randomUUID().toString());
            attendance.setEmployee(logs.get(0).getEmployee());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setTotalHours(totalHours);
            attendance.setStatus(finalStatus);
            attendance.setActiveStatus(Attendance.ActiveStatus.Active);

            attendanceRepository.save(attendance);
        }
    }
}
