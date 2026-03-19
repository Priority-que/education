package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师端：成绩权重DTO
 */
@Data
public class GradeWeightDto {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 考勤权重（百分比）
     */
    private BigDecimal attendanceWeight;

    /**
     * 作业权重（百分比）
     */
    private BigDecimal homeworkWeight;

    /**
     * 测验权重（百分比）
     */
    private BigDecimal quizWeight;

    /**
     * 考试权重（百分比）
     */
    private BigDecimal examWeight;
}

