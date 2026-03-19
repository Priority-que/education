package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 教师端测验成绩分布分析结果。
 */
@Data
public class CourseExamAnalysisVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 各测验平均分。
     */
    private List<ExamAverageScoreItem> examAverageScoreList;

    /**
     * 成绩分布图数据。
     */
    private Map<String, Integer> scoreRangeDistribution;

    /**
     * 各题正确率。
     */
    private List<QuestionAccuracyItem> questionAccuracyList;

    /**
     * 测验平均分项。
     */
    @Data
    public static class ExamAverageScoreItem {
        private Long examId;
        private String examTitle;
        private BigDecimal averageScore;
    }

    /**
     * 题目正确率项。
     */
    @Data
    public static class QuestionAccuracyItem {
        private Long examId;
        private String examTitle;
        private Long questionId;
        private String questionType;
        private String questionContent;
        private BigDecimal correctRate;
    }
}
