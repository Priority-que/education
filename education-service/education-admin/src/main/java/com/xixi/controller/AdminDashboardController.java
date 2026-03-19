package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.admin.DashboardRebuildDto;
import com.xixi.pojo.query.admin.DashboardTrendQuery;
import com.xixi.service.AdminDashboardService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板接口。
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @MethodPurpose("查询看板总览")
    @GetMapping("/overview")
    public Result getOverview() {
        return Result.success(adminDashboardService.getOverview());
    }

    @MethodPurpose("查询看板趋势")
    @GetMapping("/trend")
    public Result getTrend(DashboardTrendQuery query) {
        return Result.success(adminDashboardService.getTrend(query));
    }

    @MethodPurpose("查询看板分布统计")
    @GetMapping("/distribution")
    public Result getDistribution() {
        return Result.success(adminDashboardService.getDistribution());
    }

    @MethodPurpose("重建统计数据")
    @PostMapping("/rebuild")
    public Result rebuild(
            @RequestBody DashboardRebuildDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminDashboardService.rebuildStatistics(dto, parseUserId(userIdHeader));
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
