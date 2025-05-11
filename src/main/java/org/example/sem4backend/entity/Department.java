package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Department {

    @Id
    @Column(name = "department_id", columnDefinition = "CHAR(36)")
    UUID departmentId;

    @Column(name = "department_name", unique = true, nullable = false)
    String departmentName;

    @Enumerated(EnumType.STRING)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}