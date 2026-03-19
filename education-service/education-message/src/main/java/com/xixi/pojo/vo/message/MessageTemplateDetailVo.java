package com.xixi.pojo.vo.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员模板详情/分页返回对象。
 */
@Data
public class MessageTemplateDetailVo {
    private Long id;
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
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
