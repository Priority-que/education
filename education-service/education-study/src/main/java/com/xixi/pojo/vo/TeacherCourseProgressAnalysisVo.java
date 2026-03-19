package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师端课程学习进度分析结果。
 */
@Data
public class TeacherCourseProgressAnalysisVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 平均学习进度（百分比）。
     */
    private BigDecimal averageProgress;

    /**
     * 课程总学生数。
     */
    private Integer totalStudents;

    /**
     * 各进度段学生分布。
     */
    private List<ProgressDistributionItem> progressDistribution;

    /**
     * 学习进度趋势（图表数据）。
     */
    private List<ProgressTrendItem> progressTrend;

    /**
     * 进度段分布项。
     */
    @Data
    public static class ProgressDistributionItem {
        private String rangeLabel;
        private Integer studentCount;
    }

    /**
     * 进度趋势项。
     */
    @Data
    public static class ProgressTrendItem {
        private String date;
        private BigDecimal averageProgress;
        private Integer activeStudents;
    }
}
