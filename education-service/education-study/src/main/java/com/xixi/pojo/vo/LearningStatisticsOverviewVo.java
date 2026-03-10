package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 学习统计概览VO
 */
@Data
public class LearningStatisticsOverviewVo {
    
    /**
     * 总学习时长（小时）
     */
    private BigDecimal totalStudyHours;
    
    /**
     * 已完成课程数
     */
    private Integer completedCourses;
    
    /**
     * 学习中课程数
     */
    private Integer studyingCourses;
    
    /**
     * 总笔记数
     */
    private Integer totalNotes;
    
    /**
     * 总作业提交数
     */
    private Integer totalHomeworkSubmissions;

    /**
     * 待完成作业数（已发布且学生未提交）
     */
    private Integer pendingHomeworkCount;
    
    /**
     * 总测验完成数
     */
    private Integer totalExamSubmissions;
}
