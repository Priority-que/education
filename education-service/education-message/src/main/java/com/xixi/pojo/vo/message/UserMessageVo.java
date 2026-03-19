package com.xixi.pojo.vo.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的消息详情与列表项。
 */
@Data
public class UserMessageVo {
    private Long messageId;
    private String messageCategory;
    private String messageTitle;
    private String messageContent;
    /**
     * 0-未读，1-已读
     */
    private Integer isRead;
    private String readStatus;
    private LocalDateTime readTime;
    /**
     * 0-普通，1-重要，2-紧急
     */
    private Integer priority;
    private String priorityText;
    private LocalDateTime expiryTime;
    private LocalDateTime createdTime;
}
