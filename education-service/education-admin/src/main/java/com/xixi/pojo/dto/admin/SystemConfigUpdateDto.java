package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 更新系统配置请求参数。
 */
@Data
public class SystemConfigUpdateDto {
    private Long id;
    private String configValue;
    private String configName;
    private String description;
    private String configType;
    private String options;
    private Integer sortOrder;
}
