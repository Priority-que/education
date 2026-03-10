package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AdminBizConstants;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.admin.AdminUserRoleUpdateDto;
import com.xixi.pojo.dto.admin.AdminUserStatusUpdateDto;
import com.xixi.pojo.dto.admin.AuditHandleDto;
import com.xixi.pojo.query.admin.AdminUserPageQuery;
import com.xixi.pojo.vo.admin.AdminUserDetailVo;
import com.xixi.pojo.vo.admin.AdminUserPageVo;
import com.xixi.service.AdminAuditService;
import com.xixi.service.AdminUserService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户编排接口。
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminUserController {
    private final AdminUserService adminUserService;
    private final AdminAuditService adminAuditService;

    @MethodPurpose("分页查询用户")
    @GetMapping("/page")
    public Result getUserPage(AdminUserPageQuery query) {
        IPage<AdminUserPageVo> page = adminUserService.getUserPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询用户详情")
    @GetMapping("/detail/{userId}")
    public Result getUserDetail(@PathVariable Long userId) {
        AdminUserDetailVo detailVo = adminUserService.getUserDetail(userId);
        return Result.success(detailVo);
    }

    @MethodPurpose("调整用户角色")
    @PutMapping("/role/{userId}")
    public Result updateUserRole(
            @PathVariable Long userId,
            @RequestBody AdminUserRoleUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminUserService.updateUserRole(userId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("调整用户状态（封禁或解封）")
    @PutMapping("/status/{userId}")
    public Result updateUserStatus(
            @PathVariable Long userId,
            @RequestBody AdminUserStatusUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminUserService.updateUserStatus(userId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("企业资质审核入口")
    @PostMapping("/enterprise/audit/{auditId}")
    public Result enterpriseAudit(
            @PathVariable Long auditId,
            @RequestBody AuditHandleDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.handleAuditByScene(AdminBizConstants.AUDIT_TYPE_ENTERPRISE, auditId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("教师资质审核入口")
    @PostMapping("/teacher/audit/{auditId}")
    public Result teacherAudit(
            @PathVariable Long auditId,
            @RequestBody AuditHandleDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.handleAuditByScene(AdminBizConstants.AUDIT_TYPE_TEACHER, auditId, dto, parseUserId(userIdHeader));
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
