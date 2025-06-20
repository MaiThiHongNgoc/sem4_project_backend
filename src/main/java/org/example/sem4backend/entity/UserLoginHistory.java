package org.example.sem4backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_login_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginHistory {

    @Id
    @Column(name = "login_history_id", columnDefinition = "CHAR(36)")
    String loginHistoryId;

    @PrePersist
    public void prePersist() {
        if (this.loginHistoryId == null) {
            this.loginHistoryId = UUID.randomUUID().toString();
        }
        if (this.loginTime == null) {
            this.loginTime = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "login_time", nullable = false)
    LocalDateTime loginTime;

    @Column(name = "ip_address", length = 50)
    String ipAddress;

    @Column(name = "device_info", length = 255)
    String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    public enum Status {
        Active,
        Inactive
    }
}
