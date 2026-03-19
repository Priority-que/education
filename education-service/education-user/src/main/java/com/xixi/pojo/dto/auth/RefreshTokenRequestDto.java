package com.xixi.pojo.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    private String refreshToken;
    private String deviceId;
}

