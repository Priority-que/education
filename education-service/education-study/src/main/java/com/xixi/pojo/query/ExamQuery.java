package com.xixi.pojo.query;

import lombok.Data;

/**
 * 测验查询条件
 */
@Data
public class ExamQuery {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 学生ID（用于查询参与状态）
     */
    private Long studentId;

    /**
     * 教师ID（教师端查询使用）
     */
    private Long teacherId;

    /**
     * 测验状态：DRAFT/PUBLISHED/IN_PROGRESS/ENDED
     */
    private String status;

    /**
     * 测验类型：QUIZ/MIDTERM/FINAL
     */
    private String examType;

    /**
     * 排序字段：createdTime/startTime/endTime
     */
    private String sortBy;

    /**
     * 排序方式：ASC/DESC
     */
    private String sortOrder = "DESC";
}
