package org.example.sem4backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleRequest {
    String employeeId;
    String scheduleInfoId;
    Date workDay;
    Date startTime;
    Date endTime;
    String status;
}