package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师端：录入成绩DTO
 */
@Data
public class GradeCreateDto {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 考勤成绩
     */
    private BigDecimal attendanceScore;

    /**
     * 作业成绩
     */
    private BigDecimal homeworkScore;

    /**
     * 测验成绩
     */
    private BigDecimal quizScore;

    /**
     * 考试成绩
     */
    private BigDecimal examScore;
}

