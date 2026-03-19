package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.SystemConfig;
import com.xixi.exception.BizException;
import com.xixi.mapper.SystemConfigMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.SystemConfigBatchUpdateDto;
import com.xixi.pojo.dto.admin.SystemConfigCreateDto;
import com.xixi.pojo.dto.admin.SystemConfigUpdateDto;
import com.xixi.service.AdminConfigService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminConfigServiceImpl implements AdminConfigService {
    private static final String BIZ_TYPE_CONFIG = "CONFIG";

    private final SystemConfigMapper systemConfigMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;

    @Override
    @MethodPurpose("按分组查询配置")
    public List<SystemConfig> getConfigByGroup(String group) {
        if (!StringUtils.hasText(group)) {
            throw new BizException(400, "group不能为空");
        }
        return systemConfigMapper.selectByGroup(group.trim());
    }

    @Override
    @MethodPurpose("查询配置详情")
    public SystemConfig getConfigDetail(Long id) {
        if (id == null) {
            throw new BizException(400, "id不能为空");
        }
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new BizException(404, "配置不存在");
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("新增系统配置")
    public Result createConfig(SystemConfigCreateDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        validateCreateDto(dto);
        SystemConfig exists = systemConfigMapper.selectByConfigKey(dto.getConfigKey().trim());
        if (exists != null) {
            throw new BizException(409, "配置键已存在");
        }
        SystemConfig config = new SystemConfig();
        config.setConfigKey(dto.getConfigKey().trim());
        config.setConfigValue(trimToNull(dto.getConfigValue()));
        config.setConfigName(dto.getConfigName().trim());
        config.setConfigGroup(dto.getConfigGroup().trim());
        config.setDescription(trimToNull(dto.getDescription()));
        config.setConfigType(normalizeConfigType(dto.getConfigType()));
        config.setOptions(trimToNull(dto.getOptions()));
        config.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        config.setIsSystem(dto.getIsSystem() != null && dto.getIsSystem());
        config.setCreatedTime(LocalDateTime.now());
        config.setUpdatedTime(LocalDateTime.now());
        systemConfigMapper.insert(config);

        publishAndLog(adminId, "CREATE", config.getId(), dto);
        return Result.success("配置创建成功", Map.of("id", config.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("更新系统配置")
    public Result updateConfig(SystemConfigUpdateDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "id不能为空");
        }
        SystemConfig config = systemConfigMapper.selectById(dto.getId());
        if (config == null) {
            throw new BizException(404, "配置不存在");
        }
        if (Boolean.TRUE.equals(config.getIsSystem()) && !StringUtils.hasText(dto.getConfigValue())) {
            throw new BizException(400, "系统配置至少需要更新configValue");
        }
        if (StringUtils.hasText(dto.getConfigValue())) {
            config.setConfigValue(dto.getConfigValue().trim());
        }
        if (StringUtils.hasText(dto.getConfigName())) {
            config.setConfigName(dto.getConfigName().trim());
        }
        if (dto.getDescription() != null) {
            config.setDescription(trimToNull(dto.getDescription()));
        }
        if (StringUtils.hasText(dto.getConfigType())) {
            config.setConfigType(normalizeConfigType(dto.getConfigType()));
        }
        if (dto.getOptions() != null) {
            config.setOptions(trimToNull(dto.getOptions()));
        }
        if (dto.getSortOrder() != null) {
            config.setSortOrder(dto.getSortOrder());
        }
        config.setUpdatedTime(LocalDateTime.now());
        systemConfigMapper.updateById(config);

        publishAndLog(adminId, "UPDATE", config.getId(), dto);
        return Result.success("配置更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("批量更新系统配置")
    public Result batchUpdateConfig(SystemConfigBatchUpdateDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BizException(400, "items不能为空");
        }
        if (dto.getItems().size() > 200) {
            throw new BizException(400, "单次批量更新最多200条");
        }
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedItems = new ArrayList<>();
        for (SystemConfigUpdateDto item : dto.getItems()) {
            try {
                updateConfig(item, adminId);
                successIds.add(item.getId());
            } catch (BizException e) {
                failedItems.add(Map.of(
                        "id", item == null ? null : item.getId(),
                        "code", e.getCode(),
                        "message", e.getMessage()
                ));
            }
        }
        adminDomainEventProducer.publish(
            "BATCH_UPDATE",
            BIZ_TYPE_CONFIG,
            null,
            JSONUtil.toJsonStr(Map.of(
                    "successCount", successIds.size(),
                    "failedCount", failedItems.size()
            )),
            adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "CONFIG_BATCH_UPDATE",
                "批量更新系统配置",
                "PUT",
                "/admin/config/batch",
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("successCount", successIds.size(), "failedCount", failedItems.size())),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("批量更新完成", Map.of(
                "successCount", successIds.size(),
                "failedCount", failedItems.size(),
                "successIds", successIds,
                "failedItems", failedItems
        ));
    }

    @Override
    @MethodPurpose("触发配置热加载通知")
    public Result reloadConfig(Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        adminDomainEventProducer.publish("RELOAD", BIZ_TYPE_CONFIG, null, null, adminId);
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "CONFIG_RELOAD",
                "触发配置热加载",
                "POST",
                "/admin/config/reload",
                null,
                "{\"message\":\"reload trigger\"}",
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("配置热加载事件已发送");
    }

    @MethodPurpose("发布配置事件并记录操作日志")
    private void publishAndLog(Long adminId, String eventType, Long configId, Object request) {
        adminDomainEventProducer.publish(eventType, BIZ_TYPE_CONFIG, configId, JSONUtil.toJsonStr(request), adminId);
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "CONFIG_" + eventType,
                "配置变更",
                "PUT",
                "/admin/config",
                JSONUtil.toJsonStr(request),
                JSONUtil.toJsonStr(Map.of("configId", configId, "eventType", eventType)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
    }

    @MethodPurpose("校验新增配置参数")
    private void validateCreateDto(SystemConfigCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getConfigKey())) {
            throw new BizException(400, "configKey不能为空");
        }
        if (!StringUtils.hasText(dto.getConfigName())) {
            throw new BizException(400, "configName不能为空");
        }
        if (!StringUtils.hasText(dto.getConfigGroup())) {
            throw new BizException(400, "configGroup不能为空");
        }
    }

    @MethodPurpose("标准化配置类型")
    private String normalizeConfigType(String configType) {
        if (!StringUtils.hasText(configType)) {
            return "TEXT";
        }
        String normalized = configType.trim().toUpperCase();
        if (!List.of("TEXT", "NUMBER", "BOOLEAN", "JSON", "SELECT").contains(normalized)) {
            throw new BizException(400, "configType非法");
        }
        return normalized;
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
