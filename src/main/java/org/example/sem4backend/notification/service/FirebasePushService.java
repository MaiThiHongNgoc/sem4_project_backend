package org.example.sem4backend.notification.service;

import org.springframework.stereotype.Service;


import java.io.IOException;
import okhttp3.*;

@Service
public class FirebasePushService {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";

    // 🔁 THAY BẰNG Firebase Cloud Messaging server key của bạn
    private static final String SERVER_KEY = "gwSCap2ewXSvc_qbJj3Owjtuq7yCAsM0NEzcbuOW8Ec";

    public void sendPushNotification(String fcmToken, String title, String message) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String json = "{"
                + "\"to\":\"" + fcmToken + "\","
                + "\"notification\":{"
                +     "\"title\":\"" + title + "\","
                +     "\"body\":\"" + message + "\""
                + "}"
                + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(FCM_API_URL)
                .post(body)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("✅ FCM response: " + response.body().string());
    }
}
