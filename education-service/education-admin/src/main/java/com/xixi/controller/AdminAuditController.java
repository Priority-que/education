package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.AuditRecord;
import com.xixi.pojo.dto.admin.AuditApproveDto;
import com.xixi.pojo.dto.admin.AuditBatchHandleDto;
import com.xixi.pojo.dto.admin.AuditRejectDto;
import com.xixi.pojo.query.admin.AuditPageQuery;
import com.xixi.pojo.vo.admin.AuditStatVo;
import com.xixi.service.AdminAuditService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审核中心接口。
 */
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminAuditController {
    private final AdminAuditService adminAuditService;

    @MethodPurpose("分页查询审核单")
    @GetMapping("/page")
    public Result getAuditPage(AuditPageQuery query) {
        IPage<AuditRecord> page = adminAuditService.getAuditPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询审核单详情")
    @GetMapping("/detail/{auditId}")
    public Result getAuditDetail(@PathVariable Long auditId) {
        return Result.success(adminAuditService.getAuditDetail(auditId));
    }

    @MethodPurpose("审核通过")
    @PostMapping("/approve/{auditId}")
    public Result approveAudit(
            @PathVariable Long auditId,
            @RequestBody(required = false) AuditApproveDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.approveAudit(auditId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("审核拒绝")
    @PostMapping("/reject/{auditId}")
    public Result rejectAudit(
            @PathVariable Long auditId,
            @RequestBody AuditRejectDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.rejectAudit(auditId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("批量审核处理")
    @PostMapping("/batch")
    public Result batchAudit(
            @RequestBody AuditBatchHandleDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminAuditService.batchHandleAudit(dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("查询审核统计")
    @GetMapping("/stat")
    public Result getAuditStat(@RequestParam(required = false) String auditType) {
        List<AuditStatVo> statList = adminAuditService.getAuditStat(auditType);
        return Result.success(statList);
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
