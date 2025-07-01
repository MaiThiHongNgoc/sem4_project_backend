package org.example.sem4backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.sem4backend.entity.EmployeeHistory;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeHistoryResponse {
    String historyId;
    String employeeId;
    String employeeName;
    String departmentId;
    String departmentName;
    String positionId;
    String positionName;
    String startDate;
    String endDate;
    String reason;
    EmployeeHistory.Status status;
}
