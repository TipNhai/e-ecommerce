package com.example.demo.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisTokenService {
    RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-duration}")
    @NonFinal
    protected int refreshDuration;

    @Value("${jwt.remaining-Time:180}")
    @NonFinal
    protected int remainingTime;

    String prefixRefreshToken = "refresh:";
    String prefixBlacklist = "blacklist:";

    public void blacklistToken(String accessToken, long seconds) {
        String key = prefixBlacklist + accessToken;
        long ttl = seconds > 0 ? seconds : remainingTime;
        redisTemplate.opsForValue().set(key, "true", Duration.ofSeconds(ttl));
    }

    public void blacklistToken(String accessToken) {
        blacklistToken(accessToken, remainingTime);
    }

    // Kiểm tra xem có nằm trong blacklist không
    public boolean isTokenBlacklist(String accessToken) {
        String key = prefixBlacklist + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // lưu refresh token mới
    public void saveRefreshToken(String userId, String refreshToken) {
        String key = prefixRefreshToken + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(refreshDuration));
    }

    // lấy refresh token
    public String getRefreshToken(String userId) {
        Object token = redisTemplate.opsForValue().get(prefixRefreshToken + userId);
        if (token == null) {
            // Hỗ trợ fallback key cũ có dấu cách "refresh: "
            token = redisTemplate.opsForValue().get("refresh: " + userId);
        }
        return token != null ? token.toString() : null;
    }

    // xoá refresh token khi logout
    public void deleteToken(String userId) {
        redisTemplate.delete(prefixRefreshToken + userId);
        redisTemplate.delete("refresh: " + userId);
    }
}