package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Leave {

    @Id
    @Column(name = "leave_id", columnDefinition = "CHAR(36)")
    UUID leaveId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @Column(name = "leave_start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    java.util.Date leaveStartDate;

    @Column(name = "leave_end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    java.util.Date leaveEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    LeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    LeaveStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    ActiveStatus activeStatus;

    public enum LeaveType {
        SickLeave,
        AnnualLeave,
        UnpaidLeave,
        Other
    }

    public enum LeaveStatus {
        Pending,
        Approved,
        Rejected
    }

    public enum ActiveStatus {
        Active,
        Inactive
    }
}