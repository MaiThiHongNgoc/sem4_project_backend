package org.example.sem4backend.notification.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    public void saveNotification(String title, String message, String senderId, List<String> roles, List<String> userIds) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("sentAt", new Date());
        data.put("sentBy", senderId);
        data.put("roles", roles);
        data.put("userIds", userIds);

        db.collection("notifications").add(data);
    }
}