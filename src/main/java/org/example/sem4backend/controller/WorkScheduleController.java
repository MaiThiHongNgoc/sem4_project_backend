package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.WorkScheduleRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.WorkScheduleResponse;
import org.example.sem4backend.service.WorkScheduleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {
    private final WorkScheduleService service;

    @PreAuthorize("hasAnyRole('Admin','Hr','User')")
    @PostMapping
    public ApiResponse<WorkScheduleResponse> create(@RequestBody WorkScheduleRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasAnyRole('Admin','Hr','User')")
    @PostMapping("/bulk")
    public ApiResponse<List<WorkScheduleResponse>> createBulk(@RequestBody List<WorkScheduleRequest> requests) {
        return service.createBulk(requests);
    }


    @PreAuthorize("hasAnyRole('Admin','Hr','User')")
    @GetMapping
    public ApiResponse<List<WorkScheduleResponse>> getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('Admin','Hr','User')")
    @GetMapping("/{id}")
    public ApiResponse<WorkScheduleResponse> getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PreAuthorize("hasAnyRole('Admin','Hr')")
    @PutMapping("/{id}")
    public ApiResponse<WorkScheduleResponse> update(@PathVariable String id, @RequestBody WorkScheduleRequest request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }
}
