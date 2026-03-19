package com.xixi.pojo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDto {
    private String username;
    private String realName;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Boolean gender;
    private LocalDate birthday;
    /**
     * 兼容简单改密：直接传入新密码。
     */
    private String password;
    /**
     * 可选：当前密码（传了就会校验）。
     */
    private String currentPassword;
    /**
     * 推荐：新密码。
     */
    private String newPassword;
    /**
     * 可选：确认新密码。
     */
    private String confirmPassword;
}
