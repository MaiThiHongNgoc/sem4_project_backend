package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "employee_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeHistory {

    @Id
    @Column(name = "history_id", columnDefinition = "CHAR(36)")
    String historyId;

    @PrePersist
    public void prePersist() {
        if (this.historyId == null) {
            this.historyId = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @ManyToOne
    @JoinColumn(name = "position_id")
    Position position;

    @Column(name = "start_date", nullable = false)
    String startDate;

    @Column(name = "end_date")
    String endDate;

    @Column(name = "reason", length = 255)
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}