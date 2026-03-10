package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UsersVo {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private Integer role;
    private Boolean status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

