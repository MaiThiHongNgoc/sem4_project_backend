package org.example.sem4backend.util;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class FacePlusPlusUtil {

    private static final String API_KEY = "j0TjUDY3p3qeeVC3rpIxISWi1AkeNh7W";
    private static final String API_SECRET = "QfNedoOjhtMghxsBlc0Ez1vUEanO0_am";
    private static final String API_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";

    public static double getConfidence(String base64Img1, String base64Img2) throws IOException {
        base64Img1 = cleanBase64(base64Img1);
        base64Img2 = cleanBase64(base64Img2);

        if (base64Img1 == null || base64Img1.isEmpty() || base64Img2 == null || base64Img2.isEmpty()) {
            throw new IOException("Base64 image is empty or null");
        }

        if (base64Img1.length() > 2_000_000 || base64Img2.length() > 2_000_000) {
            throw new IOException("Base64 image is too large. Must be under 2MB.");
        }

        // Log thông tin base64 ảnh để debug
        System.out.println("📷 [Ảnh 1] length: " + base64Img1.length() + ", starts with: " + base64Img1.substring(0, 20));
        System.out.println("📷 [Ảnh 2] length: " + base64Img2.length() + ", starts with: " + base64Img2.substring(0, 20));

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("api_key", API_KEY)
                .add("api_secret", API_SECRET)
                .add("image_base64_1", base64Img1)
                .add("image_base64_2", base64Img2)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                System.err.println("❌ Face++ API error: " + response.code() + " - " + responseBody);
                throw new IOException("API call failed: " + response.code() + " - " + responseBody);
            }

            // Parse JSON để lấy confidence
            JSONObject json = new JSONObject(responseBody);
            if (!json.has("confidence")) {
                throw new IOException("No 'confidence' in Face++ response. Full response: " + responseBody);
            }

            return json.getDouble("confidence");
        }
    }

    // Hàm loại bỏ tiền tố "data:image/jpeg;base64,"
    private static String cleanBase64(String base64) {
        if (base64 == null) return null;
        if (base64.contains(",")) {
            return base64.substring(base64.indexOf(",") + 1).trim();
        }
        return base64.trim();
    }
}
