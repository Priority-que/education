package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩统计VO
 */
@Data
public class GradeStatisticsVo {
    
    /**
     * 总学分统计
     */
    private BigDecimal totalCredits;
    
    /**
     * 平均GPA
     */
    private BigDecimal averageGpa;
    
    /**
     * 成绩分布（A/B/C/D/F数量）
     */
    private Map<String, Integer> gradeDistribution;
    
    /**
     * 各课程成绩趋势（图表数据）
     */
    private List<CourseGradeTrend> courseGradeTrends;
    
    @Data
    public static class CourseGradeTrend {
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 课程名称
         */
        private String courseName;
        
        /**
         * 最终成绩
         */
        private BigDecimal finalScore;
        
        /**
         * 成绩等级
         */
        private String gradeLevel;
        
        /**
         * 发布时间
         */
        private String publishedTime;
    }
}
















