package org.example.sem4backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.otp.expirationTime}")
    private long otpExpirationTime;

    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Lưu OTP vào Redis với thời gian hết hạn
    public void storeOtpInRedis(String email, String otpCode) {
        redisTemplate.opsForValue().set(email, otpCode, otpExpirationTime, TimeUnit.MINUTES);
    }

    // Lấy OTP từ Redis
    public String getOtpFromRedis(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // Xóa OTP khỏi Redis sau khi sử dụng
    public void deleteOtpFromRedis(String email) {
        redisTemplate.delete(email);
    }

    // Kiểm tra xem OTP có còn hiệu lực trong Redis không
    public boolean isOtpExpired(String email) {
        return redisTemplate.getExpire(email, TimeUnit.MINUTES) <= 0;
    }
}
