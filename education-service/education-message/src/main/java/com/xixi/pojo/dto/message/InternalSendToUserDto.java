package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内部投递：发送给单个用户请求参数。
 */
@Data
public class InternalSendToUserDto {
    private Long userId;
    /**
     * SYNC/MQ，默认SYNC。
     */
    private String deliverMode;
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
}

