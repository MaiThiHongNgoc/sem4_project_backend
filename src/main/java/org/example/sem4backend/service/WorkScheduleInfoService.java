package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.WorkScheduleInfoRequest;
import org.example.sem4backend.dto.response.WorkScheduleInfoResponse;
import org.example.sem4backend.entity.WorkScheduleInfo;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.WorkScheduleInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleInfoService {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleInfoService.class);
    private final WorkScheduleInfoRepository workScheduleInfoRepository;

    public WorkScheduleInfoResponse createWorkScheduleInfo(WorkScheduleInfoRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("Name is null or blank");
            throw new AppException(ErrorCode.INVALID_INPUT, "Name cannot be null or blank");
        }

        if (workScheduleInfoRepository.findByName(request.getName()).isPresent()) {
            log.error("Work schedule info with name '{}' already exists", request.getName());
            throw new AppException(ErrorCode.DUPLICATE_NAME, "Work schedule info name already exists");
        }

        WorkScheduleInfo workScheduleInfo = new WorkScheduleInfo();
        workScheduleInfo.setName(request.getName());
        workScheduleInfo.setDescription(request.getDescription());
        workScheduleInfo.setDefaultStartTime(convertLocalTimeToDate(request.getDefaultStartTime()));
        workScheduleInfo.setDefaultEndTime(convertLocalTimeToDate(request.getDefaultEndTime()));

        try {
            workScheduleInfo.setStatus(WorkScheduleInfo.Status.valueOf(request.getStatus()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", request.getStatus());
            throw new AppException(ErrorCode.INVALID_INPUT, "Invalid status: " + request.getStatus() + ". Valid values: Active, Inactive");
        }

        try {
            WorkScheduleInfo savedWorkScheduleInfo = workScheduleInfoRepository.save(workScheduleInfo);
            log.info("Created work schedule info with ID: {}", savedWorkScheduleInfo.getScheduleInfoId());
            return mapToResponse(savedWorkScheduleInfo);
        } catch (Exception e) {
            log.error("Failed to create work schedule info for name: {}", request.getName(), e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "Failed to create work schedule info: " + e.getMessage());
        }
    }

    public WorkScheduleInfoResponse getWorkScheduleInfoById(String scheduleInfoId) {
        WorkScheduleInfo workScheduleInfo = workScheduleInfoRepository.findById(scheduleInfoId)
                .orElseThrow(() -> {
                    log.warn("Work schedule info not found for ID: {}", scheduleInfoId);
                    return new AppException(ErrorCode.NOT_FOUND, "Work schedule info not found");
                });
        return mapToResponse(workScheduleInfo);
    }

    public List<WorkScheduleInfoResponse> getAllWorkScheduleInfos() {
        return workScheduleInfoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public WorkScheduleInfoResponse updateWorkScheduleInfo(String scheduleInfoId, WorkScheduleInfoRequest request) {
        WorkScheduleInfo workScheduleInfo = workScheduleInfoRepository.findById(scheduleInfoId)
                .orElseThrow(() -> {
                    log.warn("Work schedule info not found for ID: {}", scheduleInfoId);
                    return new AppException(ErrorCode.NOT_FOUND, "Work schedule info not found");
                });

        workScheduleInfoRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getScheduleInfoId().equals(scheduleInfoId)) {
                        log.error("Work schedule info with name '{}' already exists", request.getName());
                        throw new AppException(ErrorCode.DUPLICATE_NAME, "Work schedule info name already exists");
                    }
                });

        workScheduleInfo.setName(request.getName());
        workScheduleInfo.setDescription(request.getDescription());
        workScheduleInfo.setDefaultStartTime(convertLocalTimeToDate(request.getDefaultStartTime()));
        workScheduleInfo.setDefaultEndTime(convertLocalTimeToDate(request.getDefaultEndTime()));

        try {
            workScheduleInfo.setStatus(WorkScheduleInfo.Status.valueOf(request.getStatus()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", request.getStatus());
            throw new AppException(ErrorCode.INVALID_INPUT, "Invalid status: " + request.getStatus() + ". Valid values: Active, Inactive");
        }

        try {
            WorkScheduleInfo updatedWorkScheduleInfo = workScheduleInfoRepository.save(workScheduleInfo);
            log.info("Updated work schedule info with ID: {}", updatedWorkScheduleInfo.getScheduleInfoId());
            return mapToResponse(updatedWorkScheduleInfo);
        } catch (Exception e) {
            log.error("Failed to update work schedule info with ID: {}", scheduleInfoId, e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "Failed to update work schedule info: " + e.getMessage());
        }
    }

    public void deleteWorkScheduleInfo(String scheduleInfoId) {
        WorkScheduleInfo workScheduleInfo = workScheduleInfoRepository.findById(scheduleInfoId)
                .orElseThrow(() -> {
                    log.warn("Work schedule info not found for ID: {}", scheduleInfoId);
                    return new AppException(ErrorCode.NOT_FOUND, "Work schedule info not found");
                });
        workScheduleInfoRepository.delete(workScheduleInfo);
        log.info("Deleted work schedule info with ID: {}", scheduleInfoId);
    }

    private WorkScheduleInfoResponse mapToResponse(WorkScheduleInfo workScheduleInfo) {
        WorkScheduleInfoResponse response = new WorkScheduleInfoResponse();
        response.setScheduleInfoId(workScheduleInfo.getScheduleInfoId());
        response.setName(workScheduleInfo.getName());
        response.setDescription(workScheduleInfo.getDescription());
        response.setDefaultStartTime(convertDateToLocalTime(workScheduleInfo.getDefaultStartTime()));
        response.setDefaultEndTime(convertDateToLocalTime(workScheduleInfo.getDefaultEndTime()));
        response.setStatus(workScheduleInfo.getStatus().name());
        return response;
    }

    private Date convertLocalTimeToDate(LocalTime localTime) {
        if (localTime == null) return null;
        return Date.from(localTime.atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    private LocalTime convertDateToLocalTime(Date date) {
        if (date == null) return null;

        if (date instanceof java.sql.Time time) {
            return time.toLocalTime();
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
