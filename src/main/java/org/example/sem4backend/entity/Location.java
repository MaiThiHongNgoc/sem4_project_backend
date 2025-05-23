package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @Column(name = "location_id", columnDefinition = "CHAR(36)")
    private String locationId;

    @PrePersist
    public void prePersist() {
        if (this.locationId == null) {
            this.locationId = UUID.randomUUID().toString();
        }
    }

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "created_by", columnDefinition = "CHAR(36)")
    private String createdBy;

    @Column(name = "created_at", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;


    private Boolean active = true;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_fixed_location")
    private Boolean isFixedLocation = false;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, INACTIVE, DELETED
    }
}
