package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @Column(name = "employee_id", columnDefinition = "CHAR(36)")
    String employeeId;

    @PrePersist
    public void prePersist() {
        if (this.employeeId == null) {
            this.employeeId = UUID.randomUUID().toString();
        }
    }

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    Gender gender;

    public enum Gender {
        Male,
        Female,
        Other
    }

    @Column(name = "date_of_birth", nullable = false)
    LocalDate dateOfBirth;

    @Column(name = "phone", unique = true)
    String phone;

    @Column(name = "address")
    String address;

    @Column(name = "img", columnDefinition = "LONGTEXT")
    private String img;


    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @ManyToOne
    @JoinColumn(name = "position_id")
    Position position;

    @Column(name = "hire_date", nullable = false)
    LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Resigned,
        Retired,
        OnLeave
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
