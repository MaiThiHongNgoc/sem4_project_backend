package org.example.sem4backend.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class FirebasePushService {

    private String accessToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        refreshAccessToken();
    }

    private void refreshAccessToken() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-service-account.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        credentials.refreshIfExpired();
        accessToken = credentials.getAccessToken().getTokenValue();
        System.out.println("🔑 Firebase accessToken = " + accessToken);

    }

    public boolean sendPushNotification(String token, String title, String body) throws IOException {
        refreshAccessToken();

        Map<String, Object> message = Map.of(
                "token", token,
                "notification", Map.of("title", title, "body", body)
        );

        Map<String, Object> requestBody = Map.of("message", message);

        URL url = new URL("https://fcm.googleapis.com/v1/projects/notifications-71ba5/messages:send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json; UTF-8");
        conn.setDoOutput(true);

        String json = objectMapper.writeValueAsString(requestBody);
        conn.getOutputStream().write(json.getBytes());

        int responseCode = conn.getResponseCode();
        conn.disconnect();
        return responseCode == 200;
    }

    public Map<String, Integer> sendPushToMultipleTokens(List<String> tokens, String title, String body) {
        int success = 0, failure = 0;
        for (String token : tokens) {
            try {
                boolean result = sendPushNotification(token, title, body);
                if (result) success++;
                else failure++;
            } catch (IOException e) {
                failure++;
            }
        }
        return Map.of("success", success, "failure", failure);
    }

    public boolean sendPushToTopic(String topic, String title, String body) throws IOException {
        refreshAccessToken();

        Map<String, Object> message = Map.of(
                "topic", topic,
                "notification", Map.of("title", title, "body", body)
        );

        Map<String, Object> requestBody = Map.of("message", message);

        URL url = new URL("https://fcm.googleapis.com/v1/projects/notifications-71ba5/messages:send");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json; UTF-8");
        conn.setDoOutput(true);

        String json = objectMapper.writeValueAsString(requestBody);
        conn.getOutputStream().write(json.getBytes());

        int responseCode = conn.getResponseCode();
        conn.disconnect();
        return responseCode == 200;
    }
}