package org.example.sem4backend.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleInfoResponse {
    String scheduleInfoId;
    String name;
    String description;
    LocalTime defaultStartTime;
    LocalTime defaultEndTime;
    String status;
}