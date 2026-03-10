package com.xixi.pojo.query;

import lombok.Data;

/**
 * 测验提交查询条件
 */
@Data
public class ExamSubmissionQuery {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
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
     * 测验类型
     */
    private String examType;

    /**
     * 教师ID（教师端查询使用）
     */
    private Long teacherId;

    /**
     * 测验ID
     */
    private Long examId;

    /**
     * 提交状态：IN_PROGRESS/PENDING_REVIEW/GRADED
     * 兼容旧值：SUBMITTED/AUTO_GRADED/MANUAL_GRADED
     */
    private String status;

    /**
     * 排序字段：submitTime/startTime/totalScore
     */
    private String sortBy;

    /**
     * 排序方式：ASC/DESC
     */
    private String sortOrder = "DESC";
}
