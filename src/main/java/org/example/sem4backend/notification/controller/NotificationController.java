package org.example.sem4backend.notification.controller;

import org.example.sem4backend.notification.dto.NotificationResponse;
import org.example.sem4backend.notification.repository.UserFCMTokenRepository;
import org.example.sem4backend.notification.service.FirebasePushService;
import org.example.sem4backend.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    @Autowired
    private FirebasePushService firebasePushService;

    @Autowired
    private UserFCMTokenRepository tokenRepo;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam String userId,
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<NotificationResponse> notifications = notificationService.getNotifications(userId, role, page, size);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/push-to-roles")
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<String> pushToRoles(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        String senderId = (String) payload.get("sentBy");
        List<String> roles = (List<String>) payload.get("roles");

        List<String> tokens = tokenRepo.findFcmTokensByRoles(roles);
        Map<String, Integer> result = firebasePushService.sendPushToMultipleTokens(tokens, title, message);

        notificationService.saveNotification(title, message, senderId, roles, List.of());

        return ResponseEntity.ok("✅ Sent to roles: " + String.join(", ", roles) +
                " | Success: " + result.get("success") +
                " | Failed: " + result.get("failure"));
    }

    @PostMapping("/push-to-user")
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<String> pushToUser(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String title = payload.get("title");
        String message = payload.get("message");

        List<String> tokens = tokenRepo.findFcmTokensByUserId(userId);
        Map<String, Integer> result = firebasePushService.sendPushToMultipleTokens(tokens, title, message);
        notificationService.saveNotification(title, message, "system", List.of("User"), List.of());



        return ResponseEntity.ok("✅ Sent to user: " + userId +
                " | Success: " + result.get("success") +
                " | Failed: " + result.get("failure"));
    }

    @PostMapping("/push-to-topic")
    @PreAuthorize("hasAnyRole('Admin', 'Hr')")
    public ResponseEntity<String> pushToTopic(@RequestBody Map<String, String> payload) throws IOException {
        String topic = payload.get("topic");
        String title = payload.get("title");
        String message = payload.get("message");

        boolean sent = firebasePushService.sendPushToTopic(topic, title, message);
        notificationService.saveNotification(
                title,
                message,
                "system", // hoặc truyền senderId nếu có
                List.of(topic), // topic tương ứng với role, ví dụ: "Employee"
                List.of()       // không gửi cho user cụ thể
        );
        return ResponseEntity.ok("✅ Topic " + topic + " | Status: " + (sent ? "Success" : "Failed"));
    }
}