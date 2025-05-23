package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
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
    UUID qrInfoId = UUID.randomUUID();

    @Column(name = "qr_code", nullable = false, unique = true)
    String qrCode;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    Location location;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    Date createdAt = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "expired_at")
    Date expiredAt;

    @Column(name = "active")
    Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift")
    Shift shift;

    @Column(name = "device_info")
    String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status = Status.ACTIVE;

    public enum Shift {
        Morning,
        Afternoon,
        Evening,
        Night
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        DELETED
    }
}
