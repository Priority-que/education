package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端学生学习监控结果。
 */
@Data
public class TeacherLearningMonitorVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 总学生数。
     */
    private Integer totalStudentCount;

    /**
     * 活跃学生数（学习时长大于0）。
     */
    private Integer activeStudentCount;

    /**
     * 学生学习进度列表。
     */
    private List<StudentProgressItem> studentProgressList;

    /**
     * 章节完成情况列表。
     */
    private List<ChapterCompletionItem> chapterCompletionList;

    /**
     * 学习时长统计。
     */
    private StudyDurationStatistics studyDurationStatistics;

    /**
     * 学生学习进度项。
     */
    @Data
    public static class StudentProgressItem {
        private Long studentId;
        private Long userId;
        private String studentNumber;
        private String studentName;
        private String nickname;
        private String avatar;
        private String learningStatus;
        private BigDecimal progressPercentage;
        private Integer totalStudyTime;
        private LocalDateTime lastStudyTime;
        private Integer completedChapterCount;
        private Integer totalChapterCount;
    }

    /**
     * 章节完成情况项。
     */
    @Data
    public static class ChapterCompletionItem {
        private Long chapterId;
        private String chapterName;
        private Integer totalStudents;
        private Integer completedStudents;
        private BigDecimal completionRate;
        private BigDecimal averageProgress;
        private Integer totalStudyTime;
    }

    /**
     * 学习时长统计。
     */
    @Data
    public static class StudyDurationStatistics {
        private Integer totalStudyTime;
        private Integer averageStudyTime;
        private Integer maxStudyTime;
        private Integer minStudyTime;
    }
}

