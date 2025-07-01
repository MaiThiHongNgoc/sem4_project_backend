package org.example.sem4backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleResponse {
    String scheduleId;
    String employeeId;
    String employeeName;
    String scheduleInfoId;
    String scheduleInfoName;       
    Date workDay;
    Date startTime;
    Date endTime;
    String status;
}