package com.xixi.pojo.dto.auth;

import lombok.Data;

@Data
public class SmsAuthLoginRequestDto {
    private String phone;
    private String smsCode;
    private String deviceId;
}
