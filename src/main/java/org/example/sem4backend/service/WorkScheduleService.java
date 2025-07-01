package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.WorkScheduleRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.WorkScheduleResponse;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.WorkSchedule;
import org.example.sem4backend.entity.WorkScheduleInfo;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.WorkScheduleInfoRepository;
import org.example.sem4backend.repository.WorkScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {
    private final WorkScheduleRepository repository;
    private final EmployeeRepository employeeRepo;
    private final WorkScheduleInfoRepository scheduleInfoRepo;

    public ApiResponse<WorkScheduleResponse> create(WorkScheduleRequest request) {
        Employee employee = employeeRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        WorkScheduleInfo info = null;
        if (request.getScheduleInfoId() != null) {
            info = scheduleInfoRepo.findById(request.getScheduleInfoId())
                    .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_INFO_NOT_FOUND));
        }

        WorkSchedule schedule = WorkSchedule.builder()
                .employee(employee)
                .scheduleInfo(info)
                .workDay(request.getWorkDay())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(WorkSchedule.Status.valueOf(request.getStatus()))
                .build();
        repository.save(schedule);

        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, mapToResponse(schedule));
    }

    public ApiResponse<List<WorkScheduleResponse>> getAll() {
        return ApiResponse.success(repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    public ApiResponse<WorkScheduleResponse> getById(String id) {
        WorkSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));
        return ApiResponse.success(mapToResponse(schedule));
    }

    public ApiResponse<WorkScheduleResponse> update(String id, WorkScheduleRequest request) {
        WorkSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepo.findById(request.getEmployeeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
            schedule.setEmployee(employee);
        }
        if (request.getScheduleInfoId() != null) {
            WorkScheduleInfo info = scheduleInfoRepo.findById(request.getScheduleInfoId())
                    .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_INFO_NOT_FOUND));
            schedule.setScheduleInfo(info);
        }
        schedule.setWorkDay(request.getWorkDay());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(WorkSchedule.Status.valueOf(request.getStatus()));

        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, mapToResponse(repository.save(schedule)));
    }

    public ApiResponse<Void> delete(String id) {
        if (!repository.existsById(id)) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }
        repository.deleteById(id);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL);
    }

    private WorkScheduleResponse mapToResponse(WorkSchedule schedule) {
        return WorkScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .employeeId(schedule.getEmployee().getEmployeeId())
                .employeeName(schedule.getEmployee().getFullName()) // hoặc .getName() tùy field
                .scheduleInfoId(schedule.getScheduleInfo() != null ? schedule.getScheduleInfo().getScheduleInfoId() : null)
                .scheduleInfoName(schedule.getScheduleInfo() != null ? schedule.getScheduleInfo().getName() : null)
                .workDay(schedule.getWorkDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus().name())
                .build();
    }

}