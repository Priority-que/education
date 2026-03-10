package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历浏览记录视图对象。
 */
@Data
public class ResumeViewLogVo {
    /**
     * 浏览记录ID。
     */
    private Long id;

    /**
     * 简历ID。
     */
    private Long resumeId;

    /**
     * 浏览者ID。
     */
    private Long viewerId;

    /**
     * 浏览者类型。
     */
    private String viewerType;

    /**
     * 浏览时间。
     */
    private LocalDateTime viewTime;

    /**
     * IP地址。
     */
    private String ipAddress;

    /**
     * User-Agent。
     */
    private String userAgent;
}
