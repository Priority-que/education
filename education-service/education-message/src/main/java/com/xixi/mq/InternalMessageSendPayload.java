package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内部消息投递命令载荷。
 */
@Data
public class InternalMessageSendPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ALL/ROLE/USER
     */
    private String targetType;
    private List<Object> targetValue;
    /**
     * SYSTEM/COURSE/CERTIFICATE/JOB/OTHER
     */
    private String messageType;
    private String messageTitle;
    private String messageContent;
    private Long relatedId;
    private String relatedType;
    /**
     * 0-普通，1-重要，2-紧急
     */
    private Integer priority;
    private LocalDateTime expiryTime;

    private String templateCode;
    private Map<String, Object> params;

    private Long operatorId;
    private Integer operatorRole;
}

