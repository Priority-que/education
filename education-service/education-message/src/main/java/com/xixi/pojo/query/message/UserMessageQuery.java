package com.xixi.pojo.query.message;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 我的消息分页查询参数。
 */
@Data
public class UserMessageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 20L;
    private String messageType;
    /**
     * 0-未读，1-已读
     */
    private Integer isRead;
    /**
     * 0-普通，1-重要，2-紧急
     */
    private Integer priority;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String keyword;
}

