package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LoginRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.LoginResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.service.AuthService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage);
        }

        try {
            LoginResponse loginResponse = authService.login(request);
            return ApiResponse.success(ErrorCode.SUCCESS, loginResponse);
        } catch (RuntimeException e) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED, e.getMessage());
        }
    }

}