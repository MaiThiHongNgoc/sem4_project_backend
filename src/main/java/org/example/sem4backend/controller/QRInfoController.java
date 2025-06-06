package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.QRInfo;
import org.example.sem4backend.service.QRInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QRInfoController {

    private final QRInfoService qrInfoService;

    @GetMapping
    public List<QRInfo> getAll() {
        return qrInfoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRInfo> getById(@PathVariable String id) {
        QRInfo qr = qrInfoService.findById(id);
        return qr != null ? ResponseEntity.ok(qr) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<QRInfo> update(@PathVariable String id, @RequestBody QRInfo updatedQR) {
        QRInfo qr = qrInfoService.update(id, updatedQR);
        return qr != null ? ResponseEntity.ok(qr) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = qrInfoService.softDelete(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public List<QRInfo> search(@RequestParam("q") String keyword) {
        return qrInfoService.search(keyword);
    }
}
