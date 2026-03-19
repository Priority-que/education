package com.xixi.pojo.vo;

import lombok.Data;

/**
 * 测验题目VO（学生端，不包含正确答案）
 */
@Data
public class ExamQuestionVo {
    
    /**
     * 试题ID
     */
    private Long id;
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 题目类型
     */
    private String questionType;
    
    /**
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 选项(JSON数组)
     */
    private String options;

    /**
     * 正确答案（教师端展示）
     */
    private String correctAnswer;
    
    /**
     * 分值
     */
    private Integer score;
    
    /**
     * 排序
     */
    private Integer sortOrder;
}
