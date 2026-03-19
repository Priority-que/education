package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员修改系统消息请求参数。
 */
@Data
public class SystemMessageUpdateDto {
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
    /**
     * ROLE时可传角色编码数组，USER时可传用户ID数组。
     */
    private List<Object> targetValue;
    private LocalDateTime expiryTime;
}
