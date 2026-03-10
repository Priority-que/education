package com.xixi.pojo.query;

import lombok.Data;

/**
 * 教师端选课学生查询条件
 */
@Data
public class TeacherStudentCourseQuery {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 学习状态: STUDYING-学习中, COMPLETED-已完成, DROPPED-已退课
     */
    private String learningStatus;
    
    /**
     * 排序字段: selectedTime-选课时间, progressPercentage-学习进度, totalStudyTime-学习时长, lastStudyTime-最后学习时间
     */
    private String sortBy;
    
    /**
     * 排序方式: ASC-升序, DESC-降序
     */
    private String sortOrder = "DESC";
    
    /**
     * 学生姓名（模糊查询）
     */
    private String studentName;
    
    /**
     * 学号（模糊查询）
     */
    private String studentNumber;
}
















