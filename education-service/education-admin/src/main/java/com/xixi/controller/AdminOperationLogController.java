package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.OperationLog;
import com.xixi.pojo.query.admin.OperationLogPageQuery;
import com.xixi.service.AdminOperationLogService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志接口。
 */
@RestController
@RequestMapping("/admin/log/operation")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminOperationLogController {
    private final AdminOperationLogService adminOperationLogService;

    @MethodPurpose("分页查询操作日志")
    @GetMapping("/page")
    public Result getOperationLogPage(OperationLogPageQuery query) {
        IPage<OperationLog> page = adminOperationLogService.getOperationLogPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询操作日志详情")
    @GetMapping("/detail/{id}")
    public Result getOperationLogDetail(@PathVariable Long id) {
        return Result.success(adminOperationLogService.getOperationLogDetail(id));
    }

    @MethodPurpose("导出操作日志")
    @GetMapping("/export")
    public Result exportOperationLog(OperationLogPageQuery query) {
        List<OperationLog> list = adminOperationLogService.exportOperationLog(query);
        return Result.success(list);
    }

    @MethodPurpose("清理历史操作日志")
    @DeleteMapping("/cleanup")
    public Result cleanupOperationLog(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminOperationLogService.cleanupOperationLog(beforeTime, parseUserId(userIdHeader));
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
