package org.example.sem4backend.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "attendance_appeals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceAppeal {

    @Id
    @Column(name = "appeal_id", columnDefinition = "CHAR(36)")
    String appealId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    Attendance attendance;

    @Column(name = "appeal_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date appealDate;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    String reason;

    @Column(name = "evidence",columnDefinition = "LONGTEXT")
    String evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    User reviewedBy;

    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date reviewedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    public enum Status {
        Pending,
        Approved,
        Rejected
    }

    @PrePersist
    public void prePersist() {
        if (this.appealId == null) {
            this.appealId = UUID.randomUUID().toString();
        }
        if (this.appealDate == null) {
            this.appealDate = new Date();
        }
        if (this.status == null) {
            this.status = Status.Pending;
        }
    }
}

