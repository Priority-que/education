package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 测验结果VO
 */
@Data
public class ExamResultVo {
    
    /**
     * 提交ID
     */
    private Long submissionId;
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 测验标题
     */
    private String examTitle;
    
    /**
     * 总分
     */
    private BigDecimal totalScore;
    
    /**
     * 各题得分详情
     */
    private List<QuestionResult> questionResults;

    /**
     * 各题得分详情（前端兼容字段）
     */
    private List<QuestionResult> questionResultList;
    
    /**
     * 答题时间（分钟）
     */
    private Integer answerTime;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    @Data
    public static class QuestionResult {
        /**
         * 题目ID
         */
        private Long questionId;
        
        /**
         * 题目内容
         */
        private String questionContent;
        
        /**
         * 题目类型
         */
        private String questionType;
        
        /**
         * 学生答案
         */
        private String studentAnswer;
        
        /**
         * 正确答案（如果允许查看）
         */
        private String correctAnswer;
        
        /**
         * 得分
         */
        private BigDecimal score;
        
        /**
         * 满分
         */
        private Integer fullScore;

        /**
         * 满分（前端兼容字段）
         */
        private Integer maxScore;

        /**
         * 是否答对（主观题通常为null）
         */
        private Boolean isCorrect;

        /**
         * 批改状态：AUTO_GRADED/PENDING_REVIEW/REVIEWED
         */
        private String reviewStatus;
    }
}
