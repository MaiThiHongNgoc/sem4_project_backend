package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.entity.Location;
import org.example.sem4backend.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> create(@RequestBody Location location) {
        return ResponseEntity.ok(locationService.createLocation(location));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable UUID id, @RequestBody Location location) {
        return ResponseEntity.ok(locationService.updateLocation(id, location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDelete(@PathVariable UUID id) {
        locationService.softDeleteLocation(id);
        return ResponseEntity.ok("Location marked as deleted");
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAll() {
        return ResponseEntity.ok(locationService.getAllActiveLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }
}