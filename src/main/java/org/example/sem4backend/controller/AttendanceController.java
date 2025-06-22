package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Attendance;
import org.example.sem4backend.repository.AttendanceRepository;
import org.example.sem4backend.service.AttendanceCalculationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCalculationService calculationService;

    @GetMapping
    public List<Attendance> getAllActiveAttendances() {
        return attendanceRepository.findByActiveStatus(Attendance.ActiveStatus.Active);
    }

    @PostMapping
    public Attendance create(@RequestBody Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @PutMapping("/{id}")
    public Attendance update(@PathVariable String id, @RequestBody Attendance updated) {
        return attendanceRepository.findById(id).map(att -> {
            att.setAttendanceDate(updated.getAttendanceDate());
            att.setTotalHours(updated.getTotalHours());
            att.setStatus(updated.getStatus());
            return attendanceRepository.save(att);
        }).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable String id) {
        attendanceRepository.findById(id).ifPresent(att -> {
            att.setActiveStatus(Attendance.ActiveStatus.Inactive);
            attendanceRepository.save(att);
        });
    }

    @PostMapping("/summary")
    public String summarize(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        calculationService.generateDailyAttendanceSummary(date);
        return "Attendance summary generated for " + date;
    }
}
