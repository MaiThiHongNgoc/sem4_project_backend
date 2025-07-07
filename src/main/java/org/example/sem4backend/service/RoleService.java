package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.RoleRequest;
import org.example.sem4backend.dto.response.ApiResponse;
import org.example.sem4backend.dto.response.RoleResponse;
import org.example.sem4backend.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<RoleResponse> getRoles(String status) {
        logger.info("Fetching roles with status={}", status);

        List<RoleResponse> allRoles = jdbcTemplate.query(
                "CALL sp_get_all_roles()",
                (rs, rowNum) -> new RoleResponse(
                        rs.getString("role_id"),
                        rs.getString("role_name"),
                        rs.getString("description"),
                        rs.getString("status")
                )
        );

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            allRoles = allRoles.stream()
                    .filter(r -> r.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        logger.info("Total roles fetched: {}", allRoles.size());
        return allRoles;
    }

    public ApiResponse<RoleResponse> addRole(RoleRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_add_role(?, ?)",
                    request.getRoleName(),
                    request.getDescription()
            );
            RoleResponse response = new RoleResponse(UUID.randomUUID().toString(), request.getRoleName(), request.getDescription(), "Active");
            return ApiResponse.success(ErrorCode.SUCCESS, response);
        } catch (Exception e) {
            logger.error("Error adding role: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi thêm role: " + e.getMessage());
        }
    }

    public ApiResponse<RoleResponse> updateRole(UUID roleId, RoleRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_update_role(?, ?, ?)",
                    roleId.toString(),
                    request.getRoleName(),
                    request.getDescription()
            );
            RoleResponse response = new RoleResponse(roleId.toString(), request.getRoleName(), request.getDescription(), "Active");
            return ApiResponse.success(ErrorCode.SUCCESS, response);
        } catch (Exception e) {
            logger.error("Error updating role: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi cập nhật role: " + e.getMessage());
        }
    }

    public ApiResponse<Void> deleteRole(UUID roleId) {
        try {
            jdbcTemplate.update(
                    "CALL sp_delete_role(?)",
                    roleId.toString()
            );
            return ApiResponse.success(ErrorCode.SUCCESS);
        } catch (Exception e) {
            logger.error("Error deleting role: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi xóa role: " + e.getMessage());
        }
    }
    public ApiResponse<RoleResponse> getRoleById(UUID roleId) {
        try {
            List<RoleResponse> roles = jdbcTemplate.query(
                    "SELECT role_id, role_name, description, status FROM roles WHERE role_id = ?",
                    new Object[]{roleId.toString()},
                    (rs, rowNum) -> new RoleResponse(
                            rs.getString("role_id"),
                            rs.getString("role_name"),
                            rs.getString("description"),
                            rs.getString("status")
                    )
            );

            if (roles.isEmpty()) {
                return ApiResponse.error(ErrorCode.NOT_FOUND, "Không tìm thấy vai trò với ID đã cho");
            }

            return ApiResponse.success(ErrorCode.SUCCESS, roles.get(0));
        } catch (Exception e) {
            logger.error("Lỗi khi lấy role theo ID: ", e);
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "Lỗi lấy role theo ID: " + e.getMessage());
        }
    }



}