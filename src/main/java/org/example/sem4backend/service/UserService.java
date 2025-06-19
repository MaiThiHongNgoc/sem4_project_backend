package org.example.sem4backend.service;

import jakarta.validation.Valid;
import org.example.sem4backend.dto.request.UserRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.UserResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.example.sem4backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ Lấy toàn bộ users từ stored procedure (có lọc theo status)
    public List<UserResponse> getUsers(String status) {
        logger.info("Fetching users with status={}", status);

        String query = "CALL sp_get_all_users()";

        List<UserResponse> allUsers = jdbcTemplate.query(query, (rs, rowNum) -> {
            UserResponse user = new UserResponse();
            user.setUserId(UUID.fromString(rs.getString("user_id")));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getString("role_id"));
            user.setStatus(rs.getString("status"));
            return user;
        });

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        logger.info("Total users fetched: {}", allUsers.size());
        return allUsers;
    }

    public List<UserResponse> getAllUsersNative() {
        logger.info("Fetching all users using native query");

        return userRepository.findAllUserNative().stream().map(user -> {
            UserResponse response = new UserResponse();
            response.setUserId(UUID.fromString(user.getUserId()));
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());

            if (user.getRole() != null) {
                response.setRole(user.getRole().getRoleName());
            } else {
                logger.warn("⚠️ User {} has no role assigned!", user.getUsername());
                response.setRole(null);
            }

            response.setStatus(String.valueOf(user.getStatus()));
            return response;
        }).collect(Collectors.toList());
    }

    // ✅ Đăng ký user mới
    public ApiResponse<UserResponse> register(UserRequest request) {
        String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer usernameCount = jdbcTemplate.queryForObject(checkUsernameQuery, Integer.class, request.getUsername());
        if (usernameCount != null && usernameCount > 0) {
            return ApiResponse.error(ErrorCode.USERNAME_ALREADY_EXISTS, "Tên đăng nhập đã tồn tại");
        }

        String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer emailCount = jdbcTemplate.queryForObject(checkEmailQuery, Integer.class, request.getEmail());
        if (emailCount != null && emailCount > 0) {
            return ApiResponse.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        try {
            jdbcTemplate.update(
                    "CALL sp_add_user(?, ?, ?, ?, ?)",
                    request.getEmployeeId(),
                    request.getUsername(),
                    encodedPassword,
                    request.getEmail(),
                    request.getRoleId()
            );
        } catch (Exception e) {
            logger.error("Lỗi thêm user: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi thêm user: " + e.getMessage());
        }

        UserResponse user = getUserByUsername(request.getUsername());
        return ApiResponse.success(user);
    }

    // ✅ Cập nhật user
    public ApiResponse<UserResponse> updateUser(UUID userId, @Valid UserRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ApiResponse.error(ErrorCode.VALIDATION_FAILED, errorMessage);
        }

        String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";
        Integer usernameCount = jdbcTemplate.queryForObject(checkUsernameQuery, Integer.class, request.getUsername(), userId.toString());
        if (usernameCount != null && usernameCount > 0) {
            return ApiResponse.error(ErrorCode.USERNAME_ALREADY_EXISTS, "Tên đăng nhập đã tồn tại");
        }

        String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        Integer emailCount = jdbcTemplate.queryForObject(checkEmailQuery, Integer.class, request.getEmail(), userId.toString());
        if (emailCount != null && emailCount > 0) {
            return ApiResponse.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        try {
            jdbcTemplate.update(
                    "CALL sp_update_user(?, ?, ?, ?, ?)",
                    userId.toString(),
                    request.getUsername(),
                    encodedPassword,
                    request.getEmail(),
                    request.getRoleId()
            );
        } catch (Exception e) {
            logger.error("Error updating user: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi trong quá trình cập nhật người dùng: " + e.getMessage());
        }

        UserResponse user = getUserByUsername(request.getUsername());
        return ApiResponse.success(ErrorCode.SUCCESS, user);
    }

    public ApiResponse<Void> deleteUser(UUID userId) {
        try {
            jdbcTemplate.update(
                    "CALL sp_delete_user(?)",
                    userId.toString()
            );
            return ApiResponse.success(ErrorCode.SUCCESS);
        } catch (Exception e) {
            logger.error("Error deleting user: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi xóa user: " + e.getMessage());
        }
    }

    // ✅ Lấy thông tin user theo username
    private UserResponse getUserByUsername(String username) {
        String query = "SELECT user_id, username, email, role_id, status FROM users WHERE username = ?";
        try {
            Map<String, Object> map = jdbcTemplate.queryForMap(query, username);
            UserResponse user = new UserResponse();
            user.setUserId(UUID.fromString((String) map.get("user_id")));
            user.setUsername((String) map.get("username"));
            user.setEmail((String) map.get("email"));
            user.setRole((String) map.get("role_id"));
            user.setStatus((String) map.get("status"));
            return user;
        } catch (Exception e) {
            logger.error("Error fetching user by username: {}", username, e);
            throw new IllegalArgumentException("User not found: " + username);
        }
    }
}
