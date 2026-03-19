package com.xixi.pojo.vo.auth;

import lombok.Data;

@Data
public class LoginResponseVo {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long accessExpiresIn;
    private Long refreshExpiresIn;
    private AuthUserVo user;
}

