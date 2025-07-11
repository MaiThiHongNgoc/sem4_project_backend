package org.example.sem4backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
    LocalDate workDay;
    Date startTime;
    Date endTime;
    String status;
    private String shiftType; // "Normal" hoặc "OT"
    private Boolean isApproved; // chỉ dùng cho admin nếu cần tạo thủ công
}