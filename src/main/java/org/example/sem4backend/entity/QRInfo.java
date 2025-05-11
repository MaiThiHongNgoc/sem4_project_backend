package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "qr_infos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRInfo {

    @Id
    @Column(name = "qr_info_id", columnDefinition = "CHAR(36)")
    UUID qrInfoId;

    @Column(name = "qr_code", nullable = false, unique = true)
    String qrCode;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "location_name")
    String locationName;

    @ManyToOne
    @JoinColumn(name = "created_by")
    User createdBy;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    java.util.Date createdAt;

    @Column(name = "expired_at")
    @Temporal(TemporalType.DATE)
    java.util.Date expiredAt;

    @Column(name = "active")
    Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift")
    Shift shift;

    @Column(name = "device_info")
    String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Shift {
        Morning,
        Afternoon,
        Evening,
        Night
    }

    public enum Status {
        Active,
        Inactive
    }
}