package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "work_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkSchedule {

    @Id
    @Column(name = "schedule_id", columnDefinition = "CHAR(36)")
    String scheduleId;

    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) {
            this.scheduleId = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "schedule_info_id", referencedColumnName = "schedule_info_id")
    WorkScheduleInfo scheduleInfo;

    @Column(name = "work_day", nullable = false)
    @Temporal(TemporalType.DATE)
    java.util.Date workDay;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIME)
    Date startTime;

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIME)
    Date endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}