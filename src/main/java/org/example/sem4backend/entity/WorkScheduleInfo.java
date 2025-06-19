package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "work_schedule_infos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleInfo {

    @Id
    @Column(name = "schedule_info_id", columnDefinition = "CHAR(36)")
    String scheduleInfoId;
    @PrePersist
    public void prePersist() {
        if (this.scheduleInfoId == null) {
            this.scheduleInfoId = UUID.randomUUID().toString();
        }
    }

    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Column(name = "description")
    String description;

    @Column(name = "default_start_time", nullable = false)
    @Temporal(TemporalType.TIME)
    java.util.Date defaultStartTime;

    @Column(name = "default_end_time", nullable = false)
    @Temporal(TemporalType.TIME)
    java.util.Date defaultEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}