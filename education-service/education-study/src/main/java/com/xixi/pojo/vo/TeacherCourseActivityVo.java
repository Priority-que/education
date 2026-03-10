package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师端课程学生活跃度统计结果。
 */
@Data
public class TeacherCourseActivityVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 活跃学生数。
     */
    private Integer activeStudentCount;

    /**
     * 学习时长排名。
     */
    private List<StudyTimeRankItem> studyTimeRanking;

    /**
     * 学习进度排名。
     */
    private List<ProgressRankItem> progressRanking;

    /**
     * 活跃时间段分布。
     */
    private List<ActiveTimeDistributionItem> activeTimeDistribution;

    /**
     * 学习时长排名项。
     */
    @Data
    public static class StudyTimeRankItem {
        private Integer rank;
        private Long studentId;
        private Long userId;
        private String studentNumber;
        private String studentName;
        private Integer totalStudyTime;
    }

    /**
     * 学习进度排名项。
     */
    @Data
    public static class ProgressRankItem {
        private Integer rank;
        private Long studentId;
        private Long userId;
        private String studentNumber;
        private String studentName;
        private BigDecimal progressPercentage;
    }

    /**
     * 活跃时间段分布项。
     */
    @Data
    public static class ActiveTimeDistributionItem {
        private Integer hour;
        private String timeRange;
        private Integer studyTime;
    }
}

