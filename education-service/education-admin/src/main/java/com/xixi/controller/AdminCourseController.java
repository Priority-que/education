package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AdminBizConstants;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.admin.AuditHandleDto;
import com.xixi.service.AdminAuditService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程审核接口。
 */
@RestController
@RequestMapping("/admin/course")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminCourseController {
    private final AdminAuditService adminAuditService;

    @MethodPurpose("课程上架审核")
    @PostMapping("/audit/{auditId}")
    public Result auditCourse(
            @PathVariable Long auditId,
            @RequestBody AuditHandleDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.handleAuditByScene(AdminBizConstants.AUDIT_TYPE_COURSE, auditId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("课程举报处理")
    @PostMapping("/report/handle/{auditId}")
    public Result handleCourseReport(
            @PathVariable Long auditId,
            @RequestBody AuditHandleDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.handleAuditByScene(AdminBizConstants.AUDIT_TYPE_COURSE_REPORT, auditId, dto, parseUserId(userIdHeader));
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
}
