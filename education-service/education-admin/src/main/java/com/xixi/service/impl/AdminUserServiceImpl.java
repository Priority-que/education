package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.SystemConfig;
import com.xixi.exception.BizException;
import com.xixi.mapper.OperationLogMapper;
import com.xixi.mapper.SystemConfigMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.AdminUserRoleUpdateDto;
import com.xixi.pojo.dto.admin.AdminUserStatusUpdateDto;
import com.xixi.pojo.query.admin.AdminUserPageQuery;
import com.xixi.pojo.vo.admin.AdminUserDetailVo;
import com.xixi.pojo.vo.admin.AdminUserPageVo;
import com.xixi.service.AdminUserService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 用户编排服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private static final String CONFIG_GROUP_ADMIN_USER = "admin_user";
    private static final String CONFIG_TYPE_TEXT = "TEXT";
    private static final String PREFIX_ROLE = "admin.user.role.";
    private static final String PREFIX_STATUS = "admin.user.status.";

    private final OperationLogMapper operationLogMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;

    @Override
    @MethodPurpose("分页查询管理端用户")
    public IPage<AdminUserPageVo> getUserPage(AdminUserPageQuery query) {
        AdminUserPageQuery safeQuery = query == null ? new AdminUserPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        String roleFilter = normalizeRoleFilter(safeQuery.getUserRole());
        return operationLogMapper.selectAdminUserPage(
                new Page<>(pageNum, pageSize),
                trimToNull(safeQuery.getKeyword()),
                roleFilter,
                safeQuery.getStatus()
        );
    }

    @Override
    @MethodPurpose("查询管理端用户详情")
    public AdminUserDetailVo getUserDetail(Long userId) {
        if (userId == null) {
            throw new BizException(400, "userId不能为空");
        }
        AdminUserPageVo aggregate = operationLogMapper.selectUserAggregate(userId);
        if (aggregate == null) {
            throw new BizException(404, "用户不存在");
        }
        Map<String, String> overrideMap = getOverrideMap(List.of(userId));
        String overrideRole = overrideMap.get(PREFIX_ROLE + userId);
        String overrideStatus = overrideMap.get(PREFIX_STATUS + userId);

        AdminUserDetailVo detailVo = new AdminUserDetailVo();
        detailVo.setUserId(userId);
        detailVo.setUserName(aggregate.getUserName());
        detailVo.setUserRole(StringUtils.hasText(overrideRole) ? overrideRole : aggregate.getUserRole());
        detailVo.setOperationCount(aggregate.getOperationCount());
        detailVo.setLastOperationTime(aggregate.getLastOperationTime());
        int baseStatus = aggregate.getStatus() == null ? 1 : aggregate.getStatus();
        detailVo.setStatus(StringUtils.hasText(overrideStatus) ? ("0".equals(overrideStatus) ? 0 : 1) : baseStatus);
        detailVo.setRoleSource(StringUtils.hasText(overrideRole) ? "SYSTEM_CONFIG" : "USER_TABLE");
        detailVo.setStatusSource(StringUtils.hasText(overrideStatus) ? "SYSTEM_CONFIG" : "USER_TABLE");
        detailVo.setRecentLogs(operationLogMapper.selectRecentByUserId(userId, 10));
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("更新用户角色")
    public Result updateUserRole(Long userId, AdminUserRoleUpdateDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (userId == null) {
            throw new BizException(400, "userId不能为空");
        }
        if (dto == null || !StringUtils.hasText(dto.getRole())) {
            throw new BizException(400, "role不能为空");
        }
        String newRole = normalizeRoleFilter(dto.getRole());
        if (!List.of("ADMIN", "STUDENT", "TEACHER", "ENTERPRISE").contains(newRole)) {
            throw new BizException(400, "role取值非法");
        }

        saveOrUpdateUserOverride(PREFIX_ROLE + userId, newRole, "用户角色覆盖配置");

        adminDomainEventProducer.publish(
                "UPDATE",
                "USER_ROLE",
                userId,
                JSONUtil.toJsonStr(Map.of("role", newRole)),
                adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "USER_ROLE_UPDATE",
                "更新用户角色",
                "PUT",
                "/admin/user/role/" + userId,
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("userId", userId, "role", newRole)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("用户角色更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("更新用户状态（封禁/解封）")
    public Result updateUserStatus(Long userId, AdminUserStatusUpdateDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (userId == null) {
            throw new BizException(400, "userId不能为空");
        }
        if (dto == null || dto.getStatus() == null) {
            throw new BizException(400, "status不能为空");
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BizException(400, "status只能为0或1");
        }

        saveOrUpdateUserOverride(PREFIX_STATUS + userId, String.valueOf(dto.getStatus()), "用户状态覆盖配置");

        adminDomainEventProducer.publish(
                "UPDATE",
                "USER_STATUS",
                userId,
                JSONUtil.toJsonStr(Map.of("status", dto.getStatus())),
                adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "USER_STATUS_UPDATE",
                "更新用户状态",
                "PUT",
                "/admin/user/status/" + userId,
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("userId", userId, "status", dto.getStatus())),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("用户状态更新成功");
    }

    @MethodPurpose("批量读取用户覆盖配置")
    private Map<String, String> getOverrideMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Long userId : userIds) {
            keys.add(PREFIX_ROLE + userId);
            keys.add(PREFIX_STATUS + userId);
        }
        List<SystemConfig> configs = systemConfigMapper.selectByConfigKeys(new ArrayList<>(keys));
        Map<String, String> map = new HashMap<>();
        for (SystemConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    @MethodPurpose("保存或更新用户覆盖配置")
    private void saveOrUpdateUserOverride(String configKey, String value, String configName) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            SystemConfig insert = new SystemConfig();
            insert.setConfigKey(configKey);
            insert.setConfigValue(value);
            insert.setConfigName(configName);
            insert.setConfigGroup(CONFIG_GROUP_ADMIN_USER);
            insert.setDescription("管理端用户编排覆盖配置");
            insert.setConfigType(CONFIG_TYPE_TEXT);
            insert.setSortOrder(0);
            insert.setIsSystem(false);
            insert.setCreatedTime(LocalDateTime.now());
            insert.setUpdatedTime(LocalDateTime.now());
            systemConfigMapper.insert(insert);
            return;
        }
        config.setConfigValue(value);
        config.setUpdatedTime(LocalDateTime.now());
        systemConfigMapper.updateById(config);
    }

    @MethodPurpose("字符串标准化为大写")
    private String normalizeUpper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    @MethodPurpose("角色筛选值归一化")
    private String normalizeRoleFilter(String value) {
        String normalized = normalizeUpper(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return switch (normalized) {
            case "1", "ADMIN", "ROLE_ADMIN" -> "ADMIN";
            case "2", "STUDENT", "ROLE_STUDENT" -> "STUDENT";
            case "3", "TEACHER", "ROLE_TEACHER" -> "TEACHER";
            case "4", "ENTERPRISE", "ROLE_ENTERPRISE", "COMPANY" -> "ENTERPRISE";
            default -> normalized;
        };
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
