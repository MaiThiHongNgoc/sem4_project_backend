package org.example.sem4backend.dto;

import lombok.Data;

@Data
public class QRAttendanceRequest {
    private String qrCode;
    private String employeeId;
    private String status; // "CheckIn" or "CheckOut"
}