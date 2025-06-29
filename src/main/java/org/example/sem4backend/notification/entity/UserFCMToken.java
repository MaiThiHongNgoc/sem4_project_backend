package org.example.sem4backend.notification.entity;

import jakarta.persistence.*;
import org.example.sem4backend.entity.User;

import java.util.UUID;

@Entity
@Table(name = "user_fcm_token")
public class UserFCMToken {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Entity User có liên kết với Role

    @Column(name = "fcm_token")
    private String fcmToken;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}