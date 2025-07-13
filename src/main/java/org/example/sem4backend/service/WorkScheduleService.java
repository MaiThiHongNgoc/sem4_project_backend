package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.WorkScheduleRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.WorkScheduleFullResponse;
import org.example.sem4backend.dto.response.WorkScheduleResponse;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.WorkSchedule;
import org.example.sem4backend.entity.WorkScheduleInfo;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.WorkScheduleInfoRepository;
import org.example.sem4backend.repository.WorkScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
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
                .shiftType(WorkSchedule.ShiftType.valueOf(request.getShiftType())) // bạn cần thêm shiftType từ request
                .build();
        repository.save(schedule);

        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, mapToResponse(schedule));
    }

    public ApiResponse<List<WorkScheduleResponse>> createBulk(List<WorkScheduleRequest> requests) {
        List<WorkScheduleResponse> responses = requests.stream().map(request -> {
            Employee employee = employeeRepo.findById(request.getEmployeeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

            WorkScheduleInfo info = null;
            if (request.getScheduleInfoId() != null) {
                info = scheduleInfoRepo.findById(request.getScheduleInfoId())
                        .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_INFO_NOT_FOUND));
            }

            Optional<WorkSchedule> existing = repository.findDuplicateSchedule(
                    employee.getEmployeeId(),
                    info != null ? info.getScheduleInfoId() : null,
                    request.getWorkDay()
            );
            if (existing.isPresent()) {
                return null;
            }

            WorkSchedule schedule = WorkSchedule.builder()
                    .employee(employee)
                    .scheduleInfo(info)
                    .workDay(request.getWorkDay())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .shiftType(WorkSchedule.ShiftType.valueOf(request.getShiftType())) // bạn cần thêm shiftType từ request
                    .build();

            return mapToResponse(repository.save(schedule));
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL, responses);
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

    public ApiResponse<Void> softDelete(String id) {
        WorkSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));
        schedule.setStatus(WorkSchedule.Status.Inactive);
        repository.save(schedule);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL);
    }

    private WorkScheduleResponse mapToResponse(WorkSchedule schedule) {
        return WorkScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .employeeId(schedule.getEmployee().getEmployeeId())
                .employeeName(schedule.getEmployee().getFullName())
                .scheduleInfoId(schedule.getScheduleInfo() != null ? schedule.getScheduleInfo().getScheduleInfoId() : null)
                .scheduleInfoName(schedule.getScheduleInfo() != null ? schedule.getScheduleInfo().getName() : null)
                .workDay(schedule.getWorkDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus().name())
                .build();
    }

    public ApiResponse<List<WorkScheduleFullResponse>> getSchedulesByEmployeeAndDateRange(String employeeId, LocalDate fromDate, LocalDate toDate) {
        List<WorkSchedule> schedules = repository.findByEmployeeAndDateRange(employeeId, fromDate, toDate);
        List<WorkScheduleFullResponse> responses = schedules.stream().map(this::mapToFullResponse).collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    public ApiResponse<List<WorkScheduleFullResponse>> getEditableSchedules(String employeeId, LocalDate fromDate, LocalDate toDate) {
        List<WorkSchedule> schedules = repository.findByEmployeeAndDateRange(employeeId, fromDate, toDate);
        List<WorkScheduleFullResponse> responses = schedules.stream()
                .filter(ws -> ws.getStatus() == WorkSchedule.Status.Active)
                .map(this::mapToFullResponse).collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    public WorkScheduleFullResponse mapToFullResponse(WorkSchedule ws) {
        return WorkScheduleFullResponse.builder()
                .scheduleId(ws.getScheduleId())
                .employeeId(ws.getEmployee().getEmployeeId())
                .employeeName(ws.getEmployee().getFullName())
                .scheduleInfoId(ws.getScheduleInfo() != null ? ws.getScheduleInfo().getScheduleInfoId() : null)
                .scheduleInfoName(ws.getScheduleInfo() != null ? ws.getScheduleInfo().getName() : null)
                .scheduleStartTime(ws.getScheduleInfo() != null ? ws.getScheduleInfo().getDefaultStartTime() : null)
                .scheduleEndTime(ws.getScheduleInfo() != null ? ws.getScheduleInfo().getDefaultEndTime() : null)
                .workDay(ws.getWorkDay())
                .startTime(ws.getStartTime())
                .endTime(ws.getEndTime())
                .status(ws.getStatus().name())
                .build();
    }

    @Scheduled(cron = "0 0 7 * * MON")
    public void autoGenerateDefaultSchedules() {
        Optional<WorkScheduleInfo> defaultShiftOpt = scheduleInfoRepo.findByName("Normal");

        if (defaultShiftOpt.isEmpty()) {
            System.err.println("⚠️ Không tìm thấy ca 'Normal'. Bỏ qua việc sinh lịch tự động.");
            return;
        }

        WorkScheduleInfo defaultShift = defaultShiftOpt.get();

        List<Employee> employees = employeeRepo.findAll();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 5; i++) {
            LocalDate workDate = monday.plusDays(i);

            for (Employee employee : employees) {
                boolean exists = repository
                        .findDuplicateSchedule(employee.getEmployeeId(), defaultShift.getScheduleInfoId(), workDate)
                        .isPresent();

                if (!exists) {
                    WorkSchedule schedule = WorkSchedule.builder()
                            .employee(employee)
                            .scheduleInfo(defaultShift)
                            .workDay(workDate)
                            .startTime(defaultShift.getDefaultStartTime())
                            .endTime(defaultShift.getDefaultEndTime())
                            .status(WorkSchedule.Status.Active)
                            .shiftType(WorkSchedule.ShiftType.Normal)
                            .build();
                    repository.save(schedule);
                }
            }
        }

        System.out.println("✅ Đã sinh lịch làm việc mặc định cho tuần mới (ca 'Normal').");
    }


    public ApiResponse<Void> approveOvertime(String scheduleId) {
        WorkSchedule schedule = repository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));
        schedule.setStatus(WorkSchedule.Status.Active);
        repository.save(schedule);
        return ApiResponse.success(ErrorCode.OPERATION_SUCCESSFUL);
    }

    public ApiResponse<List<WorkScheduleFullResponse>> getOvertimeSchedulesByStatus(String employeeId, WorkSchedule.Status status) {
        List<WorkSchedule> schedules = repository
                .findByEmployee_EmployeeIdAndShiftTypeAndStatus(employeeId, WorkSchedule.ShiftType.OT, status);

        List<WorkScheduleFullResponse> responses = schedules.stream()
                .map(this::mapToFullResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    public ApiResponse<List<WorkScheduleFullResponse>> getOvertimeSchedulesByStatusAndDateRange(
            String employeeId,
            WorkSchedule.Status status,
            LocalDate fromDate,
            LocalDate toDate) {

        List<WorkSchedule> schedules = repository.findOTByEmployeeAndStatusAndDateRange(employeeId, status, fromDate, toDate);

        List<WorkScheduleFullResponse> responses = schedules.stream()
                .map(this::mapToFullResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    public List<WorkSchedule> getOvertimeSchedulesFlexible(String employeeId, String statusStr, LocalDate fromDate, LocalDate toDate) {
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                WorkSchedule.Status status = Arrays.stream(WorkSchedule.Status.values())
                        .filter(s -> s.name().equalsIgnoreCase(statusStr))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không hợp lệ: " + statusStr));

                return repository.findOTByEmployeeAndStatusAndDateRange(employeeId, status, fromDate, toDate);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không hợp lệ: " + statusStr);
            }
        } else {
            return repository.findOTByEmployeeAndDateRange(employeeId, fromDate, toDate);
        }
    }


}
