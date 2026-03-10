package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * GPA统计VO
 */
@Data
public class GpaVo {
    
    /**
     * 总GPA
     */
    private BigDecimal totalGpa;
    
    /**
     * 各课程GPA明细
     */
    private List<CourseGpa> courseGpaList;
    
    /**
     * GPA趋势（图表数据）
     */
    private List<GpaTrend> gpaTrends;
    
    @Data
    public static class CourseGpa {
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 课程名称
         */
        private String courseName;
        
        /**
         * 成绩等级
         */
        private String gradeLevel;
        
        /**
         * GPA
         */
        private BigDecimal gpa;
        
        /**
         * 获得学分
         */
        private BigDecimal creditEarned;
    }
    
    @Data
    public static class GpaTrend {
        /**
         * 学期/时间
         */
        private String period;
        
        /**
         * 该期间GPA
         */
        private BigDecimal gpa;
    }
}
















