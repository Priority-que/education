package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 测验提交DTO
 */
@Data
public class ExamSubmissionDto {
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 答案(JSON格式)
     */
    private String answers;
}
















