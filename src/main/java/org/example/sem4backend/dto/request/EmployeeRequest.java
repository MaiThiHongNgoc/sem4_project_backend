package org.example.sem4backend.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeRequest {
    String fullName;
    String gender;
    LocalDate dateOfBirth;
    String phone;
    String email;
    String address;
    UUID departmentId;
    UUID positionId;
    LocalDate hireDate;
}