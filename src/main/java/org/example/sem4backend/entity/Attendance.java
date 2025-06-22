package org.example.sem4backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attendance {

    @Id
    @Column(name = "attendance_id", columnDefinition = "CHAR(36)")
    String attendanceId;
    @PrePersist
    public void prePersist() {
        if (this.attendanceId == null) {
            this.attendanceId = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @Column(name = "attendance_date", nullable = false)
    @Temporal(TemporalType.DATE)
    java.util.Date attendanceDate;

    @Column(name = "total_hours", precision = 5, scale = 2)
    BigDecimal totalHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    ActiveStatus activeStatus;

    public enum Status {
        Present,
        Absent,
        Late,
        OnLeave
    }

    public enum ActiveStatus {
        Active,
        Inactive
    }
}