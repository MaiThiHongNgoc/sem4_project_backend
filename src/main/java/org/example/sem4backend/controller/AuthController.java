package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LoginRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.LoginResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        // Kiểm tra nếu có lỗi validation
        if (bindingResult.hasErrors()) {
            // Tạo thông báo lỗi từ các lỗi validation
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ApiResponse.<LoginResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessage)
                    .result(null)
                    .build();
        }

        logger.debug("Received login request for username: {}", request.getUsername());
        try {
            LoginResponse loginResponse = authService.login(request);
            logger.debug("Login successful for username: {}", request.getUsername());
            return ApiResponse.<LoginResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message(ErrorCode.SUCCESS.getMessage())
                    .result(loginResponse)
                    .build();
        } catch (RuntimeException e) {
            logger.error("Login failed for username: {}. Error: {}", request.getUsername(), e.getMessage());
            return ApiResponse.<LoginResponse>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }
}