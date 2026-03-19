package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 学生选课DTO
 */
@Data
public class StudentCourseJoinDto {
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
}

