package org.example.sem4backend.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    String employeeId;
    String fullName;
    String gender;
    LocalDate dateOfBirth;
    String positionName;
    String departmentName;
    String phone;
    String address;
    String img;
    String departmentId;
    String positionId;
    LocalDate hireDate;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
