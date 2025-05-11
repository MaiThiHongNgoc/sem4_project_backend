package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.RoleRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.RoleResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.RoleService;
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
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(
            @RequestParam(required = false) String status
    ) {
        logger.info("Received getRoles request: status={}", status);
        List<RoleResponse> roles = roleService.getRoles(status);
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, roles), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> addRole(
            @RequestBody @Valid RoleRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received addRole request: roleName={}", request.getRoleName());
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        ApiResponse<RoleResponse> response = roleService.addRole(request);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @RequestBody @Valid RoleRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Received updateRole request for roleId={}", roleId);
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        ApiResponse<RoleResponse> response = roleService.updateRole(roleId, request);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID roleId) {
        logger.info("Received deleteRole request for roleId={}", roleId);
        ApiResponse<Void> response = roleService.deleteRole(roleId);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}