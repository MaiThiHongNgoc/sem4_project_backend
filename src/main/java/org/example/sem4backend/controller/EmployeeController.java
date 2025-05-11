package org.example.sem4backend.controller;


import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.EmployeeRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.EmployeeResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String status
    ) {
        logger.info("Received getEmployees request: status={}", status);
        List<EmployeeResponse> employees = employeeService.getEmployees(status);
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, employees), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(
            @RequestBody @Valid EmployeeRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received addEmployee request for email={}", request.getEmail());
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        try {
            EmployeeResponse response = employeeService.addEmployee(request);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, response), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.warn("Add employee failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @RequestBody @Valid EmployeeRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received updateEmployee request for id={}", id);
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        try {
            EmployeeResponse response = employeeService.updateEmployee(id, request);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, response), HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.warn("Update employee failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        logger.info("Received deleteEmployee request for id={}", id);
        try {
            employeeService.deleteEmployee(id);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS), HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.warn("Delete employee failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}