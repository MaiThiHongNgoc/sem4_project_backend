package org.example.sem4backend.notification.service;

import org.example.sem4backend.notification.repository.UserFCMTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScheduledNotificationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FirebasePushService firebasePushService;

    @Autowired
    private UserFCMTokenRepository tokenRepo;

    @Scheduled(cron = "0 0 8 * * *") // 9h sáng mỗi ngày
    public void sendDailyReminder() {
        String title = "Chào buổi sáng!";
        String message = "Chúc bạn một ngày tốt lành.";

        List<String> tokens = tokenRepo.findFcmTokensByRoles(List.of("User"));
        Map<String, Integer> result = firebasePushService.sendPushToMultipleTokens(tokens, title, message);
        notificationService.saveNotification(title, message, "system", List.of("User"), List.of());


        System.out.println("[Scheduler] Sent reminder | Success: " + result.get("success") + ", Failed: " + result.get("failure"));
    }
}

