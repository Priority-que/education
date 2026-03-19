package com.xixi.pojo.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String username;
    private String password;
    private String deviceId;
}

