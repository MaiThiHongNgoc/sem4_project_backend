package org.example.sem4backend.util;

import okhttp3.*;
import java.io.IOException;

public class FacePlusPlusUtil {

    private static final String API_KEY = "j0TjUDY3p3qeeVC3rpIxISWi1AkeNh7W";
    private static final String API_SECRET = "QfNedoOjhtMghxsBlc0Ez1vUEanO0_am";
    private static final String API_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";

    public static double getConfidence(String base64Img1, String base64Img2) throws IOException {
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
            if (!response.isSuccessful()) {
                throw new IOException("API call failed: " + response);
            }

            String json = response.body().string();

            // Lấy confidence chính xác hơn bằng JSON parsing (tốt hơn regex nếu có thể dùng thư viện như Jackson/Gson)
            String confidenceStr = json.replaceAll(".*\"confidence\":\\s*(\\d+\\.?\\d*).*", "$1");
            return Double.parseDouble(confidenceStr);
        }
    }
}
