package com.xixi.pojo.query.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 审核记录分页查询参数。
 */
@Data
public class AuditPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String auditType;
    private String auditStatus;
    private String targetName;
    private String applicantName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
