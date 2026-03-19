package com.xixi.pojo.query.message;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 管理员系统消息分页查询参数。
 */
@Data
public class SystemMessageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 20L;
    private String messageType;
    private String status;
    private Integer priority;
    private String targetType;
    private String keyword;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
