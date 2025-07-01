package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.EmployeeHistoryRequest;
import org.example.sem4backend.dto.response.EmployeeHistoryResponse;
import org.example.sem4backend.entity.*;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeHistoryService {
    private final EmployeeHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    public EmployeeHistoryResponse create(EmployeeHistoryRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        Department department = null;
        Position position = null;

        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        if (request.getPositionId() != null) {
            position = positionRepository.findById(String.valueOf(request.getPositionId()))
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        }

        EmployeeHistory history = EmployeeHistory.builder()
                .historyId(UUID.randomUUID().toString())
                .employee(employee)
                .department(department)
                .position(position)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(request.getStatus())
                .build();

        historyRepository.save(history);

        return toResponse(history);
    }

    public List<EmployeeHistoryResponse> getAll() {
        return historyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeHistoryResponse getById(String id) {
        EmployeeHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HISTORY_NOT_FOUND));
        return toResponse(history);
    }

    public EmployeeHistoryResponse update(String id, EmployeeHistoryRequest request) {
        EmployeeHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HISTORY_NOT_FOUND));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        Position position = null;
        if (request.getPositionId() != null) {
            position = positionRepository.findById(String.valueOf(request.getPositionId()))
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        }

        history.setEmployee(employee);
        history.setDepartment(department);
        history.setPosition(position);
        history.setStartDate(request.getStartDate());
        history.setEndDate(request.getEndDate());
        history.setReason(request.getReason());
        history.setStatus(request.getStatus());

        return toResponse(historyRepository.save(history));
    }

    public void delete(String id) {
        EmployeeHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HISTORY_NOT_FOUND));
        historyRepository.delete(history);
    }

    public List<EmployeeHistoryResponse> getByEmployeeId(String employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(UUID.fromString(employeeId)))
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        String userRole = user.getRole().getRoleName();

        if (userRole.equals("USER") && !user.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return historyRepository.findByEmployee(employee).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EmployeeHistoryResponse toResponse(EmployeeHistory history) {
        return EmployeeHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .employeeId(history.getEmployee().getEmployeeId())
                .employeeName(history.getEmployee().getFullName())
                .departmentId(
                        history.getDepartment() != null
                                ? history.getDepartment().getDepartmentId()
                                : null
                )
                .departmentName(
                        history.getDepartment() != null
                                ? history.getDepartment().getDepartmentName()
                                : null
                )
                .positionId(
                        history.getPosition() != null
                                ? history.getPosition().getPositionId()
                                : null
                )
                .positionName(
                        history.getPosition() != null
                                ? history.getPosition().getPositionName()
                                : null
                )
                .startDate(history.getStartDate())
                .endDate(history.getEndDate())
                .reason(history.getReason())
                .status(history.getStatus())
                .build();
    }


}
