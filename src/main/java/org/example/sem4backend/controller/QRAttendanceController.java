
package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.FaceAttendanceRequest;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.service.QRAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/qrattendance")
public class QRAttendanceController {

    @Autowired
    private QRAttendanceService qrAttendanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/face")
    public ResponseEntity<?> faceAttendance(@RequestBody FaceAttendanceRequest request) {
        if (request.getEmployeeId() == null || request.getImageBase64() == null ||
                request.getLatitude() == null || request.getLongitude() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Tạo đối tượng QRAttendance
        QRAttendance attendance = new QRAttendance();
        attendance.setEmployee(employee);
        attendance.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        attendance.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        attendance.setFaceRecognitionImage(request.getImageBase64());
        attendance.setAttendanceDate(Date.from(Instant.now()));
        attendance.setAttendanceMethod(QRAttendance.AttendanceMethod.FaceGPS);
        attendance.setActiveStatus(QRAttendance.ActiveStatus.Active);

        QRAttendance saved = qrAttendanceService.create(attendance);
        return ResponseEntity.ok(saved);
    }


    // Lấy tất cả các bản ghi active
    @GetMapping("/active")
    public ResponseEntity<List<QRAttendance>> getAllActive() {
        List<QRAttendance> list = qrAttendanceService.getAllActive();
        return ResponseEntity.ok(list);
    }

    // Lấy bản ghi theo qrId
    @GetMapping("/{qrId}")
    public ResponseEntity<QRAttendance> getById(@PathVariable String qrId) {
        Optional<QRAttendance> optional = qrAttendanceService.getById(qrId);
        return optional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo mới
    @PostMapping
    public ResponseEntity<QRAttendance> create(@RequestBody QRAttendance qrAttendance) {
        QRAttendance created = qrAttendanceService.create(qrAttendance);
        return ResponseEntity.ok(created);
    }


    // Cập nhật
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PutMapping("/{qrId}")
    public ResponseEntity<QRAttendance> update(@PathVariable String qrId,
                                               @RequestBody QRAttendance updateData) {
        QRAttendance updated = qrAttendanceService.update(qrId, updateData);
        return ResponseEntity.ok(updated);
    }

    // Soft delete
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @DeleteMapping("/{qrId}")
    public ResponseEntity<Void> softDelete(@PathVariable String qrId) {
        qrAttendanceService.softDelete(qrId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-employees/{employeeId}")
    public ResponseEntity<List<QRAttendance>> getAllWithEmployees() {
        List<QRAttendance> list = qrAttendanceService.getAllWithEmployees();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-employee")
    public ResponseEntity<List<QRAttendance>> getAttendancesByEmployeeId(@RequestParam String employeeId) {
        List<QRAttendance> attendances = qrAttendanceService.getAttendanceByEmployeeId(employeeId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<QRAttendance>> filterAttendances(
            @RequestParam(required = false) QRAttendance.ActiveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate
    ) {
        List<QRAttendance> results = qrAttendanceService.filterByStatusAndDate(status, startDate, endDate);
        return ResponseEntity.ok(results);
    }


}