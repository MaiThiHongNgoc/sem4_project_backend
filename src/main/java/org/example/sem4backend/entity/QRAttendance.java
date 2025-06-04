package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

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

    @PrePersist
    public void prePersist() {
        if (this.qrId == null) {
            this.qrId = java.util.UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "qr_info_id")
    QRInfo qrInfo;

    @Column(name = "scan_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date scanTime;

    @Column(name = "attendance_date", nullable = false)
    @Temporal(TemporalType.DATE)
    Date attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    ActiveStatus activeStatus;

    public enum Status {
        CheckIn,
        CheckOut,
        Present,
        Late,
        Absent,
        On_Leave
    }

    public enum ActiveStatus {
        Active,
        Inactive
    }
}
