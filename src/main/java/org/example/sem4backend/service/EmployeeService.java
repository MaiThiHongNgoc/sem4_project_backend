package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.EmployeeRequest;
import org.example.sem4backend.dto.response.EmployeeResponse;
import org.example.sem4backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRepository userRepository;

    public String getEmployeeIdByUserId(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + userId));
    }


    public List<EmployeeResponse> getEmployees(String status) {
        logger.info("Fetching employees with status={}", status);

        String baseQuery = "SELECT * FROM employees";
        List<Object> params = new ArrayList<>();

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            baseQuery += " WHERE status = ?";
            params.add(status);
        }

        List<EmployeeResponse> employees = jdbcTemplate.query(
                baseQuery,
                params.toArray(),
                this::mapToEmployeeResponse
        );

        logger.info("Total employees fetched: {}", employees.size());
        return employees;
    }

    public EmployeeResponse addEmployee(EmployeeRequest request) {
        try {
            UUID employeeId = UUID.randomUUID();

            // Convert LocalDate to java.sql.Date
            Date dob = request.getDateOfBirth() != null ? Date.valueOf(request.getDateOfBirth()) : null;
            Date hireDate = request.getHireDate() != null ? Date.valueOf(request.getHireDate()) : null;

            jdbcTemplate.update(
                    "CALL sp_add_employee(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    employeeId.toString(),
                    request.getFullName(),
                    request.getGender(),
                    dob,
                    request.getPhone(),
                    request.getAddress(),
                    request.getImg(),
                    request.getDepartmentId() != null ? request.getDepartmentId().toString() : null,
                    request.getPositionId() != null ? request.getPositionId().toString() : null,
                    hireDate
            );

            EmployeeResponse response = getEmployeeById(employeeId);
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
            Date dob = request.getDateOfBirth() != null ? Date.valueOf(request.getDateOfBirth()) : null;
            Date hireDate = request.getHireDate() != null ? Date.valueOf(request.getHireDate()) : null;

            jdbcTemplate.update(
                    "CALL sp_update_employee(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    employeeId.toString(),
                    request.getFullName(),
                    request.getGender(),
                    dob,
                    request.getPhone(),
                    request.getAddress(),
                    request.getImg(),
                    request.getDepartmentId() != null ? request.getDepartmentId().toString() : null,
                    request.getPositionId() != null ? request.getPositionId().toString() : null,
                    hireDate
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

    private EmployeeResponse getEmployeeById(UUID employeeId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM employees WHERE employee_id = ?",
                    this::mapToEmployeeResponse,
                    employeeId.toString()
            );
        } catch (Exception e) {
            logger.warn("Employee not found for id: {}", employeeId);
            return null;
        }
    }

    private EmployeeResponse mapToEmployeeResponse(ResultSet rs, int rowNum) throws SQLException {
        EmployeeResponse response = new EmployeeResponse();
        response.setEmployeeId(String.valueOf(UUID.fromString(rs.getString("employee_id"))));
        response.setFullName(rs.getString("full_name"));
        response.setGender(rs.getString("gender"));

        Date dob = rs.getDate("date_of_birth");
        response.setDateOfBirth(dob != null ? dob.toLocalDate() : null);

        response.setPhone(rs.getString("phone"));
        response.setAddress(rs.getString("address"));
        response.setImg(rs.getString("img"));

        String departmentId = rs.getString("department_id");
        response.setDepartmentId(departmentId != null ? UUID.fromString(departmentId) : null);

        String positionId = rs.getString("position_id");
        response.setPositionId(positionId != null ? UUID.fromString(positionId) : null);

        Date hireDate = rs.getDate("hire_date");
        response.setHireDate(hireDate != null ? hireDate.toLocalDate() : null);

        response.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        response.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        response.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

        return response;
    }
}