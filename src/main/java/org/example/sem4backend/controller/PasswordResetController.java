package org.example.sem4backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.EnterOtpRequest;
import org.example.sem4backend.dto.request.ForgotPasswordRequest;
import org.example.sem4backend.dto.request.ResetPasswordRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.service.PasswordResetService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // 1. Gửi mã OTP tới email
    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtpToEmail(@RequestBody @Valid ForgotPasswordRequest request) {
        return passwordResetService.sendOtpToEmail(request);
    }

    // 2. Xác nhận mã OTP
    @PostMapping("/verify-otp/{email}")
    public ApiResponse<Void> verifyOtp(
            @PathVariable String email,
            @RequestBody @Valid EnterOtpRequest otpRequest) {
        return passwordResetService.verifyOtp(email, otpRequest.getOtpCode());
    }

    // 3. Đặt lại mật khẩu
    @PostMapping("/reset-password/{email}")
    public ApiResponse<Void> resetPassword(
            @PathVariable String email,
            @RequestBody @Valid ResetPasswordRequest resetRequest) {
        return passwordResetService.resetPassword(email, resetRequest);
    }
}