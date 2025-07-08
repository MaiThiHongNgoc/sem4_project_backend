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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    public ApiResponse<List<WorkScheduleResponse>> createBulk(List<WorkScheduleRequest> requests) {
        List<WorkScheduleResponse> responses = requests.stream().map(request -> {
            Employee employee = employeeRepo.findById(request.getEmployeeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

            WorkScheduleInfo info = null;
            if (request.getScheduleInfoId() != null) {
                info = scheduleInfoRepo.findById(request.getScheduleInfoId())
                        .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_INFO_NOT_FOUND));
            }

            // Kiểm tra xem ca này đã đăng ký chưa (tránh đăng lại)
            Optional<WorkSchedule> existing = repository.findDuplicateSchedule(
                    employee.getEmployeeId(),
                    info != null ? info.getScheduleInfoId() : null,
                    request.getWorkDay()
            );
            if (existing.isPresent()) {
                // Có thể throw lỗi nếu muốn chặn toàn bộ
                return null; // bỏ qua ca này
            }

            WorkSchedule schedule = WorkSchedule.builder()
                    .employee(employee)
                    .scheduleInfo(info)
                    .workDay(request.getWorkDay())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .status(WorkSchedule.Status.valueOf(request.getStatus()))
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


    public ApiResponse<List<WorkScheduleFullResponse>> getAllWithDetails() {
        List<WorkSchedule> schedules = repository.findAll();

        List<WorkScheduleFullResponse> responses = schedules.stream().map(ws -> {
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
        }).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    public ApiResponse<List<WorkScheduleFullResponse>> getFilteredSchedules(String empId, LocalDate workDay) {
        System.out.println("🧪 Lọc lịch làm với employeeId = " + empId + ", workDay = " + workDay);
        List<WorkSchedule> schedules = repository.findByEmployeeIdAndWorkDay(empId, workDay);

        System.out.println("📊 Số lịch tìm được: " + schedules.size());

        List<WorkScheduleFullResponse> responses = schedules.stream().map(ws -> WorkScheduleFullResponse.builder()
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
                .defaultStartTime(ws.getScheduleInfo().getDefaultStartTime()) // ✅
                .defaultEndTime(ws.getScheduleInfo().getDefaultEndTime())
                .status(ws.getStatus().name())
                .build()
        ).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }



    public ApiResponse<List<WorkScheduleFullResponse>> getSchedulesByEmployeeAndDateRange(
            String employeeId,LocalDate fromDate, LocalDate toDate
    ) {
        List<WorkSchedule> schedules = repository.findByEmployeeAndDateRange(employeeId, fromDate, toDate);

        List<WorkScheduleFullResponse> responses = schedules.stream().map(ws -> WorkScheduleFullResponse.builder()
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
                .defaultStartTime(ws.getScheduleInfo().getDefaultStartTime()) // ✅
                .defaultEndTime(ws.getScheduleInfo().getDefaultEndTime())
                .status(ws.getStatus().name())
                .build()
        ).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }




}