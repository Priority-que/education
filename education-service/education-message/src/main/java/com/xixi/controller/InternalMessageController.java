package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.InternalSendByTemplateDto;
import com.xixi.pojo.dto.message.InternalSendToRoleDto;
import com.xixi.pojo.dto.message.InternalSendToUserDto;
import com.xixi.pojo.dto.message.InternalSendToUsersDto;
import com.xixi.service.InternalMessageSendService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部消息投递接口（9.1~9.4）。
 */
@RestController
@RequestMapping("/message/internal/send")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN, RoleConstants.TEACHER, RoleConstants.ENTERPRISE})
public class InternalMessageController {
    private final InternalMessageSendService internalMessageSendService;

    @MethodPurpose("内部投递：发送给单个用户")
    @PostMapping("/user")
    public Result sendToUser(
            @RequestBody InternalSendToUserDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return internalMessageSendService.sendToUser(request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("内部投递：批量发送给用户列表")
    @PostMapping("/users")
    public Result sendToUsers(
            @RequestBody InternalSendToUsersDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return internalMessageSendService.sendToUsers(request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("内部投递：按角色发送")
    @PostMapping("/role")
    public Result sendToRole(
            @RequestBody InternalSendToRoleDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return internalMessageSendService.sendToRole(request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("内部投递：按模板发送")
    @PostMapping("/template")
    public Result sendByTemplate(
            @RequestBody InternalSendByTemplateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return internalMessageSendService.sendByTemplate(request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("解析请求头中的用户ID")
    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @MethodPurpose("解析请求头中的角色编码")
    private Integer parseRole(String roleHeader) {
        if (!StringUtils.hasText(roleHeader)) {
            return null;
        }
        try {
            return Integer.parseInt(roleHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
