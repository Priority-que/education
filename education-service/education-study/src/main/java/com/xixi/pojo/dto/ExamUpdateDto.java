package com.xixi.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编辑测验DTO。
 */
@Data
public class ExamUpdateDto {

    /**
     * 测验ID。
     */
    private Long id;

    /**
     * 教师ID。
     */
    private Long teacherId;

    /**
     * 课程ID（草稿可修改）。
     */
    private Long courseId;

    /**
     * 测验标题。
     */
    private String examTitle;

    /**
     * 测验描述。
     */
    private String examDescription;

    /**
     * 测验类型：QUIZ/MIDTERM/FINAL。
     */
    private String examType;

    /**
     * 总分。
     */
    private Integer totalScore;

    /**
     * 及格分。
     */
    private Integer passScore;

    /**
     * 时长限制（分钟，0表示不限）。
     */
    private Integer timeLimit;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;
}
