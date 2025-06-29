package org.example.sem4backend.notification.controller;

import org.example.sem4backend.notification.repository.UserFCMTokenRepository;
import org.example.sem4backend.notification.service.FirebasePushService;
import org.example.sem4backend.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FirebasePushService firebasePushService;

    @Autowired
    private UserFCMTokenRepository tokenRepo;

    // ✅ Gửi push + lưu Firestore theo nhiều role
    @PostMapping("/push-to-roles")
    public ResponseEntity<String> pushToRoles(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        String senderId = (String) payload.get("sentBy");
        List<String> roles = (List<String>) payload.get("roles");

        List<String> tokens = tokenRepo.findFcmTokensByRoles(roles);
        for (String token : tokens) {
            try {
                firebasePushService.sendPushNotification(token, title, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        notificationService.saveNotification(title, message, senderId, roles, List.of());
        return ResponseEntity.ok("✅ Sent to roles: " + String.join(", ", roles));
    }

    // ✅ Gửi push đến 1 user cụ thể
    @PostMapping("/push")
    public ResponseEntity<String> pushToUser(@RequestBody Map<String, String> payload) throws IOException {
        firebasePushService.sendPushNotification(
                payload.get("fcmToken"),
                payload.get("title"),
                payload.get("message")
        );
        return ResponseEntity.ok("✅ Sent to 1 user");
    }

    // ✅ Lưu Firestore thông báo không gửi push
    @PostMapping("/save")
    public ResponseEntity<String> saveOnly(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        String senderId = (String) payload.get("sentBy");
        List<String> roles = (List<String>) payload.get("roles");
        List<String> userIds = (List<String>) payload.get("userIds");

        notificationService.saveNotification(title, message, senderId, roles, userIds);
        return ResponseEntity.ok("✅ Saved only");
    }
}