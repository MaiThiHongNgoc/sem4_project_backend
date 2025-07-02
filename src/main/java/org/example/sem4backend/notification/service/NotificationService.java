package org.example.sem4backend.notification.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.sem4backend.notification.dto.NotificationResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.redis.connection.util.DecodeUtils.convertToList;

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

    // ✅ NEW: Lấy danh sách thông báo theo userId hoặc role, phân trang
    public List<NotificationResponse> getNotifications(String userId, String role, int page, int size) {
        Firestore db = FirestoreClient.getFirestore();
        List<NotificationResponse> results = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> query = db.collection("notifications")
                    .orderBy("sentAt", Query.Direction.DESCENDING)
                    .get();

            List<QueryDocumentSnapshot> docs = query.get().getDocuments();

            List<NotificationResponse> filtered = docs.stream()
                    .map(doc -> {
                        Map<String, Object> data = doc.getData();
                        NotificationResponse res = new NotificationResponse();
                        res.setId(doc.getId());
                        res.setTitle((String) data.get("title"));
                        res.setMessage((String) data.get("message"));
                        res.setSentAt(data.get("sentAt") instanceof Timestamp ? ((Timestamp) data.get("sentAt")).toDate() : null);
                        res.setSentBy((String) data.get("sentBy"));
                        res.setRoles(toStringList(data.get("roles")));
                        res.setUserIds(toStringList(data.get("userIds")));
                        return res;
                    })
                    .filter(n -> n.getUserIds().contains(userId) || n.getRoles().contains(role))
                    .collect(Collectors.toList());

            int start = page * size;
            int end = Math.min(start + size, filtered.size());

            if (start < filtered.size()) {
                results = filtered.subList(start, end);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private List<String> toStringList(Object obj) {
        if (obj instanceof List) {
            return ((List<?>) obj).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else if (obj instanceof String) {
            return List.of(obj.toString()); // trường hợp roles là "Employee"
        }
        return List.of(); // nếu null hoặc kiểu khác
    }


}
