package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LeaveRequest;
import org.example.sem4backend.dto.response.LeaveResponse;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.Leave;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.LeaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveService.class);
    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveResponse createLeave(LeaveRequest request) {
        // Validate input
        if (request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            log.error("Employee ID is null or empty");
            throw new AppException(ErrorCode.INVALID_EMPLOYEE_ID, "Employee ID cannot be null or empty");
        }

        // Validate and convert employeeId to UUID
        UUID employeeId;
        try {
            employeeId = UUID.fromString(request.getEmployeeId().trim());
            log.info("Processing leave request for employeeId: {}", employeeId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for employeeId: {}", request.getEmployeeId());
            throw new AppException(ErrorCode.INVALID_EMPLOYEE_ID, "Employee ID must be a valid UUID: " + request.getEmployeeId());
        }

        // Fetch employee
        Employee employee = employeeRepository.findById(employeeId.toString())
                .orElseThrow(() -> {
                    log.warn("Employee not found for ID: {}", employeeId);
                    return new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
                });

        Leave leave = new Leave();
        leave.setEmployee(employee);

        // Set dates directly
        if (request.getLeaveStartDate() != null) {
            leave.setLeaveStartDate(request.getLeaveStartDate());
        } else {
            throw new AppException(ErrorCode.INVALID_LEAVE_DATE, "Leave start date cannot be null");
        }
        if (request.getLeaveEndDate() != null) {
            leave.setLeaveEndDate(request.getLeaveEndDate());
        } else {
            throw new AppException(ErrorCode.INVALID_LEAVE_DATE, "Leave end date cannot be null");
        }

        // Validate leaveType
        try {
            leave.setLeaveType(Leave.LeaveType.valueOf(request.getLeaveType()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid leave type: {}", request.getLeaveType());
            throw new AppException(ErrorCode.INVALID_LEAVE_TYPE, "Invalid leave type: " + request.getLeaveType() + ". Valid types: SickLeave, AnnualLeave, UnpaidLeave, Other");
        }

        leave.setStatus(Leave.LeaveStatus.Pending);
        leave.setActiveStatus(Leave.ActiveStatus.Active);

        try {
            Leave savedLeave = leaveRepository.save(leave);
            return mapToResponse(savedLeave);
        } catch (Exception e) {
            log.error("Failed to save leave for employeeId: {}", employeeId, e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "Failed to create leave request: " + e.getMessage());
        }
    }

    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LeaveResponse getLeaveById(String leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));
        return mapToResponse(leave);
    }

    public void deleteLeave(String leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));
        leaveRepository.delete(leave);
    }

    private LeaveResponse mapToResponse(Leave leave) {
        LeaveResponse response = new LeaveResponse();
        response.setLeaveId(leave.getLeaveId());
        response.setEmployeeId(UUID.fromString(leave.getEmployee().getEmployeeId()));
        response.setEmployeeName(leave.getEmployee().getFullName());
        response.setLeaveStartDate(leave.getLeaveStartDate());
        response.setLeaveEndDate(leave.getLeaveEndDate());
        response.setLeaveType(leave.getLeaveType().name());
        response.setStatus(leave.getStatus().name());
        response.setActiveStatus(leave.getActiveStatus().name());
        return response;
    }
}