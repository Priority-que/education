package com.xixi.pojo.vo.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthUserVo {
    private Long id;
    private String username;
    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime lastLoginTime;
}

