package org.example.sem4backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleRequest {
    String employeeId;
    String scheduleInfoId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDay;

    @JsonFormat(pattern = "HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private Date endTime;
    String status;
    private String shiftType; // "Normal" hoặc "OT"
    private Boolean isApproved; // chỉ dùng cho admin nếu cần tạo thủ công
}