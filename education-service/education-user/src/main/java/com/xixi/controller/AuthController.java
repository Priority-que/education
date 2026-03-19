package com.xixi.controller;

import com.xixi.pojo.dto.auth.LoginRequestDto;
import com.xixi.pojo.dto.auth.LogoutRequestDto;
import com.xixi.pojo.dto.auth.RefreshTokenRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthLoginRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthSendRequestDto;
import com.xixi.pojo.vo.auth.AuthUserVo;
import com.xixi.pojo.vo.auth.LoginResponseVo;
import com.xixi.pojo.vo.auth.SmsAuthSendResponseVo;
import com.xixi.service.AuthService;
import com.xixi.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequestDto request, HttpServletRequest servletRequest) {
        LoginResponseVo response = authService.login(request, getClientIp(servletRequest), getUserAgent(servletRequest));
        return Result.success("login success", response);
    }

    @PostMapping("/sms-auth/send")
    public Result sendSmsAuthCode(@RequestBody SmsAuthSendRequestDto request) {
        SmsAuthSendResponseVo response = authService.sendSmsAuthCode(request);
        return Result.success("sms auth code sent", response);
    }

    @PostMapping("/sms-auth/login")
    public Result smsAuthLogin(@RequestBody SmsAuthLoginRequestDto request, HttpServletRequest servletRequest) {
        LoginResponseVo response = authService.smsAuthLogin(request, getClientIp(servletRequest), getUserAgent(servletRequest));
        return Result.success("login success", response);
    }

    @PostMapping("/refresh")
    public Result refresh(@RequestBody RefreshTokenRequestDto request, HttpServletRequest servletRequest) {
        LoginResponseVo response = authService.refresh(request, getClientIp(servletRequest), getUserAgent(servletRequest));
        return Result.success("refresh success", response);
    }

    @PostMapping("/logout")
    public Result logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutRequestDto request
    ) {
        authService.logout(authorization, request);
        return Result.success("logout success");
    }

    @GetMapping("/me")
    public Result me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthUserVo userVo = authService.me(authorization);
        return Result.success(userVo);
    }

    @PostMapping("/admin/kick/{userId}")
    public Result kick(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long userId
    ) {
        long tokenVersion = authService.kick(authorization, userId);
        return Result.success("kick success, new tokenVersion: " + tokenVersion);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            int index = xForwardedFor.indexOf(',');
            return index > 0 ? xForwardedFor.substring(0, index).trim() : xForwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? "" : userAgent;
    }
}
