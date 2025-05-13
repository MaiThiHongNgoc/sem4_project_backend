package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    UUID qrId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "qr_info_id")
    QRInfo qrInfo;

    @Column(name = "scan_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    java.util.Date scanTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    ActiveStatus activeStatus;

    public enum Status {
        CheckIn,
        CheckOut
    }

    public enum ActiveStatus {
        Active,
        Inactive
    }
}