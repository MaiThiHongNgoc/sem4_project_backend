package org.example.sem4backend.service;

import lombok.RequiredArgsConstructor;
import org.example.sem4backend.dto.request.DepartmentRequest;
import org.example.sem4backend.dto.response.DepartmentResponse;
import org.example.sem4backend.entity.Department;
import org.example.sem4backend.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DepartmentRepository departmentRepository;

    // ✅ 1. Lấy tất cả phòng ban bằng native query (qua repository)
    public List<DepartmentResponse> getAllDepartmentsNative() {
        logger.info("Fetching all departments using native query...");
        List<Department> departments = departmentRepository.findAllDepartmentNative();

        List<DepartmentResponse> responses = departments.stream().map(department -> DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .status(String.valueOf(department.getStatus()))
                .build()).toList();

        logger.info("Total departments fetched: {}", responses.size());
        return responses;
    }

    // ✅ 2. Lấy phòng ban theo status (Active / Inactive) bằng JdbcTemplate
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

    // ✅ 3. Thêm phòng ban bằng stored procedure
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

    // ✅ 4. Cập nhật phòng ban
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

    // ✅ 5. Xóa phòng ban
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

    // ✅ 6. Tìm theo tên phòng ban (dùng trong add)
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

    // ✅ 7. Tìm theo ID
    public DepartmentResponse getDepartmentById(UUID departmentId) {
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
        return DepartmentResponse.builder()
                .departmentId(UUID.fromString(rs.getString("department_id")))
                .departmentName(rs.getString("department_name"))
                .status(rs.getString("status"))
                .build();
    }
}
