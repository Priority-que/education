package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内部投递：按模板发送请求参数。
 */
@Data
public class InternalSendByTemplateDto {
    private String templateCode;
    /**
     * SYNC/MQ，默认SYNC。
     */
    private String deliverMode;
    /**
     * ALL/ROLE/USER
     */
    private String targetType;
    /**
     * ROLE时传角色数组；USER时传用户ID数组；ALL可为空。
     */
    private List<Object> targetValue;
    /**
     * SYSTEM/COURSE/CERTIFICATE/JOB/OTHER，为空时默认SYSTEM。
     */
    private String messageType;
    /**
     * 可覆盖模板标题，空则回退模板subject/templateName。
     */
    private String messageTitle;
    /**
     * 模板变量参数，格式：{ "name":"张三" }
     */
    private Map<String, Object> params;
    private Long relatedId;
    private String relatedType;
    /**
     * 0-普通，1-重要，2-紧急
     */
    private Integer priority;
    private LocalDateTime expiryTime;
}

