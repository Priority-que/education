package com.xixi.pojo.query;

import lombok.Data;

/**
 * 成绩查询条件
 */
@Data
public class GradeQuery {
    
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
     * 是否通过
     */
    private Boolean isPass;

    /**
     * 是否已发布
     */
    private Boolean isPublished;
}
















