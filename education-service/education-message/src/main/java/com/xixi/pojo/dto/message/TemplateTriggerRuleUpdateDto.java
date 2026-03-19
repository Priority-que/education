package com.xixi.pojo.dto.message;

import lombok.Data;

/**
 * 模板触发规则更新参数。
 */
@Data
public class TemplateTriggerRuleUpdateDto {
    private Long id;
    private String ruleCode;
    private String eventCode;
    private String templateCode;
    private String messageType;
    private String deliverMode;
    private Integer priority;
    private String relatedType;
    /**
     * 0-禁用，1-启用。
     */
    private Integer status;
    private String remark;
}

