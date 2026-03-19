package com.xixi.pojo.query;

import lombok.Data;

/**
 * 作业查询条件。
 */
@Data
public class HomeworkQuery {

    /**
     * 页码。
     */
    private Integer pageNum = 1;

    /**
     * 每页大小。
     */
    private Integer pageSize = 10;

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 教师ID（教师端查询使用）。
     */
    private Long teacherId;

    /**
     * 作业状态：DRAFT/PUBLISHED/CLOSED。
     */
    private String status;

    /**
     * 排序字段：createdTime/deadline/totalScore。
     */
    private String sortBy;

    /**
     * 排序方式：ASC/DESC。
     */
    private String sortOrder = "DESC";

    /**
     * 学生ID（用于查询提交状态）。
     */
    private Long studentId;
}
