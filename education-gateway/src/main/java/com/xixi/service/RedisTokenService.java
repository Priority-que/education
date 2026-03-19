package com.xixi.service;

import reactor.core.publisher.Mono;

public interface RedisTokenService {
    Mono<Boolean> isAccessTokenBlacklisted(String jti);

    Mono<Integer> getTokenVersion(Long userId);
}

