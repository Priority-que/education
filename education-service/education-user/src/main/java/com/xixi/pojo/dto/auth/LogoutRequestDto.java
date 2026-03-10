package com.xixi.pojo.dto.auth;

import lombok.Data;

@Data
public class LogoutRequestDto {
    private String deviceId;
    private String refreshToken;
}

