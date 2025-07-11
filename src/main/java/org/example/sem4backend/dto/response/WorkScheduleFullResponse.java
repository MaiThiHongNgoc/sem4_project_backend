package org.example.sem4backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class WorkScheduleFullResponse {
    String scheduleId;

    // Employee
    String employeeId;
    String employeeName;
    String employeeEmail;

    // Schedule Info
    String scheduleInfoId;
    String scheduleInfoName;
    Date scheduleStartTime;
    Date scheduleEndTime;

    Date defaultStartTime;
    Date defaultEndTime;

    // Work Schedule
    LocalDate workDay;
    Date startTime;
    Date endTime;
    String status;
}