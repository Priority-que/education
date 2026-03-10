package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 新增系统配置请求参数。
 */
@Data
public class SystemConfigCreateDto {
    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String description;
    private String configType;
    private String options;
    private Integer sortOrder;
    private Boolean isSystem;
}
