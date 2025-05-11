package org.example.sem4backend.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Random;
import lombok.Generated;
import org.example.sem4backend.dto.request.ForgotPasswordRequest;
import org.example.sem4backend.dto.request.ResetPasswordRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.UserRepository;
import org.example.sem4backend.util.EmailSender;
import org.example.sem4backend.util.RedisUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final EmailSender emailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<Void> sendOtpToEmail(ForgotPasswordRequest request) {
        Optional<User> userOpt = this.userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ApiResponse.error(ErrorCode.USER_NOT_FOUND, "Email không tồn tại trong hệ thống.");
        } else {
            String otpCode = this.generateOtp();
            this.redisUtil.storeOtpInRedis(request.getEmail(), otpCode);
            this.emailSender.sendOtpEmail(request.getEmail(), otpCode);
            return ApiResponse.success(ErrorCode.OTP_SENT_SUCCESSFULLY);
        }
    }

    public ApiResponse<Void> verifyOtp(String email, String otpCode) {
        String storedOtp = this.redisUtil.getOtpFromRedis(email);
        if (storedOtp == null) {
            return ApiResponse.error(ErrorCode.OTP_EXPIRED, "Mã OTP đã hết hạn.");
        } else if (!storedOtp.equals(otpCode)) {
            return ApiResponse.error(ErrorCode.INVALID_OTP, "Mã OTP không đúng.");
        } else {
            this.redisUtil.storeOtpInRedis(email + "_verified", "true");
            return ApiResponse.success(ErrorCode.OTP_VERIFIED_SUCCESSFULLY);
        }
    }

    @Transactional
    public ApiResponse<Void> resetPassword(String email, ResetPasswordRequest resetRequest) {
        String isVerified = this.redisUtil.getOtpFromRedis(email + "_verified");
        if (!"true".equals(isVerified)) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED, "Bạn chưa xác thực OTP hoặc OTP đã hết hạn.");
        } else if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            return ApiResponse.error(ErrorCode.INVALID_INPUT, "Mật khẩu xác nhận không khớp.");
        } else {
            String encodedPassword = this.passwordEncoder.encode(resetRequest.getNewPassword());
            int updatedRows = this.userRepository.updatePassword(email, encodedPassword);
            if (updatedRows == 0) {return ApiResponse.error(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với email này.");
            } else {
                this.redisUtil.deleteOtpFromRedis(email);
                this.redisUtil.deleteOtpFromRedis(email + "_verified");
                return ApiResponse.success(ErrorCode.PASSWORD_UPDATED_SUCCESSFULLY);
            }
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Generated
    public PasswordResetService(final UserRepository userRepository, final RedisUtil redisUtil, final EmailSender emailSender, final BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
    }
}