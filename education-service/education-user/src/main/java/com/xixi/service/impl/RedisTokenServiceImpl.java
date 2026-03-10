package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.xixi.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {
    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String SESSION_PREFIX = "auth:session:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String TOKEN_VERSION_PREFIX = "auth:token:ver:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRefreshToken(Long userId, String deviceId, String refreshToken, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                buildRefreshKey(userId, deviceId),
                sha256(refreshToken),
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public boolean verifyRefreshToken(Long userId, String deviceId, String refreshToken) {
        String storedHash = stringRedisTemplate.opsForValue().get(buildRefreshKey(userId, deviceId));
        if (!StringUtils.hasText(storedHash)) {
            return false;
        }
        byte[] a = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] b = sha256(refreshToken).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    @Override
    public void rotateRefreshToken(Long userId, String deviceId, String oldToken, String newToken, long ttlSeconds) {
        if (!verifyRefreshToken(userId, deviceId, oldToken)) {
            return;
        }
        saveRefreshToken(userId, deviceId, newToken, ttlSeconds);
    }

    @Override
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        if (!StringUtils.hasText(jti) || ttlSeconds <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "1",
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public boolean isAccessTokenBlacklisted(String jti) {
        if (!StringUtils.hasText(jti)) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    @Override
    public int getTokenVersion(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(TOKEN_VERSION_PREFIX + userId);
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public long incrTokenVersion(Long userId) {
        Long value = stringRedisTemplate.opsForValue().increment(TOKEN_VERSION_PREFIX + userId);
        return value == null ? 0 : value;
    }

    @Override
    public void removeRefreshToken(Long userId, String deviceId) {
        String refreshKey = buildRefreshKey(userId, deviceId);
        String sessionKey = buildSessionKey(userId, deviceId);
        stringRedisTemplate.delete(refreshKey);
        stringRedisTemplate.delete(sessionKey);
    }

    @Override
    public void removeAllRefreshTokenForUser(Long userId) {
        Set<String> refreshKeys = stringRedisTemplate.keys(REFRESH_PREFIX + userId + ":*");
        Set<String> sessionKeys = stringRedisTemplate.keys(SESSION_PREFIX + userId + ":*");
        if (refreshKeys != null && !refreshKeys.isEmpty()) {
            stringRedisTemplate.delete(refreshKeys);
        }
        if (sessionKeys != null && !sessionKeys.isEmpty()) {
            stringRedisTemplate.delete(sessionKeys);
        }
    }

    @Override
    public void saveSessionMeta(Long userId, String deviceId, String ip, String userAgent, long ttlSeconds) {
        Map<String, Object> map = new HashMap<>();
        map.put("ip", ip);
        map.put("userAgent", userAgent);
        map.put("loginTime", LocalDateTime.now().toString());
        stringRedisTemplate.opsForValue().set(
                buildSessionKey(userId, deviceId),
                JSONUtil.toJsonStr(map),
                Duration.ofSeconds(ttlSeconds)
        );
    }

    private String buildRefreshKey(Long userId, String deviceId) {
        return REFRESH_PREFIX + userId + ":" + deviceId;
    }

    private String buildSessionKey(Long userId, String deviceId) {
        return SESSION_PREFIX + userId + ":" + deviceId;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("sha256 not supported", e);
        }
    }
}

