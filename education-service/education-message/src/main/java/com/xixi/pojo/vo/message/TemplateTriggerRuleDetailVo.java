package com.xixi.pojo.vo.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板触发规则详情/分页返回对象。
 */
@Data
public class TemplateTriggerRuleDetailVo {
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
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

