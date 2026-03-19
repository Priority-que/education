package com.xixi.pojo.query;

import lombok.Data;

/**
 * 学生选课查询条件
 */
@Data
public class StudentCourseQuery {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程关键词（按课程名称模糊查询）
     */
    private String keyword;
    
    /**
     * 学习状态: STUDYING-学习中, COMPLETED-已完成, DROPPED-已退课
     */
    private String learningStatus;

    /**
     * 归一化后的持久化学习状态，仅供服务层传递给 Mapper 使用。
     */
    private String persistedLearningStatus;

    /**
     * 是否仅查询“未开始”课程（已选课但学习进度为 0）。
     */
    private Boolean notStartedOnly;
    
    /**
     * 排序字段: selectedTime-选课时间, progressPercentage-学习进度, lastStudyTime-最后学习时间
     */
    private String sortBy;
    
    /**
     * 排序方式: ASC-升序, DESC-降序
     */
    private String sortOrder = "DESC";
}

















