package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.WorkScheduleInfoRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.WorkScheduleInfoResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.WorkScheduleInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-schedule-infos")
@RequiredArgsConstructor
public class WorkScheduleInfoController {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleInfoController.class);
    private final WorkScheduleInfoService workScheduleInfoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Hr', 'Admin')")
    public ApiResponse<WorkScheduleInfoResponse> createWorkScheduleInfo(@Valid @RequestBody WorkScheduleInfoRequest request) {
        log.info("Received request to create work schedule info: {}", request.getName());
        WorkScheduleInfoResponse response = workScheduleInfoService.createWorkScheduleInfo(request);
        return ApiResponse.success(ErrorCode.SUCCESS, response);
    }

    @GetMapping("/{scheduleInfoId}")
    @PreAuthorize("hasAnyRole('User', 'Hr', 'Admin')")
    public ApiResponse<WorkScheduleInfoResponse> getWorkScheduleInfo(@PathVariable String scheduleInfoId) {
        log.info("Received request to get work schedule info with ID: {}", scheduleInfoId);
        WorkScheduleInfoResponse response = workScheduleInfoService.getWorkScheduleInfoById(scheduleInfoId);
        return ApiResponse.success(ErrorCode.SUCCESS, response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('User', 'Hr', 'Admin')")
    public ApiResponse<List<WorkScheduleInfoResponse>> getAllWorkScheduleInfos() {
        log.info("Received request to get all work schedule infos");
        List<WorkScheduleInfoResponse> responses = workScheduleInfoService.getAllWorkScheduleInfos();
        return ApiResponse.success(ErrorCode.SUCCESS, responses);
    }

    @PutMapping("/{scheduleInfoId}")
    @PreAuthorize("hasAnyRole('Hr', 'Admin')")
    public ApiResponse<WorkScheduleInfoResponse> updateWorkScheduleInfo(
            @PathVariable String scheduleInfoId,
            @Valid @RequestBody WorkScheduleInfoRequest request) {
        log.info("Received request to update work schedule info with ID: {}", scheduleInfoId);
        WorkScheduleInfoResponse response = workScheduleInfoService.updateWorkScheduleInfo(scheduleInfoId, request);
        return ApiResponse.success(ErrorCode.SUCCESS, response);
    }

    @DeleteMapping("/{scheduleInfoId}")
    @PreAuthorize("hasAnyRole('Hr', 'Admin')")
    public ApiResponse<Void> deleteWorkScheduleInfo(@PathVariable String scheduleInfoId) {
        log.info("Received request to delete work schedule info with ID: {}", scheduleInfoId);
        workScheduleInfoService.deleteWorkScheduleInfo(scheduleInfoId);
        return ApiResponse.success(ErrorCode.SUCCESS);
    }
}