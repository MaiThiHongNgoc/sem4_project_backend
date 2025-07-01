package org.example.sem4backend.notification.controller;

import org.example.sem4backend.entity.User;
import org.example.sem4backend.notification.entity.UserFCMToken;
import org.example.sem4backend.notification.repository.UserFCMTokenRepository;
import org.example.sem4backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/fcm")
public class FCMTokenController {

    @Autowired
    private UserFCMTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestBody TokenRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();

        // Kiểm tra xem token này đã tồn tại cho user chưa
        UserFCMToken existing = tokenRepository.findByUserAndFcmToken(user, request.getFcmToken());

        if (existing == null) {
            UserFCMToken token = new UserFCMToken();
            token.setId(UUID.randomUUID().toString());
            token.setUser(user);
            token.setFcmToken(request.getFcmToken());
            tokenRepository.save(token);
        }

        return ResponseEntity.ok("✅ Token registered");
    }

    public static class TokenRequest {
        private String userId;
        private String fcmToken;

        // Getters & Setters
        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getFcmToken() {
            return fcmToken;
        }
        public void setFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
        }
    }
}
