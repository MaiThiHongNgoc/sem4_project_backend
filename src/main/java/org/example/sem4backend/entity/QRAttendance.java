package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "qr_attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRAttendance {

    @Id
    @Column(name = "qr_id", columnDefinition = "CHAR(36)")
    String qrId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "qr_info_id")
    QRInfo qrInfo;

    @Column(name = "scan_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date scanTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    ActiveStatus activeStatus;

    @Column(name = "attendance_date", nullable = false)
    @Temporal(TemporalType.DATE)
    Date attendanceDate;

    @Column(name = "face_recognition_image",columnDefinition = "MEDIUMTEXT")
    String faceRecognitionImage;

    @Column(name = "latitude", precision = 10, scale = 8)
    BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_method", nullable = false)
    AttendanceMethod attendanceMethod;

    public enum Status {
        CheckIn,
        CheckOut,
        Present,
        Late,
        Absent,
        OnLeave
    }

    public enum ActiveStatus {
        Active,
        Inactive
    }

    public enum AttendanceMethod {
        QR,
        FaceGPS,
        Unknown
    }

    @PrePersist
    public void prePersist() {
        if (this.qrId == null) {
            this.qrId = UUID.randomUUID().toString();
        }
        if (this.scanTime == null) {
            this.scanTime = new Date();
        }
        if (this.attendanceDate == null) {
            this.attendanceDate = new Date();
        }
        if (this.attendanceMethod == null) {
            if (this.qrInfo != null) {
                this.attendanceMethod = AttendanceMethod.QR;
            } else if (this.faceRecognitionImage != null
                    || (this.latitude != null && this.longitude != null)) {
                this.attendanceMethod = AttendanceMethod.FaceGPS;
            } else {
                this.attendanceMethod = AttendanceMethod.Unknown;
            }
        }
    }
}