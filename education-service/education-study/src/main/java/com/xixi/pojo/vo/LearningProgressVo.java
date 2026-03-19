package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 学习进度VO
 */
@Data
public class LearningProgressVo {
    
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

