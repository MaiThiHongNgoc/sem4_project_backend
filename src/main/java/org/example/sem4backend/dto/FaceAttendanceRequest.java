package org.example.sem4backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaceAttendanceRequest {
    private String employeeId;
    private String imageBase64;
    private Double latitude;
    private Double longitude;
}
