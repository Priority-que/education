package com.xixi.pojo.dto.message;

import lombok.Data;

import java.util.List;

/**
 * 管理员创建模板请求参数。
 */
@Data
public class MessageTemplateCreateDto {
    private String templateCode;
    private String templateName;
    private String templateType;
    private String templateSubject;
    private String templateContent;
    private List<String> variables;
    private String description;
    /**
     * 0-禁用, 1-启用
     */
    private Integer status;
}
