package com.xixi.pojo.query;

import lombok.Data;

/**
 * 作业提交查询条件
 */
@Data
public class HomeworkSubmissionQuery {
    
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
     * 作业ID
     */
    private Long homeworkId;
    
    /**
     * 提交状态
     */
    private String status;

    /**
     * 教师ID（教师端查询使用）
     */
    private Long teacherId;

    /**
     * 排序字段：submissionTime/gradedTime/score
     */
    private String sortBy;

    /**
     * 排序方式：ASC/DESC
     */
    private String sortOrder = "DESC";
}
