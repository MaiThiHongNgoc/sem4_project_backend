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
        logger.debug("Attempting login for username: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            logger.warn("Authentication failed for username: {}. Reason: Bad credentials", request.getUsername());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        logger.debug("Authentication successful for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại");
                });

        if (user.getStatus() != User.Status.Active) {
            logger.error("Account disabled for username: {}", request.getUsername());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Tài khoản đã bị vô hiệu hóa.");
        }

        String roleName = user.getRole() != null ? user.getRole().getRole_name() : "USER";
        String token = jwtTokenProvider.createToken(user.getUsername(), roleName);

        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getRole_id().toString() : null,
                user.getStatus() != null ? user.getStatus().name() : null,
                token
        );
    }
}
