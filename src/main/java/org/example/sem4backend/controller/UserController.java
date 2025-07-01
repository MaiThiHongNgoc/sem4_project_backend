package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.UserRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.UserResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.example.sem4backend.dto.request.ChangePasswordRequest;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
            @RequestParam(required = false) String status
    ) {
        List<UserResponse> users = userService.getUsers(status);
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, users), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestBody @Valid UserRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage));
        }

        ApiResponse<UserResponse> response = userService.register(request);

        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UserRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return new ResponseEntity<>(ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage), HttpStatus.BAD_REQUEST);
        }
        ApiResponse<UserResponse> response = userService.updateUser(userId, request, bindingResult);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        ApiResponse<Void> response = userService.deleteUser(userId);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    @GetMapping("/native")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersNative() {
        List<UserResponse> users = userService.getAllUsersNative();
        return new ResponseEntity<>(ApiResponse.success(ErrorCode.SUCCESS, users), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Admin', 'Hr','User')")
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        ApiResponse<Void> response = userService.changePassword(userId, request);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

}