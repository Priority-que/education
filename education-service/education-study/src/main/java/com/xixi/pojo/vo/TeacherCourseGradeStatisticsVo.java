package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 教师端课程成绩统计 VO
 */
@Data
public class TeacherCourseGradeStatisticsVo {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 总人数
     */
    private Integer totalStudents;

    /**
     * 及格人数
     */
    private Integer passCount;

    /**
     * 平均分
     */
    private BigDecimal averageScore;

    /**
     * 最高分
     */
    private BigDecimal maxScore;

    /**
     * 最低分
     */
    private BigDecimal minScore;

    /**
     * 及格率（百分比）
     */
    private BigDecimal passRate;

    /**
     * 成绩等级分布（A/B/C/D/F）
     */
    private Map<String, Integer> gradeDistribution;

    /**
     * 分数段分布（90-100/80-89/70-79/60-69/0-59）
     */
    private Map<String, Integer> scoreRangeDistribution;
}

