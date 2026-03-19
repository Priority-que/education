package com.xixi.pojo.query.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 操作日志分页查询参数。
 */
@Data
public class OperationLogPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Long userId;
    private String userRole;
    private String operationType;
    private Integer status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
