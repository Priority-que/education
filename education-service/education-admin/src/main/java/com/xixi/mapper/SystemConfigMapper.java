package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置 Mapper。
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 按分组查询配置列表。
     */
    List<SystemConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 按配置键查询配置。
     */
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 按配置键批量查询配置。
     */
    List<SystemConfig> selectByConfigKeys(@Param("configKeys") List<String> configKeys);
}
