package org.example.sem4backend.service;


import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.LoginRequest;
import org.example.sem4backend.dto.response.LoginResponse;
import org.example.sem4backend.entity.User;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.UserRepository;
import org.example.sem4backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
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

        // Xác thực tài khoản và mật khẩu
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        logger.debug("Authentication successful for username: {}", request.getUsername());

        // Lấy thông tin người dùng từ database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
                });

        // Kiểm tra trạng thái tài khoản
        if (user.getStatus() != User.Status.Active) {
            logger.error("Account disabled for username: {}", request.getUsername());
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa.");
        }

        // Lấy vai trò của người dùng
        String roleName = user.getRole() != null ? user.getRole().getRole_name() : "USER";
        logger.debug("Role for username {}: {}", request.getUsername(), roleName);

        // Tạo token JWT
        String token = jwtTokenProvider.createToken(user.getUsername(), roleName);
        logger.debug("Generated JWT token for username: {}", request.getUsername());

        // Trả về thông tin người dùng và token
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
