package org.example.sem4backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.sem4backend.entity.EmployeeHistory;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeHistoryRequest {
    String employeeId;
    String departmentId;
    String positionId;
    String startDate;
    String endDate;
    String reason;
    EmployeeHistory.Status status;
}
