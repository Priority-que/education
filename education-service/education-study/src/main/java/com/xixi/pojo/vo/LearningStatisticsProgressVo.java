package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 学习进度统计VO
 */
@Data
public class LearningStatisticsProgressVo {
    
    /**
     * 课程进度列表
     */
    private List<CourseProgress> courseProgressList;
    
    @Data
    public static class CourseProgress {
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 课程名称
         */
        private String courseName;
        
        /**
         * 学习进度百分比
         */
        private BigDecimal progressPercentage;
    }
}
















