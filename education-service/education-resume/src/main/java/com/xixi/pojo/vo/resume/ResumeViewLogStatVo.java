package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 简历浏览统计视图对象。
 */
@Data
public class ResumeViewLogStatVo {
    /**
     * 总浏览次数。
     */
    private Long totalViewCount;

    /**
     * 最近浏览时间。
     */
    private LocalDateTime lastViewTime;

    /**
     * 浏览者类型分布。
     */
    private Map<String, Long> viewerTypeDistribution;
}
