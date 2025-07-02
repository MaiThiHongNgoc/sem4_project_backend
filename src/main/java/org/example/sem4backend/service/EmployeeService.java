package org.example.sem4backend.service;

import org.example.sem4backend.dto.request.EmployeeHistoryRequest;
import org.example.sem4backend.dto.request.EmployeeRequest;
import org.example.sem4backend.dto.response.EmployeeResponse;
import org.example.sem4backend.entity.Employee;
import org.example.sem4backend.entity.EmployeeHistory;
import org.example.sem4backend.repository.EmployeeRepository;
import org.example.sem4backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeHistoryService employeeHistoryService;


    public String getEmployeeIdByUserId(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + userId));
    }

    public List<EmployeeResponse> getAllNativeEmployees() {
        List<Employee> employees = employeeRepository.findAllEmployeeNative();
        return employees.stream().map(this::mapToEmployeeResponseFromEntity).toList();
    }

    public List<EmployeeResponse> getEmployees(String status) {
        logger.info("Fetching employees with status={}", status);

        String baseQuery = """
            SELECT e.*, d.department_name, p.position_name
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.department_id
            LEFT JOIN positions p ON e.position_id = p.position_id
        """;
        List<Object> params = new ArrayList<>();

        if (status != null && (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive"))) {
            baseQuery += " WHERE e.status = ?";
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

            // Get the employee after insertion
            EmployeeResponse response = getEmployeeById(employeeId);
            if (response == null) {
                throw new IllegalStateException("Failed to retrieve newly added employee");
            }

            // Ghi lịch sử nếu có phòng ban hoặc chức vụ
            if (request.getDepartmentId() != null || request.getPositionId() != null) {
                EmployeeHistoryRequest historyRequest = EmployeeHistoryRequest.builder()
                        .employeeId(employeeId.toString())
                        .departmentId(request.getDepartmentId())
                        .positionId(request.getPositionId())
                        .startDate(String.valueOf(request.getHireDate()))
                        .status(EmployeeHistory.Status.valueOf("Active"))
                        .reason("Hired")
                        .build();
                employeeHistoryService.create(historyRequest);
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

            // Lấy nhân viên hiện tại từ DB
            EmployeeResponse current = getEmployeeById(employeeId);
            if (current == null) {
                throw new IllegalStateException("Employee not found");
            }

            // Kiểm tra có thay đổi phòng ban hoặc chức vụ không
            boolean isDepartmentChanged = request.getDepartmentId() != null
                    && !request.getDepartmentId().equals(current.getDepartmentId());
            boolean isPositionChanged = request.getPositionId() != null
                    && !request.getPositionId().equals(current.getPositionId());

            jdbcTemplate.update(
                    "CALL sp_update_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    employeeId.toString(),
                    request.getFullName(),
                    request.getGender(),
                    dob,
                    request.getPhone(),
                    request.getAddress(),
                    request.getImg(),
                    request.getDepartmentId() != null ? request.getDepartmentId().toString() : null,
                    request.getPositionId() != null ? request.getPositionId().toString() : null
            );

            EmployeeResponse updated = getEmployeeById(employeeId);
            if (updated == null) {
                throw new IllegalStateException("Failed to retrieve updated employee");
            }

            // Nếu có thay đổi, thêm lịch sử
            if (isDepartmentChanged || isPositionChanged) {
                EmployeeHistoryRequest historyRequest = EmployeeHistoryRequest.builder()
                        .employeeId(employeeId.toString())
                        .departmentId(request.getDepartmentId())
                        .positionId(request.getPositionId())
                        .startDate(String.valueOf(updated.getHireDate() != null ? updated.getHireDate() : java.time.LocalDate.now()))
                        .status(EmployeeHistory.Status.valueOf(updated.getStatus()))
                        .reason("Updated department or position")
                        .build();
                employeeHistoryService.create(historyRequest);
            }

            return updated;
        } catch (Exception e) {
            logger.error("Error updating employee: ", e);
            throw new IllegalStateException("Lỗi cập nhật employee: " + e.getMessage());
        }
    }


    public void deleteEmployee(UUID employeeId) {
        try {
            int rowsAffected = jdbcTemplate.update("CALL sp_delete_employee(?)", employeeId.toString());
            if (rowsAffected == 0) {
                throw new IllegalStateException("Employee not found or already deleted");
            }
        } catch (Exception e) {
            logger.error("Error deleting employee: ", e);
            throw new IllegalStateException("Lỗi xóa employee: " + e.getMessage());
        }
    }

    public EmployeeResponse getEmployeeById(UUID employeeId) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT e.*, d.department_name, p.position_name
                    FROM employees e
                    LEFT JOIN departments d ON e.department_id = d.department_id
                    LEFT JOIN positions p ON e.position_id = p.position_id
                    WHERE e.employee_id = ?
                    """,
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
        response.setEmployeeId(rs.getString("employee_id"));
        response.setFullName(rs.getString("full_name"));
        response.setGender(rs.getString("gender"));
        response.setDateOfBirth(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null);
        response.setPhone(rs.getString("phone"));
        response.setAddress(rs.getString("address"));
        response.setImg(rs.getString("img"));

        response.setDepartmentId(rs.getString("department_id"));
        response.setDepartmentName(rs.getString("department_name"));
        response.setPositionId(rs.getString("position_id"));
        response.setPositionName(rs.getString("position_name"));

        response.setHireDate(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toLocalDate() : null);
        response.setStatus(rs.getString("status"));
        response.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        response.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return response;
    }


    private EmployeeResponse mapToEmployeeResponseFromEntity(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setEmployeeId(employee.getEmployeeId());
        response.setFullName(employee.getFullName());
        response.setGender(String.valueOf(employee.getGender()));
        response.setDateOfBirth(employee.getDateOfBirth());
        response.setPhone(employee.getPhone());
        response.setAddress(employee.getAddress());
        response.setImg(employee.getImg());

        // Không dùng UUID.fromString vì ID là String
        response.setDepartmentId(employee.getDepartment() != null
                ? employee.getDepartment().getDepartmentId()
                : null);
        response.setDepartmentName(employee.getDepartment() != null
                ? employee.getDepartment().getDepartmentName()
                : null);
        response.setPositionId(employee.getPosition() != null
                ? employee.getPosition().getPositionId()
                : null);
        response.setPositionName(employee.getPosition() != null
                ? employee.getPosition().getPositionName()
                : null);

        response.setHireDate(employee.getHireDate());
        response.setStatus(String.valueOf(employee.getStatus()));
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

}
