package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.SystemConfig;
import com.xixi.pojo.dto.admin.SystemConfigBatchUpdateDto;
import com.xixi.pojo.dto.admin.SystemConfigCreateDto;
import com.xixi.pojo.dto.admin.SystemConfigUpdateDto;
import com.xixi.service.AdminConfigService;
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

import java.util.List;

/**
 * 系统配置接口。
 */
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminConfigController {
    private final AdminConfigService adminConfigService;

    @MethodPurpose("按分组查询配置")
    @GetMapping("/group/{group}")
    public Result getConfigByGroup(@PathVariable String group) {
        List<SystemConfig> list = adminConfigService.getConfigByGroup(group);
        return Result.success(list);
    }

    @MethodPurpose("查询配置详情")
    @GetMapping("/detail/{id}")
    public Result getConfigDetail(@PathVariable Long id) {
        return Result.success(adminConfigService.getConfigDetail(id));
    }

    @MethodPurpose("新增配置")
    @PostMapping("/create")
    public Result createConfig(
            @RequestBody SystemConfigCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminConfigService.createConfig(dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("更新配置")
    @PutMapping("/update")
    public Result updateConfig(
            @RequestBody SystemConfigUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminConfigService.updateConfig(dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("批量更新配置")
    @PutMapping("/batch")
    public Result batchUpdateConfig(
            @RequestBody SystemConfigBatchUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminConfigService.batchUpdateConfig(dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("触发配置热加载")
    @PostMapping("/reload")
    public Result reloadConfig(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminConfigService.reloadConfig(parseUserId(userIdHeader));
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
