package org.example.sem4backend.dto;

import lombok.Data;

import java.util.Date;

@Data
public class QRAttendanceDTO {

    private String employeeId;      // employee_id
    private String qrInfoId;        // qr_info_id (có thể null)
    private Date scanTime;          // scan_time (nếu muốn override)
    private String status;          // enum: CheckIn, CheckOut, ...
    private Date attendanceDate;    // ngày chấm công (có thể null để auto lấy ngày hiện tại)
    private String faceRecognitionImage;
    private Double latitude;
    private Double longitude;

}
