package org.example.sem4backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LeaveRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.LeaveResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.LeaveService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PreAuthorize("hasAnyRole('User', 'Hr', 'Admin')")
    @PostMapping
    public ApiResponse<LeaveResponse> createLeave(@RequestBody LeaveRequest request) {
        LeaveResponse response = leaveService.createLeave(request);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, response);
    }

    @PreAuthorize("hasAnyRole('Hr', 'Admin')")
    @GetMapping
    public ApiResponse<List<LeaveResponse>> getAllLeaves() {
        List<LeaveResponse> list = leaveService.getAllLeaves();
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, list);
    }

    @PreAuthorize("hasAnyRole('Hr', 'Admin')")
    @GetMapping("/{id}")
    public ApiResponse<LeaveResponse> getLeaveById(@PathVariable String id) {
        LeaveResponse response = leaveService.getLeaveById(id);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, response);
    }

    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLeave(@PathVariable String id) {
        leaveService.deleteLeave(id);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL);
    }
}
