package com.xixi.util;

import com.xixi.config.AuthJwtProperties;
import com.xixi.entity.Users;
import com.xixi.exception.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_UNAME = "uname";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_JTI = "jti";
    public static final String CLAIM_VER = "ver";
    public static final String CLAIM_DEVICE = "did";

    private final AuthJwtProperties jwtProperties;

    public JwtUtil(AuthJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Users user, String deviceId, int tokenVersion) {
        return generateToken(user, deviceId, tokenVersion, TYPE_ACCESS, jwtProperties.getAccessExpireSeconds());
    }

    public String generateRefreshToken(Users user, String deviceId, int tokenVersion) {
        return generateToken(user, deviceId, tokenVersion, TYPE_REFRESH, jwtProperties.getRefreshExpireSeconds());
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(401, "token无效或已过期");
        }
    }

    public boolean validateToken(String token, String expectedType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(CLAIM_TYPE, String.class);
        String issuer = claims.getIssuer();
        return expectedType.equals(tokenType) && jwtProperties.getIssuer().equals(issuer);
    }

    public long getRemainingSeconds(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return getRemainingSeconds(claims);
        } catch (ExpiredJwtException expiredJwtException) {
            return getRemainingSeconds(expiredJwtException.getClaims());
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }

    public String getJti(String token) {
        return parseClaims(token).get(CLAIM_JTI, String.class);
    }

    public Long getUserId(String token) {
        Object uid = parseClaims(token).get(CLAIM_UID);
        if (uid == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(uid));
    }

    public Integer getRole(String token) {
        Object role = parseClaims(token).get(CLAIM_ROLE);
        if (role == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(role));
    }

    public Integer getTokenVersion(String token) {
        Object version = parseClaims(token).get(CLAIM_VER);
        if (version == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(version));
    }

    public String getDeviceId(String token) {
        return parseClaims(token).get(CLAIM_DEVICE, String.class);
    }

    public long getAccessExpireSeconds() {
        return jwtProperties.getAccessExpireSeconds();
    }

    public long getRefreshExpireSeconds() {
        return jwtProperties.getRefreshExpireSeconds();
    }

    private String generateToken(Users user, String deviceId, int tokenVersion, String tokenType, long expireSeconds) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        String jti = UUID.randomUUID().toString().replace("-", "");

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim(CLAIM_UID, user.getId())
                .claim(CLAIM_UNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_TYPE, tokenType)
                .claim(CLAIM_JTI, jti)
                .claim(CLAIM_VER, tokenVersion)
                .claim(CLAIM_DEVICE, deviceId)
                .signWith(getSecretKey())
                .compact();
    }

    private long getRemainingSeconds(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return 0;
        }
        long diff = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(diff, 0);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
