package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Attendance;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.repository.AttendanceRepository;
import org.example.sem4backend.repository.QRAttendanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceCalculationService {

    private final QRAttendanceRepository qrAttendanceRepository;
    private final AttendanceRepository attendanceRepository;

    public List<Attendance> getAttendancesWithEmployee() {
        return attendanceRepository.findActiveWithEmployee(Attendance.ActiveStatus.Active);
    }

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
            boolean onLeave = false;

            for (QRAttendance qr : logs) {
                switch (qr.getStatus()) {
                    case CheckIn -> checkIn = qr;
                    case CheckOut -> checkOut = qr;
                    case OnLeave -> onLeave = true;
                }
            }

            BigDecimal totalHours = BigDecimal.ZERO;
            Attendance.Status finalStatus;

            if (onLeave) {
                finalStatus = Attendance.Status.OnLeave;
            } else if (checkIn != null && checkOut != null && checkOut.getScanTime().after(checkIn.getScanTime())) {
                long millis = checkOut.getScanTime().getTime() - checkIn.getScanTime().getTime();
                long hours = millis / (1000 * 60 * 60);
                totalHours = BigDecimal.valueOf(hours);
                finalStatus = hours >= 8 ? Attendance.Status.Present : Attendance.Status.Late;
            } else if (checkIn != null) {
                finalStatus = Attendance.Status.Late;
            } else {
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

    public List<Attendance> getByEmployeeId(String employeeId) {
        return attendanceRepository.findByEmployeeIdWithEmployee(employeeId);
    }

    public List<Attendance> getByEmployeeAndDateRange(String employeeId, LocalDate fromDate, LocalDate toDate, String status) {
        if (status != null && !status.isEmpty()) {
            return attendanceRepository.findByEmployeeIdAndDateRangeAndStatus(employeeId, fromDate, toDate, status);
        }
        return attendanceRepository.findByEmployeeIdAndDateRange(employeeId, fromDate, toDate);
    }
}
