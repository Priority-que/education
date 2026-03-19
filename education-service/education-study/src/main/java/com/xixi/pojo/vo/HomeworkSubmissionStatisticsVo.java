package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 作业提交统计结果
 */
@Data
public class HomeworkSubmissionStatisticsVo {

    /**
     * 作业ID
     */
    private Long homeworkId;

    /**
     * 作业标题
     */
    private String homeworkTitle;

    /**
     * 总提交数
     */
    private Integer totalSubmissions;

    /**
     * 已批改数
     */
    private Integer gradedCount;

    /**
     * 未批改数
     */
    private Integer ungradedCount;

    /**
     * 迟交数
     */
    private Integer lateCount;

    /**
     * 平均分
     */
    private BigDecimal averageScore;

    /**
     * 分数分布（按百分比分段）
     */
    private Map<String, Integer> scoreDistribution;
}
