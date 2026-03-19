package com.xixi.pojo.query.message;

import lombok.Data;

/**
 * 管理员模板分页查询参数。
 */
@Data
public class MessageTemplateQuery {
    private Long pageNum = 1L;
    private Long pageSize = 20L;
    private String templateCode;
    private String templateType;
    /**
     * 0-禁用, 1-启用
     */
    private Integer status;
    /**
     * 模板名/描述关键词
     */
    private String keyword;
}
