package com.xixi.util;

import com.xixi.config.AuthJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_UNAME = "uname";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_JTI = "jti";
    public static final String CLAIM_VER = "ver";
    public static final String TYPE_ACCESS = "access";

    private final AuthJwtProperties jwtProperties;

    public JwtUtil(AuthJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Claims parseClaims(String token) throws JwtException, IllegalArgumentException {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        String type = claims.get(CLAIM_TYPE, String.class);
        String issuer = claims.getIssuer();
        return TYPE_ACCESS.equals(type) && jwtProperties.getIssuer().equals(issuer);
    }

    public Long getUserId(Claims claims) {
        Object uid = claims.get(CLAIM_UID);
        if (uid == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(uid));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getRole(Claims claims) {
        Object role = claims.get(CLAIM_ROLE);
        if (role == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(role));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getTokenVersion(Claims claims) {
        Object version = claims.get(CLAIM_VER);
        if (version == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(version));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

