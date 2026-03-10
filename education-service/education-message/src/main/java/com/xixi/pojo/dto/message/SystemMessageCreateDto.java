package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员创建系统消息请求参数。
 */
@Data
public class SystemMessageCreateDto {
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
     * ALL/ROLE/COURSE/CLASS/USER_PICKED（兼容USER）
     */
    private String targetType;
    /**
     * 新版发送规则对象（教师公告改造）。
     */
    private Map<String, Object> targetSpec;
    /**
     * ROLE时可传角色编码数组，USER时可传用户ID数组。
     */
    private List<Object> targetValue;
    private LocalDateTime expiryTime;
}
