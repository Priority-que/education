package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历浏览记录请求参数。
 */
@Data
public class ResumeViewLogRecordDto {
    /**
     * 简历ID。
     */
    private Long resumeId;

    /**
     * 浏览者ID。
     */
    private Long viewerId;

    /**
     * 浏览者类型：ENTERPRISE/ADMIN/STUDENT/SYSTEM。
     */
    private String viewerType;

    /**
     * 浏览时间，可不传，默认当前时间。
     */
    private LocalDateTime viewTime;

    /**
     * IP地址，可不传。
     */
    private String ipAddress;

    /**
     * User-Agent，可不传。
     */
    private String userAgent;
}
