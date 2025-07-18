package org.example.sem4backend.service;

import org.example.sem4backend.entity.Attendance;
import org.example.sem4backend.entity.AttendanceAppeal;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.repository.AttendanceAppealRepository;
import org.example.sem4backend.repository.AttendanceRepository;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AttendanceAppealService {

    private final AttendanceAppealRepository appealRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public AttendanceAppealService(AttendanceAppealRepository appealRepository,
                                   AttendanceRepository attendanceRepository,
                                   EmployeeRepository employeeRepository,
                                   UserRepository userRepository) {
        this.appealRepository = appealRepository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    public AttendanceAppeal createAppeal(String employeeId, String attendanceId, Date targetDate, String reason, String evidence) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        AttendanceAppeal appeal = new AttendanceAppeal();
        appeal.setEmployee(employee);
        appeal.setTargetDate(targetDate);
        appeal.setReason(reason);
        appeal.setEvidence(evidence);
        appeal.setStatus(AttendanceAppeal.Status.Pending);

        if (attendanceId != null && !attendanceId.isEmpty()) {
            Attendance attendance = attendanceRepository.findById(attendanceId)
                    .orElseThrow(() -> new RuntimeException("Attendance not found"));
            appeal.setAttendance(attendance);
        } else {
            appeal.setAttendance(null); // rõ ràng gán null để an toàn
        }

        return appealRepository.save(appeal);
    }


    public List<AttendanceAppeal> getAppealsByEmployee(String employeeId) {
        return appealRepository.findByEmployee_EmployeeId(employeeId);
    }

    public AttendanceAppeal getAppealById(String id) {
        return appealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));
    }

    public AttendanceAppeal updateStatus(String id, AttendanceAppeal.Status status, String reviewedByUserId, String note) {
        AttendanceAppeal appeal = appealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));

        User reviewer = userRepository.findById(reviewedByUserId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        appeal.setStatus(status);
        appeal.setReviewedBy(reviewer);
        appeal.setReviewedAt(new Date());
        appeal.setNote(note);

        if (status == AttendanceAppeal.Status.Approved) {
            java.sql.Date targetSqlDate = new java.sql.Date(appeal.getTargetDate().getTime());

            boolean exists = attendanceRepository
                    .findByEmployeeAndAttendanceDate(appeal.getEmployee(), targetSqlDate)
                    .isPresent();

            if (!exists) {
                Attendance newAttendance = new Attendance();
                newAttendance.setAttendanceId(UUID.randomUUID().toString());
                newAttendance.setEmployee(appeal.getEmployee());
                newAttendance.setAttendanceDate(targetSqlDate);
                newAttendance.setTotalHours(BigDecimal.valueOf(8)); // Hoặc để null nếu không xác định rõ
                newAttendance.setStatus(Attendance.Status.Present);
                newAttendance.setActiveStatus(Attendance.ActiveStatus.Active);

                attendanceRepository.save(newAttendance);
            }
        }

        return appealRepository.save(appeal);
    }


    public List<AttendanceAppeal> getAppealsByEmployeeAndStatus(String employeeId, AttendanceAppeal.Status status) {
        return appealRepository.findByEmployee_EmployeeIdAndStatus(employeeId, status);
    }

    public List<AttendanceAppeal> getAllAppeals() {
        return appealRepository.findAll();
    }


}
