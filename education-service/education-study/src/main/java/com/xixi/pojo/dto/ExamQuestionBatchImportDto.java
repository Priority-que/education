package com.xixi.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量导入测验题目DTO。
 */
@Data
public class ExamQuestionBatchImportDto {

    /**
     * 测验ID。
     */
    private Long examId;

    /**
     * 教师ID。
     */
    private Long teacherId;

    /**
     * 题目列表。
     */
    private List<ExamQuestionCreateDto> questionList;
}
