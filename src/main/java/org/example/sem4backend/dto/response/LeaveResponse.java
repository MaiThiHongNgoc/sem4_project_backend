package org.example.sem4backend.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeaveResponse {
    String leaveId;
    UUID employeeId;
    String employeeName;
    Date leaveStartDate;
    Date leaveEndDate;
    String leaveType;
    String status;
    String activeStatus;
    LocalDateTime createdAt;
}
