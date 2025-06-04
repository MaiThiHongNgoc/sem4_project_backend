package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.service.QRAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/qrattendance")
public class QRAttendanceController {

    @Autowired
    private QRAttendanceService qrAttendanceService;

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
    @PutMapping("/{qrId}")
    public ResponseEntity<QRAttendance> update(@PathVariable String qrId,
                                               @RequestBody QRAttendance updateData) {
        QRAttendance updated = qrAttendanceService.update(qrId, updateData);
        return ResponseEntity.ok(updated);
    }

    // Soft delete
    @DeleteMapping("/{qrId}")
    public ResponseEntity<Void> softDelete(@PathVariable String qrId) {
        qrAttendanceService.softDelete(qrId);
        return ResponseEntity.noContent().build();
    }
}
