package org.example.sem4backend.controller;

import lombok.Data;
import org.example.sem4backend.entity.AttendanceAppeal;
import org.example.sem4backend.service.AttendanceAppealService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance-appeals")
public class AttendanceAppealController {

    private final AttendanceAppealService appealService;

    public AttendanceAppealController(AttendanceAppealService appealService) {
        this.appealService = appealService;
    }

    // 1. POST /attendance-appeals
    @PreAuthorize("hasAnyRole('Admin', 'Hr', 'User')")
    @PostMapping
    public ResponseEntity<AttendanceAppeal> createAppeal(@RequestBody CreateAppealRequest request) {
        AttendanceAppeal appeal = appealService.createAppeal(
                request.getEmployeeId(),
                request.getAttendanceId(),
                request.getReason(),
                request.getEvidence()
        );
        return ResponseEntity.ok(appeal);
    }

    // 2. GET /attendance-appeals?employeeId=...
    @PreAuthorize("hasAnyRole('Admin', 'Hr', 'User')")
    @GetMapping
    public ResponseEntity<List<AttendanceAppeal>> getAppealsByEmployee(@RequestParam String employeeId) {
        return ResponseEntity.ok(appealService.getAppealsByEmployee(employeeId));
    }

    // 3. GET /attendance-appeals/{id}
    @PreAuthorize("hasAnyRole('Admin', 'Hr', 'User')")
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceAppeal> getAppealById(@PathVariable String id) {
        return ResponseEntity.ok(appealService.getAppealById(id));
    }

    // 4. PUT /attendance-appeals/{id}/status
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AttendanceAppeal> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        AttendanceAppeal updated = appealService.updateStatus(id, request.getStatus(), request.getReviewedBy(), request.getNote());
        return ResponseEntity.ok(updated);
    }
    @PreAuthorize("hasAnyRole('Admin', 'Hr', 'User')")
    @GetMapping("/by-employee")
    public ResponseEntity<List<AttendanceAppeal>> getAppealsByEmployeeAndOptionalStatus(
            @RequestParam String employeeId,
            @RequestParam(required = false) AttendanceAppeal.Status status
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    appealService.getAppealsByEmployeeAndStatus(employeeId, status)
            );
        } else {
            return ResponseEntity.ok(
                    appealService.getAppealsByEmployee(employeeId)
            );
        }
    }


    // === DTOs ===
    @Data
    public static class CreateAppealRequest {
        private String employeeId;
        private String attendanceId;
        private String reason;
        private String evidence;
    }

    @Data
    public static class UpdateStatusRequest {
        private AttendanceAppeal.Status status;
        private String reviewedBy;
        private String note;
    }
}