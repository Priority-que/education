package com.xixi.service;

public interface SmsAuthService {
    String sendVerifyCode(String phone);

    void checkVerifyCode(String phone, String smsCode);
}
