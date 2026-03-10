package com.xixi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.xixi.config.AliyunSmsAuthProperties;
import com.xixi.entity.Users;
import com.xixi.exception.BizException;
import com.xixi.mapper.UsersMapper;
import com.xixi.pojo.dto.auth.LoginRequestDto;
import com.xixi.pojo.dto.auth.LogoutRequestDto;
import com.xixi.pojo.dto.auth.RefreshTokenRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthLoginRequestDto;
import com.xixi.pojo.dto.auth.SmsAuthSendRequestDto;
import com.xixi.pojo.vo.auth.AuthUserVo;
import com.xixi.pojo.vo.auth.LoginResponseVo;
import com.xixi.pojo.vo.auth.SmsAuthSendResponseVo;
import com.xixi.security.LoginUserDetails;
import com.xixi.service.AuthService;
import com.xixi.service.RedisTokenService;
import com.xixi.service.SmsAuthService;
import com.xixi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_STUDENT = 2;
    private static final int STATUS_ENABLED = 1;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern SMS_CODE_PATTERN = Pattern.compile("^\\d{4,8}$");

    private final UsersMapper usersMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final SmsAuthService smsAuthService;
    private final PasswordEncoder passwordEncoder;
    private final AliyunSmsAuthProperties smsAuthProperties;

    @Override
    public LoginResponseVo login(LoginRequestDto request, String ip, String userAgent) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BizException(400, "username and password cannot be empty");
        }

        String username = request.getUsername().trim();
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BizException(401, "invalid username or password");
        } catch (DisabledException e) {
            throw new BizException(403, "account is disabled");
        }

        Users user = resolveLoginUser(authentication, username);
        String deviceId = normalizeDeviceId(request.getDeviceId());
        return issueLoginTokens(user, deviceId, ip, userAgent);
    }

    @Override
    public SmsAuthSendResponseVo sendSmsAuthCode(SmsAuthSendRequestDto request) {
        String phone = normalizePhone(request == null ? null : request.getPhone());
        String requestId = smsAuthService.sendVerifyCode(phone);
        SmsAuthSendResponseVo response = new SmsAuthSendResponseVo();
        response.setRequestId(requestId);
        return response;
    }

    @Override
    public LoginResponseVo smsAuthLogin(SmsAuthLoginRequestDto request, String ip, String userAgent) {
        if (request == null) {
            throw new BizException(400, "request cannot be null");
        }
        String phone = normalizePhone(request.getPhone());
        String smsCode = normalizeSmsCode(request.getSmsCode());

        smsAuthService.checkVerifyCode(phone, smsCode);

        Users user = usersMapper.selectByPhone(phone);
        if (user == null) {
            user = createDefaultUserByPhone(phone);
        }
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new BizException(403, "account is disabled");
        }

        String deviceId = normalizeDeviceId(request.getDeviceId());
        return issueLoginTokens(user, deviceId, ip, userAgent);
    }

    @Override
    public LoginResponseVo refresh(RefreshTokenRequestDto request, String ip, String userAgent) {
        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
            throw new BizException(400, "refreshToken cannot be empty");
        }

        Claims claims = jwtUtil.parseClaims(request.getRefreshToken());
        if (!JwtUtil.TYPE_REFRESH.equals(claims.get(JwtUtil.CLAIM_TYPE, String.class))) {
            throw new BizException(401, "invalid token type");
        }

        Long userId = toLong(claims.get(JwtUtil.CLAIM_UID));
        String tokenDeviceId = claims.get(JwtUtil.CLAIM_DEVICE, String.class);
        Integer tokenVersion = toInteger(claims.get(JwtUtil.CLAIM_VER));
        if (userId == null || !StringUtils.hasText(tokenDeviceId) || tokenVersion == null) {
            throw new BizException(401, "invalid refresh token");
        }

        String requestDeviceId = StringUtils.hasText(request.getDeviceId())
                ? request.getDeviceId().trim()
                : tokenDeviceId;
        if (!tokenDeviceId.equals(requestDeviceId)) {
            throw new BizException(401, "device mismatch");
        }

        int currentVersion = redisTokenService.getTokenVersion(userId);
        if (tokenVersion != currentVersion) {
            throw new BizException(401, "token expired, please login again");
        }

        if (!redisTokenService.verifyRefreshToken(userId, requestDeviceId, request.getRefreshToken())) {
            throw new BizException(401, "refresh token expired, please login again");
        }

        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "user not found");
        }
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new BizException(403, "account is disabled");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user, requestDeviceId, currentVersion);
        String newRefreshToken = jwtUtil.generateRefreshToken(user, requestDeviceId, currentVersion);

        redisTokenService.rotateRefreshToken(
                userId,
                requestDeviceId,
                request.getRefreshToken(),
                newRefreshToken,
                jwtUtil.getRefreshExpireSeconds()
        );
        redisTokenService.saveSessionMeta(userId, requestDeviceId, ip, userAgent, jwtUtil.getRefreshExpireSeconds());

        LoginResponseVo response = new LoginResponseVo();
        response.setTokenType("Bearer");
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setAccessExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setRefreshExpiresIn(jwtUtil.getRefreshExpireSeconds());
        return response;
    }

    @Override
    public void logout(String authorization, LogoutRequestDto request) {
        String accessToken = extractToken(authorization);
        Claims claims = jwtUtil.parseClaims(accessToken);
        if (!JwtUtil.TYPE_ACCESS.equals(claims.get(JwtUtil.CLAIM_TYPE, String.class))) {
            throw new BizException(401, "invalid token type");
        }

        Long userId = toLong(claims.get(JwtUtil.CLAIM_UID));
        String tokenDeviceId = claims.get(JwtUtil.CLAIM_DEVICE, String.class);
        String jti = claims.get(JwtUtil.CLAIM_JTI, String.class);
        Integer tokenVersion = toInteger(claims.get(JwtUtil.CLAIM_VER));
        if (userId == null || tokenVersion == null) {
            throw new BizException(401, "invalid token");
        }

        int currentVersion = redisTokenService.getTokenVersion(userId);
        if (tokenVersion != currentVersion) {
            throw new BizException(401, "token expired");
        }

        redisTokenService.blacklistAccessToken(jti, jwtUtil.getRemainingSeconds(accessToken));

        String deviceId = tokenDeviceId;
        if (request != null && StringUtils.hasText(request.getDeviceId())) {
            deviceId = request.getDeviceId().trim();
        }
        if (StringUtils.hasText(deviceId)) {
            redisTokenService.removeRefreshToken(userId, deviceId);
        }

        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            try {
                Claims refreshClaims = jwtUtil.parseClaims(request.getRefreshToken());
                Long refreshUid = toLong(refreshClaims.get(JwtUtil.CLAIM_UID));
                String refreshDid = refreshClaims.get(JwtUtil.CLAIM_DEVICE, String.class);
                if (userId.equals(refreshUid) && StringUtils.hasText(refreshDid)) {
                    redisTokenService.removeRefreshToken(userId, refreshDid);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public AuthUserVo me(String authorization) {
        String accessToken = extractToken(authorization);
        Claims claims = jwtUtil.parseClaims(accessToken);
        if (!JwtUtil.TYPE_ACCESS.equals(claims.get(JwtUtil.CLAIM_TYPE, String.class))) {
            throw new BizException(401, "invalid token type");
        }

        String jti = claims.get(JwtUtil.CLAIM_JTI, String.class);
        if (redisTokenService.isAccessTokenBlacklisted(jti)) {
            throw new BizException(401, "token expired");
        }

        Long userId = toLong(claims.get(JwtUtil.CLAIM_UID));
        Integer tokenVersion = toInteger(claims.get(JwtUtil.CLAIM_VER));
        if (userId == null || tokenVersion == null) {
            throw new BizException(401, "invalid token");
        }

        int currentVersion = redisTokenService.getTokenVersion(userId);
        if (tokenVersion != currentVersion) {
            throw new BizException(401, "token expired");
        }

        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "user not found");
        }
        return toAuthUserVo(user);
    }

    @Override
    public long kick(String authorization, Long userId) {
        String accessToken = extractToken(authorization);
        Claims claims = jwtUtil.parseClaims(accessToken);
        if (!JwtUtil.TYPE_ACCESS.equals(claims.get(JwtUtil.CLAIM_TYPE, String.class))) {
            throw new BizException(401, "invalid token type");
        }

        Integer role = toInteger(claims.get(JwtUtil.CLAIM_ROLE));
        if (role == null || role != ROLE_ADMIN) {
            throw new BizException(403, "forbidden");
        }
        if (userId == null) {
            throw new BizException(400, "userId cannot be null");
        }

        long newVersion = redisTokenService.incrTokenVersion(userId);
        redisTokenService.removeAllRefreshTokenForUser(userId);
        return newVersion;
    }

    private Users resolveLoginUser(Authentication authentication, String username) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserDetails loginUserDetails) {
            return loginUserDetails.getUser();
        }
        Users user = usersMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(401, "user not found");
        }
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new BizException(403, "account is disabled");
        }
        return user;
    }

    private LoginResponseVo issueLoginTokens(Users user, String deviceId, String ip, String userAgent) {
        int tokenVersion = redisTokenService.getTokenVersion(user.getId());
        String accessToken = jwtUtil.generateAccessToken(user, deviceId, tokenVersion);
        String refreshToken = jwtUtil.generateRefreshToken(user, deviceId, tokenVersion);

        redisTokenService.saveRefreshToken(user.getId(), deviceId, refreshToken, jwtUtil.getRefreshExpireSeconds());
        redisTokenService.saveSessionMeta(user.getId(), deviceId, ip, userAgent, jwtUtil.getRefreshExpireSeconds());
        usersMapper.updateLastLogin(user.getId(), ip, LocalDateTime.now());

        LoginResponseVo response = new LoginResponseVo();
        response.setTokenType("Bearer");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setAccessExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setRefreshExpiresIn(jwtUtil.getRefreshExpireSeconds());
        response.setUser(toAuthUserVo(user));
        return response;
    }

    private Users createDefaultUserByPhone(String phone) {
        Users user = new Users();
        user.setUsername(generateUniqueUsername(phone));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPhone(phone);
        user.setNickname(maskPhone(phone));
        user.setRole(resolveDefaultRole());
        user.setStatus(Boolean.TRUE);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        usersMapper.insert(user);
        return user;
    }

    private int resolveDefaultRole() {
        Integer configuredRole = smsAuthProperties.getDefaultRole();
        if (configuredRole == null || configuredRole <= 0) {
            return ROLE_STUDENT;
        }
        return configuredRole;
    }

    private String generateUniqueUsername(String phone) {
        String prefix = StringUtils.hasText(smsAuthProperties.getUsernamePrefix())
                ? smsAuthProperties.getUsernamePrefix().trim()
                : "u";
        String last4 = phone.substring(phone.length() - 4);
        for (int i = 0; i < 5; i++) {
            String candidate = prefix + last4 + RandomUtil.randomNumbers(6);
            if (usersMapper.selectByUsername(candidate) == null) {
                return candidate;
            }
        }
        return prefix + System.currentTimeMillis();
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private AuthUserVo toAuthUserVo(Users user) {
        AuthUserVo authUserVo = new AuthUserVo();
        authUserVo.setId(user.getId());
        authUserVo.setUsername(user.getUsername());
        authUserVo.setRealName(user.getRealName());
        authUserVo.setRole(user.getRole());
        authUserVo.setStatus(Boolean.TRUE.equals(user.getStatus()) ? STATUS_ENABLED : 0);
        authUserVo.setLastLoginTime(user.getLastLoginTime());
        return authUserVo;
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BizException(401, "unauthorized");
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private String normalizeDeviceId(String deviceId) {
        return StringUtils.hasText(deviceId) ? deviceId.trim() : "default-device";
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BizException(400, "phone cannot be empty");
        }
        String normalized = phone.trim().replace(" ", "");
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new BizException(400, "invalid phone number");
        }
        return normalized;
    }

    private String normalizeSmsCode(String smsCode) {
        if (!StringUtils.hasText(smsCode)) {
            throw new BizException(400, "smsCode cannot be empty");
        }
        String normalized = smsCode.trim();
        if (!SMS_CODE_PATTERN.matcher(normalized).matches()) {
            throw new BizException(400, "invalid smsCode");
        }
        return normalized;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
