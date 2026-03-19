package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师端作业完成情况分析结果。
 */
@Data
public class HomeworkSubmissionAnalysisVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 作业平均得分。
     */
    private BigDecimal averageScore;

    /**
     * 迟交率（百分比）。
     */
    private BigDecimal lateRate;

    /**
     * 各作业完成率明细。
     */
    private List<HomeworkCompletionItem> homeworkCompletionList;

    /**
     * 提交时间分布（按小时）。
     */
    private List<SubmissionTimeDistributionItem> submissionTimeDistribution;

    /**
     * 作业完成率项。
     */
    @Data
    public static class HomeworkCompletionItem {
        private Long homeworkId;
        private String homeworkTitle;
        private Integer totalStudents;
        private Integer submittedCount;
        private BigDecimal completionRate;
        private BigDecimal averageScore;
    }

    /**
     * 提交时间分布项。
     */
    @Data
    public static class SubmissionTimeDistributionItem {
        private Integer hour;
        private String timeRange;
        private Integer submissionCount;
    }
}
