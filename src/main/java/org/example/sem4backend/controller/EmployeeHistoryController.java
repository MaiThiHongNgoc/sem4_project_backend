package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.EmployeeHistoryRequest;
import org.example.sem4backend.dto.response.EmployeeHistoryResponse;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.service.EmployeeHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-histories")
@RequiredArgsConstructor
public class EmployeeHistoryController {

    private final EmployeeHistoryService employeeHistoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<ApiResponse<EmployeeHistoryResponse>> create(@RequestBody EmployeeHistoryRequest request) {
        EmployeeHistoryResponse response = employeeHistoryService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Hr','User')")
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getAll() {
        List<EmployeeHistoryResponse> list = employeeHistoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Hr','User')")
    public ResponseEntity<ApiResponse<EmployeeHistoryResponse>> getById(@PathVariable String id) {
        EmployeeHistoryResponse response = employeeHistoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<ApiResponse<EmployeeHistoryResponse>> update(
            @PathVariable String id,
            @RequestBody EmployeeHistoryRequest request) {
        EmployeeHistoryResponse response = employeeHistoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        employeeHistoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('Admin', 'Hr', 'User')")
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getByEmployee(@PathVariable String employeeId) {
        List<EmployeeHistoryResponse> list = employeeHistoryService.getByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
