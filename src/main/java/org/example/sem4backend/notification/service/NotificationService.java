package org.example.sem4backend.notification.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.sem4backend.notification.dto.NotificationResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final Firestore db = FirestoreClient.getFirestore();

    public void saveNotification(String title, String message, String senderId, List<String> roles, List<String> userIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("sentAt", new Date());
        data.put("sentBy", senderId);
        data.put("roles", roles);
        data.put("userIds", userIds);

        db.collection("notifications").add(data);
    }

    public List<NotificationResponse> getNotifications(String userId, String role, int page, int size) {
        List<NotificationResponse> results = new ArrayList<>();

        try {
            // Lấy tất cả thông báo, sắp xếp theo ngày giảm dần
            ApiFuture<QuerySnapshot> query = db.collection("notifications")
                    .orderBy("sentAt", Query.Direction.DESCENDING)
                    .get();
            List<QueryDocumentSnapshot> docs = query.get().getDocuments();

            // Lấy danh sách ID thông báo đã đọc
            DocumentSnapshot readSnap = db.collection("notifications_read").document(userId).get().get();
            List<String> readList = readSnap.exists()
                    ? (List<String>) readSnap.get("read")
                    : new ArrayList<>();

            // Lọc theo người dùng hoặc role, thêm isRead
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
                        res.setIsRead(readList.contains(doc.getId()));
                        return res;
                    })
                    .filter(n -> n.getUserIds().contains(userId) || n.getRoles().contains(role))
                    .collect(Collectors.toList());

            // Phân trang
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

    public void markAsRead(String userId, String notificationId) {
        try {
            DocumentReference docRef = db.collection("notifications_read").document(userId);
            DocumentSnapshot snap = docRef.get().get();
            List<String> readList = snap.exists()
                    ? (List<String>) snap.get("read")
                    : new ArrayList<>();

            if (!readList.contains(notificationId)) {
                readList.add(notificationId);
                docRef.set(Map.of("read", readList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(String userId, List<String> notificationIds) {
        try {
            Set<String> allIds = new HashSet<>(notificationIds);
            db.collection("notifications_read")
                    .document(userId)
                    .set(Map.of("read", new ArrayList<>(allIds)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> toStringList(Object obj) {
        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else if (obj instanceof String) {
            return List.of(obj.toString());
        }
        return List.of();
    }
}
