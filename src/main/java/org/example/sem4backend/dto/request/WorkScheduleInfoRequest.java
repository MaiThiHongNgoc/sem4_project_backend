package org.example.sem4backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WorkScheduleInfoRequest {
    @NotBlank(message = "Name cannot be blank")
    String name;
    String description;

    @NotNull(message = "Default start time cannot be null")
    LocalTime defaultStartTime;

    @NotNull(message = "Default end time cannot be null")
    LocalTime defaultEndTime;

    @NotBlank(message = "Status cannot be blank")
    String status;
}