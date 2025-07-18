package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
        if (this.isApproved == null) {
            this.isApproved = this.shiftType == ShiftType.Normal; // Normal -> mặc định duyệt, OT thì chưa
        }
        if (this.status == null) {
            // ✅ Nếu chưa có status, tự động đặt theo loại ca
            this.status = (this.shiftType == ShiftType.Normal) ? Status.Active : Status.Inactive;
        }
    }

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "schedule_info_id", referencedColumnName = "schedule_info_id")
    WorkScheduleInfo scheduleInfo;

    @Column(name = "work_day", nullable = false)
    LocalDate workDay;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    ShiftType shiftType;

    public enum ShiftType {
        Normal,
        OT
    }

    @Column(name = "is_approved")
    Boolean isApproved;
}