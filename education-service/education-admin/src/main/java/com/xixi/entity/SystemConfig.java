package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_config")
public class SystemConfig {
    
    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 配置分组
     */
    private String configGroup;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 配置类型: TEXT-文本, NUMBER-数字, BOOLEAN-布尔, JSON-JSON, SELECT-下拉
     */
    private String configType;
    
    /**
     * 选项(用于SELECT类型)
     */
    private String options;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 是否系统配置: 0-否, 1-是
     */
    private Boolean isSystem;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

