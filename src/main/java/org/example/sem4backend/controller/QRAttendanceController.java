package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.QRAttendanceRequest;
import org.example.sem4backend.entity.QRAttendance;
import org.example.sem4backend.service.QRAttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class QRAttendanceController {

    private final QRAttendanceService qrAttendanceService;

    @PostMapping("/mark")
    public ResponseEntity<QRAttendance> markAttendance(@RequestBody QRAttendanceRequest request) {
        return ResponseEntity.ok(qrAttendanceService.markAttendance(request));
    }

    @GetMapping
    public ResponseEntity<List<QRAttendance>> getAll() {
        return ResponseEntity.ok(qrAttendanceService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<QRAttendance>> getActiveOnly() {
        return ResponseEntity.ok(qrAttendanceService.getActiveOnly());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRAttendance> getById(@PathVariable String id) {
        return ResponseEntity.ok(qrAttendanceService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QRAttendance> update(@PathVariable String id, @RequestBody QRAttendanceRequest request) {
        return ResponseEntity.ok(qrAttendanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        qrAttendanceService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

