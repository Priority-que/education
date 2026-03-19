package com.xixi.service.impl;

import com.xixi.service.RedisTokenService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisTokenServiceImpl implements RedisTokenService {
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String TOKEN_VERSION_PREFIX = "auth:token:ver:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisTokenServiceImpl(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Boolean> isAccessTokenBlacklisted(String jti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Integer> getTokenVersion(Long userId) {
        return redisTemplate.opsForValue()
                .get(TOKEN_VERSION_PREFIX + userId)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .defaultIfEmpty(0);
    }
}
