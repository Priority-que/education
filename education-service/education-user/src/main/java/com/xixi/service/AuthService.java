package com.xixi.service;

import com.xixi.pojo.dto.auth.LoginRequestDto;
import com.xixi.pojo.dto.auth.LogoutRequestDto;
import com.xixi.pojo.dto.auth.RefreshTokenRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthLoginRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthSendRequestDto;
import com.xixi.pojo.vo.auth.AuthUserVo;
import com.xixi.pojo.vo.auth.LoginResponseVo;
import com.xixi.pojo.vo.auth.SmsAuthSendResponseVo;

public interface AuthService {
    LoginResponseVo login(LoginRequestDto request, String ip, String userAgent);

    SmsAuthSendResponseVo sendSmsAuthCode(SmsAuthSendRequestDto request);

    LoginResponseVo smsAuthLogin(SmsAuthLoginRequestDto request, String ip, String userAgent);

    LoginResponseVo refresh(RefreshTokenRequestDto request, String ip, String userAgent);

    void logout(String authorization, LogoutRequestDto request);

    AuthUserVo me(String authorization);

    long kick(String authorization, Long userId);
}
