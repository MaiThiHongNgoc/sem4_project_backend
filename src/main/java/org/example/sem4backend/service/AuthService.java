package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LoginRequest;
import org.example.sem4backend.dto.response.LoginResponse;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.exception.AppException;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.UserRepository;
import org.example.sem4backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt - Username: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            logger.warn("Failed login attempt for username: {}", request.getUsername());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", request.getUsername());
                    return new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại");
                });

        logger.info("Fetched user from DB - ID: {}, Username: {}, Email: {}, Role ID: {}",
                user.getUserId(), user.getUsername(), user.getEmail(),
                user.getRole() != null ? user.getRole().getRoleId() : "NULL");

        if (user.getStatus() != User.Status.Active) {
            logger.warn("Attempt to login with inactive user: {}, Status: {}",
                    user.getUsername(), user.getStatus());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Tài khoản đã bị vô hiệu hóa.");
        }

        if (user.getRole() == null) {
            logger.error("User.getRole() is null for username: {}. Please check role_id in users table.",
                    user.getUsername());
            throw new AppException(ErrorCode.ROLE_NOT_FOUND,
                    "Người dùng chưa được gán vai trò hợp lệ. Vui lòng kiểm tra role_id trong bảng users.");
        }

        if (user.getRole().getRoleId() == null) {
            logger.error("Role ID is null for role: {}", user.getRole());
            throw new AppException(ErrorCode.ROLE_NOT_FOUND, "Vai trò không hợp lệ: ID vai trò bị thiếu.");
        }

        String roleId = user.getRole().getRoleId().toString();
        String roleName = user.getRole().getRoleName() != null ? user.getRole().getRoleName() : "USER";

        logger.info("User role - ID: {}, Name: {}", roleId, roleName);

        String token = jwtTokenProvider.createToken(
                user.getUserId(), // userId (subject)
                user.getUsername(),          // username (claim)
                roleName                     // role (claim)
        );

        logger.info("Generated JWT token for username: {}, Role: {}", user.getUsername(), roleName);
        logger.info("User login completed - Username: {}", user.getUsername());

        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getRoleId().toString(),
                user.getStatus() != null ? user.getStatus().name() : null,
                token
        );
    }
    // hello
}
