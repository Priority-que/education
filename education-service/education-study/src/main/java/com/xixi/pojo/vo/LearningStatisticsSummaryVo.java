package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学习数据汇总
 */
@Data
public class LearningStatisticsSummaryVo {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 总学习时长（小时）
     */
    private BigDecimal totalStudyHours;

    /**
     * 学习天数
     */
    private Integer studyDays;

    /**
     * 日均学习时长（小时）
     */
    private BigDecimal averageDailyStudyHours;

    /**
     * 当前连续学习天数
     */
    private Integer currentStreakDays;

    /**
     * 最长连续学习天数
     */
    private Integer maxStreakDays;

    /**
     * 学习连续性（0-100）
     */
    private BigDecimal consistencyRate;
}
