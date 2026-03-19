package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模板触发事件参数。
 */
@Data
public class TemplateTriggerEventDto {
    /**
     * 事件唯一ID，用于幂等。
     */
    private String eventId;
    /**
     * 事件编码，如 HOMEWORK_GRADED。
     */
    private String eventCode;
    /**
     * ALL/ROLE/USER。
     */
    private String targetType;
    /**
     * ROLE时传角色数组，USER时传用户ID数组，ALL可为空。
     */
    private List<Object> targetValue;
    /**
     * 模板变量参数。
     */
    private Map<String, Object> params;
    /**
     * 可选消息类型覆盖。
     */
    private String messageType;
    /**
     * 可选优先级覆盖，0/1/2。
     */
    private Integer priority;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime expiryTime;
    /**
     * 可选投递模式覆盖：SYNC/MQ。
     */
    private String deliverMode;
    private Long operatorId;
    private Integer operatorRole;
}

