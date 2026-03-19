package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 开始测验DTO
 */
@Data
public class ExamStartDto {
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 学生ID
     */
    private Long studentId;
}

