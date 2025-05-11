package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @Column(name = "employee_id", columnDefinition = "CHAR(36)")
    UUID employeeId;

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
    String dateOfBirth;

    @Column(name = "phone", unique = true)
    String phone;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Column(name = "address")
    String address;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @ManyToOne
    @JoinColumn(name = "position_id")
    Position position;

    @Column(name = "hire_date", nullable = false)
    String hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Resigned,
        Retired,
        OnLeave
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    String createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    String updatedAt;
}