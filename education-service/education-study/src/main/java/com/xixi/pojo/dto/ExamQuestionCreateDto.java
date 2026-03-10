package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 新增测验题目DTO。
 */
@Data
public class ExamQuestionCreateDto {

    /**
     * 测验ID。
     */
    private Long examId;

    /**
     * 教师ID。
     */
    private Long teacherId;

    /**
     * 题目类型：SINGLE_CHOICE/MULTI_CHOICE/TRUE_FALSE/FILL_BLANK/ESSAY。
     */
    private String questionType;

    /**
     * 题目内容。
     */
    private String questionContent;

    /**
     * 选项（JSON数组）。
     */
    private String options;

    /**
     * 正确答案。
     */
    private String correctAnswer;

    /**
     * 分值。
     */
    private Integer score;

    /**
     * 排序。
     */
    private Integer sortOrder;
}
