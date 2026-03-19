package com.xixi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthJwtProperties {
    private String issuer = "education-platform";
    private String secret = "education-platform-please-change-this-secret-key-1234567890";
    private long accessExpireSeconds = 1800;
    private long refreshExpireSeconds = 604800;
}

