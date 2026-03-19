package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.TemplateTriggerEventDto;
import com.xixi.service.TemplateTriggerService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部模板事件触发接口。
 */
@RestController
@RequestMapping("/message/internal/trigger")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN, RoleConstants.TEACHER, RoleConstants.ENTERPRISE})
public class InternalTemplateTriggerController {
    private final TemplateTriggerService templateTriggerService;

    @MethodPurpose("内部触发：按事件编码自动触发模板发送")
    @PostMapping("/event")
    public Result triggerEvent(
            @RequestBody TemplateTriggerEventDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return templateTriggerService.triggerByEvent(request, parseUserId(userIdHeader), parseRole(roleHeader));
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

