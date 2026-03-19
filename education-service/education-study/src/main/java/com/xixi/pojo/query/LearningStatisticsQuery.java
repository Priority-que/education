package com.xixi.pojo.query;

import lombok.Data;

/**
 * 学习统计查询条件
 */
@Data
public class LearningStatisticsQuery {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 统计周期: DAY-日, WEEK-周, MONTH-月
     */
    private String period;
    
    /**
     * 开始日期
     */
    private String startDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}

