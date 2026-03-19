package com.xixi.openfeign.message.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内部模板事件触发请求。
 */
@Data
public class TemplateTriggerEventRequest {
    private String eventId;
    private String eventCode;
    private String targetType;
    private List<Object> targetValue;
    private Map<String, Object> params;
    private String messageType;
    private Integer priority;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime expiryTime;
    private String deliverMode;
    private Long operatorId;
    private Integer operatorRole;
}

