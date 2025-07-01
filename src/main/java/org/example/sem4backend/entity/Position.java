package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Position {

    @Id
    @Column(name = "position_id", columnDefinition = "CHAR(36)")
    String positionId;

    @PrePersist
    public void prePersist() {
        if (this.positionId == null) {
            this.positionId = UUID.randomUUID().toString();
        }
    }


    @Column(name = "position_name", unique = true, nullable = false)
    String positionName;

    @Enumerated(EnumType.STRING)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}