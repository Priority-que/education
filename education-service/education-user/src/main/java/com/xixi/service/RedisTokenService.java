package com.xixi.service;

public interface RedisTokenService {
    void saveRefreshToken(Long userId, String deviceId, String refreshToken, long ttlSeconds);

    boolean verifyRefreshToken(Long userId, String deviceId, String refreshToken);

    void rotateRefreshToken(Long userId, String deviceId, String oldToken, String newToken, long ttlSeconds);

    void blacklistAccessToken(String jti, long ttlSeconds);

    boolean isAccessTokenBlacklisted(String jti);

    int getTokenVersion(Long userId);

    long incrTokenVersion(Long userId);

    void removeRefreshToken(Long userId, String deviceId);

    void removeAllRefreshTokenForUser(Long userId);

    void saveSessionMeta(Long userId, String deviceId, String ip, String userAgent, long ttlSeconds);
}

