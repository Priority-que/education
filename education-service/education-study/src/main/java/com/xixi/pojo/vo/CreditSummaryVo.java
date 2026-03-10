package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 学分累计统计VO
 */
@Data
public class CreditSummaryVo {
    
    /**
     * 总学分
     */
    private BigDecimal totalCredits;
    
    /**
     * 各课程学分明细
     */
    private List<CourseCredit> courseCredits;
    
    /**
     * 按学期/学年分组统计
     */
    private List<SemesterCredit> semesterCredits;
    
    @Data
    public static class CourseCredit {
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 课程名称
         */
        private String courseName;
        
        /**
         * 获得学分
         */
        private BigDecimal creditEarned;
        
        /**
         * 成绩等级
         */
        private String gradeLevel;
    }
    
    @Data
    public static class SemesterCredit {
        /**
         * 学期/学年
         */
        private String semester;
        
        /**
         * 该学期总学分
         */
        private BigDecimal totalCredits;
        
        /**
         * 课程数量
         */
        private Integer courseCount;
    }
}
















