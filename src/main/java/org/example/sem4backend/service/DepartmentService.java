package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.DepartmentRequest;
import org.example.sem4backend.dto.response.DepartmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DepartmentResponse> getDepartments(String status) {
        logger.info("Fetching departments with status={}", status);

        String baseQuery = "SELECT department_id, department_name, status FROM departments";
        List<Object> params = new ArrayList<>();

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            baseQuery += " WHERE status = ?";
            params.add(status);
        }

        List<DepartmentResponse> departments = jdbcTemplate.query(
                baseQuery,
                params.toArray(),
                (rs, rowNum) -> mapToDepartmentResponse(rs)
        );

        logger.info("Total departments fetched: {}", departments.size());
        return departments;
    }

    public DepartmentResponse addDepartment(DepartmentRequest request) {
        try {
            jdbcTemplate.update("CALL sp_add_department(?)", request.getDepartmentName());
            DepartmentResponse response = getDepartmentByName(request.getDepartmentName());
            if (response == null) {
                throw new IllegalStateException("Failed to retrieve newly added department");
            }
            return response;
        } catch (Exception e) {
            logger.error("Error adding department: ", e);
            throw new IllegalStateException("Lỗi thêm department: " + e.getMessage());
        }
    }

    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        try {
            jdbcTemplate.update("CALL sp_update_department(?, ?)", id.toString(), request.getDepartmentName());
            DepartmentResponse response = getDepartmentById(id);
            if (response == null) {
                throw new IllegalStateException("Failed to retrieve updated department");
            }
            return response;
        } catch (Exception e) {
            logger.error("Error updating department: ", e);
            throw new IllegalStateException("Lỗi cập nhật department: " + e.getMessage());
        }
    }

    public void deleteDepartment(UUID id) {
        try {
            int rowsAffected = jdbcTemplate.update("CALL sp_delete_department(?)", id.toString());
            if (rowsAffected == 0) {
                throw new IllegalStateException("Department not found or already deleted");
            }
        } catch (Exception e) {
            logger.error("Error deleting department: ", e);
            throw new IllegalStateException("Lỗi xóa department: " + e.getMessage());
        }
    }

    private DepartmentResponse getDepartmentByName(String departmentName) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT department_id, department_name, status FROM departments WHERE department_name = ?",
                    (rs, rowNum) -> mapToDepartmentResponse(rs),
                    departmentName
            );
        } catch (Exception e) {
            logger.warn("Department not found for name: {}", departmentName);
            return null;
        }
    }

    private DepartmentResponse getDepartmentById(UUID departmentId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT department_id, department_name, status FROM departments WHERE department_id = ?",
                    (rs, rowNum) -> mapToDepartmentResponse(rs),
                    departmentId.toString()
            );
        } catch (Exception e) {
            logger.warn("Department not found for id: {}", departmentId);
            return null;
        }
    }

    private DepartmentResponse mapToDepartmentResponse(ResultSet rs) throws SQLException {
        DepartmentResponse response = new DepartmentResponse();
        response.setDepartmentId(UUID.fromString(rs.getString("department_id")));
        response.setDepartmentName(rs.getString("department_name"));
        response.setStatus(rs.getString("status"));
        return response;
    }
}