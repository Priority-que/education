package com.xixi.filter;

import com.xixi.config.AuthWhitelistProperties;
import com.xixi.service.RedisTokenService;
import com.xixi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationWebFilter implements WebFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_TOKEN_JTI = "X-Token-Jti";
    private static final String HEADER_TOKEN_VERSION = "X-Token-Version";

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final AuthWhitelistProperties authWhitelistProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationWebFilter(
            JwtUtil jwtUtil,
            RedisTokenService redisTokenService,
            AuthWhitelistProperties authWhitelistProperties
    ) {
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
        this.authWhitelistProperties = authWhitelistProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        if (isWhitelisted(path, authWhitelistProperties.getPaths())) {
            return chain.filter(exchange);
        }

        String token = extractBearerToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "未登录或token无效");
        }

        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "未登录或token无效");
        }

        if (!jwtUtil.isAccessToken(claims)) {
            return unauthorized(exchange, "未登录或token无效");
        }

        Long userId = jwtUtil.getUserId(claims);
        String username = claims.get(JwtUtil.CLAIM_UNAME, String.class);
        Integer role = jwtUtil.getRole(claims);
        Integer tokenVersion = jwtUtil.getTokenVersion(claims);
        String jti = claims.get(JwtUtil.CLAIM_JTI, String.class);
        if (userId == null || !StringUtils.hasText(username) || role == null || tokenVersion == null || !StringUtils.hasText(jti)) {
            return unauthorized(exchange, "未登录或token无效");
        }

        return Mono.zip(
                        redisTokenService.isAccessTokenBlacklisted(jti),
                        redisTokenService.getTokenVersion(userId)
                )
                .flatMap(tuple -> {
                    boolean blacklisted = tuple.getT1();
                    int redisVersion = tuple.getT2();
                    if (blacklisted || redisVersion != tokenVersion) {
                        return unauthorized(exchange, "未登录或token无效");
                    }

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header(HEADER_USER_ID, String.valueOf(userId))
                            .header(HEADER_USERNAME, username)
                            .header(HEADER_USER_ROLE, String.valueOf(role))
                            .header(HEADER_TOKEN_JTI, jti)
                            .header(HEADER_TOKEN_VERSION, String.valueOf(tokenVersion))
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("gateway auth filter error", e);
                    return unauthorized(exchange, "未登录或token无效");
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path, List<String> whitelistPaths) {
        if (whitelistPaths == null || whitelistPaths.isEmpty()) {
            return false;
        }
        for (String pattern : whitelistPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeJson(exchange, HttpStatus.UNAUTHORIZED, 401, message);
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, escapeJson(message));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
