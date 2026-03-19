package com.xixi.pojo.query.message;

import lombok.Data;

/**
 * 模板触发规则分页查询参数。
 */
@Data
public class TemplateTriggerRuleQuery {
    private Long pageNum = 1L;
    private Long pageSize = 20L;
    private String ruleCode;
    private String eventCode;
    private String templateCode;
    private String deliverMode;
    /**
     * 0-禁用，1-启用。
     */
    private Integer status;
    /**
     * 规则编码/事件编码/备注关键字。
     */
    private String keyword;
}

