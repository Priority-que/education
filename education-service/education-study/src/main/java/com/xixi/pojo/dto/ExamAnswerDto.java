package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 提交答案DTO
 */
@Data
public class ExamAnswerDto {
    
    /**
     * 提交ID
     */
    private Long submissionId;
    
    /**
     * 答案(JSON格式)
     */
    private String answers;
}

