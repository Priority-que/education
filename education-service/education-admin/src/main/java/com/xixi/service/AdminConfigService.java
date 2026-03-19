package com.xixi.service;

import com.xixi.entity.SystemConfig;
import com.xixi.pojo.dto.admin.SystemConfigBatchUpdateDto;
import com.xixi.pojo.dto.admin.SystemConfigCreateDto;
import com.xixi.pojo.dto.admin.SystemConfigUpdateDto;
import com.xixi.web.Result;

import java.util.List;

/**
 * 系统配置服务接口。
 */
public interface AdminConfigService {
    List<SystemConfig> getConfigByGroup(String group);

    SystemConfig getConfigDetail(Long id);

    Result createConfig(SystemConfigCreateDto dto, Long adminId);

    Result updateConfig(SystemConfigUpdateDto dto, Long adminId);

    Result batchUpdateConfig(SystemConfigBatchUpdateDto dto, Long adminId);

    Result reloadConfig(Long adminId);
}
