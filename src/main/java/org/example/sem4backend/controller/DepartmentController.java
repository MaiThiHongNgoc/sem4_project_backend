package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.DepartmentRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.DepartmentResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.DepartmentService;
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
@RequestMapping("/api/departments")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments(
            @RequestParam(required = false) String status
    ) {
        logger.info("Received getDepartments request: status={}", status);
        List<DepartmentResponse> departments = departmentService.getDepartments(status);
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, departments), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> addDepartment(
            @RequestBody @Valid DepartmentRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received addDepartment request: departmentName={}", request.getDepartmentName());
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        try {
            DepartmentResponse response = departmentService.addDepartment(request);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, response), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.warn("Add department failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable UUID id,
            @RequestBody @Valid DepartmentRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received updateDepartment request for id={}", id);
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        try {
            DepartmentResponse response = departmentService.updateDepartment(id, request);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, response), HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.warn("Update department failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        logger.info("Received deleteDepartment request for id={}", id);
        try {
            departmentService.deleteDepartment(id);
            return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS), HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.warn("Delete department failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.OPERATION_FAILED, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}