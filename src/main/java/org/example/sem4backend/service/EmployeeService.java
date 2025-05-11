package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.EmployeeRequest;
import org.example.sem4backend.dto.response.EmployeeResponse;
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
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<EmployeeResponse> getEmployees(String status) {
        logger.info("Fetching employees with status={}", status);

        String baseQuery = "SELECT employee_id, full_name, gender, date_of_birth, phone, email, address, department_id, position_id, hire_date, status, created_at, updated_at FROM employees";
        List<Object> params = new ArrayList<>();

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            baseQuery += " WHERE status = ?";
            params.add(status);
        }

        List<EmployeeResponse> employees = jdbcTemplate.query(
                baseQuery,
                params.toArray(),
                (rs, rowNum) -> mapToEmployeeResponse(rs)
        );

        logger.info("Total employees fetched: {}", employees.size());
        return employees;
    }

    public EmployeeResponse addEmployee(EmployeeRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_add_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    request.getFullName(),
                    request.getGender(),
                    request.getDateOfBirth(),
                    request.getPhone(),
                    request.getEmail(),
                    request.getAddress(),
                    request.getDepartmentId() != null ? request.getDepartmentId().toString() : null,
                    request.getPositionId() != null ? request.getPositionId().toString() : null,
                    request.getHireDate()
            );
            EmployeeResponse response = getEmployeeByEmail(request.getEmail());
            if (response == null) {
                throw new IllegalStateException("Failed to retrieve newly added employee");
            }
            return response;
        } catch (Exception e) {
            logger.error("Error adding employee: ", e);
            throw new IllegalStateException("Lỗi thêm employee: " + e.getMessage());
        }
    }

    public EmployeeResponse updateEmployee(UUID employeeId, EmployeeRequest request) {
        try {
            jdbcTemplate.update(
                    "CALL sp_update_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    employeeId.toString(),
                    request.getFullName(),
                    request.getGender(),
                    request.getDateOfBirth(),
                    request.getPhone(),
                    request.getEmail(),
                    request.getAddress(),
                    request.getDepartmentId() != null ? request.getDepartmentId().toString() : null,
                    request.getPositionId() != null ? request.getPositionId().toString() : null
            );
            EmployeeResponse response = getEmployeeById(employeeId);
            if (response == null) {
                throw new IllegalStateException("Failed to retrieve updated employee");
            }
            return response;
        } catch (Exception e) {
            logger.error("Error updating employee: ", e);
            throw new IllegalStateException("Lỗi cập nhật employee: " + e.getMessage());
        }
    }

    public void deleteEmployee(UUID employeeId) {
        try {
            int rowsAffected = jdbcTemplate.update(
                    "CALL sp_delete_employee(?)",
                    employeeId.toString()
            );
            if (rowsAffected == 0) {
                throw new IllegalStateException("Employee not found or already deleted");
            }
        } catch (Exception e) {
            logger.error("Error deleting employee: ", e);
            throw new IllegalStateException("Lỗi xóa employee: " + e.getMessage());
        }
    }

    private EmployeeResponse getEmployeeByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM employees WHERE email = ?",
                    (rs, rowNum) -> mapToEmployeeResponse(rs),
                    email
            );
        } catch (Exception e) {
            logger.warn("Employee not found for email: {}", email);
            return null;
        }
    }

    private EmployeeResponse getEmployeeById(UUID employeeId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM employees WHERE employee_id = ?",
                    (rs, rowNum) -> mapToEmployeeResponse(rs),
                    employeeId.toString()
            );
        } catch (Exception e) {
            logger.warn("Employee not found for id: {}", employeeId);
            return null;
        }
    }

    private EmployeeResponse mapToEmployeeResponse(ResultSet rs) throws SQLException {
        EmployeeResponse response = new EmployeeResponse();
        response.setEmployeeId(UUID.fromString(rs.getString("employee_id")));
        response.setFullName(rs.getString("full_name"));
        response.setGender(rs.getString("gender"));
        response.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        response.setPhone(rs.getString("phone"));
        response.setEmail(rs.getString("email"));
        response.setAddress(rs.getString("address"));
        String departmentId = rs.getString("department_id");
        response.setDepartmentId(departmentId != null ? UUID.fromString(departmentId) : null);
        String positionId = rs.getString("position_id");
        response.setPositionId(positionId != null ? UUID.fromString(positionId) : null);
        response.setHireDate(rs.getDate("hire_date").toLocalDate());
        response.setStatus(rs.getString("status"));
        response.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        response.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return response;
    }
}
