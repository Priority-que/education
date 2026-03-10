package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 教师端测验提交统计VO。
 */
@Data
public class ExamSubmissionStatisticsVo {

    /**
     * 测验ID。
     */
    private Long examId;

    /**
     * 测验标题。
     */
    private String examTitle;

    /**
     * 总参与人数。
     */
    private Integer totalParticipants;

    /**
     * 平均分。
     */
    private BigDecimal averageScore;

    /**
     * 及格率（百分比）。
     */
    private BigDecimal passRate;

    /**
     * 分数段分布（90-100/80-89/70-79/60-69/0-59）。
     */
    private Map<String, Integer> scoreRangeDistribution;

    /**
     * 各题正确率。
     */
    private List<QuestionAccuracyVo> questionAccuracyList;

    @Data
    public static class QuestionAccuracyVo {
        /**
         * 题目ID。
         */
        private Long questionId;

        /**
         * 题目类型。
         */
        private String questionType;

        /**
         * 题目内容。
         */
        private String questionContent;

        /**
         * 正确率（百分比）。
         */
        private BigDecimal correctRate;
    }
}
