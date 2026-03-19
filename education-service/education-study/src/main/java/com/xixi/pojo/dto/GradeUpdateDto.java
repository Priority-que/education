package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师端：修改成绩DTO
 */
@Data
public class GradeUpdateDto {

    /**
     * 成绩ID
     */
    private Long id;

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

