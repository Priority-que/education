package com.xixi.pojo.vo.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员系统消息详情/分页返回对象。
 */
@Data
public class SystemMessageDetailVo {
    private Long id;
    private String messageType;
    private String messageTitle;
    private String messageContent;
    private Long senderId;
    private String senderName;
    /**
     * 0-普通, 1-重要, 2-紧急
     */
    private Integer priority;
    /**
     * ALL/ROLE/USER
     */
    private String targetType;
    private List<Object> targetValue;
    private LocalDateTime expiryTime;
    /**
     * DRAFT/PUBLISHED/WITHDRAWN
     */
    private String status;
    private LocalDateTime publishTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
