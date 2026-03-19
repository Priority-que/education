package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端学生学习详情VO
 */
@Data
public class TeacherStudentDetailVo {
    /**
     * 选课ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生基本信息
     */
    private StudentInfo studentInfo;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 选课时间
     */
    private LocalDateTime selectedTime;
    
    /**
     * 学习状态: STUDYING-学习中, COMPLETED-已完成, DROPPED-已退课
     */
    private String learningStatus;
    
    /**
     * 学习进度百分比
     */
    private BigDecimal progressPercentage;
    
    /**
     * 总学习时长(秒)
     */
    private Integer totalStudyTime;
    
    /**
     * 最后学习时间
     */
    private LocalDateTime lastStudyTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 学习记录明细
     * TODO: 需要查询学习记录表
     */
    private List<LearningRecordInfo> learningRecords;
    
    /**
     * 笔记列表
     * TODO: 需要查询笔记表
     */
    private List<NoteInfo> notes;
    
    /**
     * 作业提交情况
     * TODO: 需要查询作业提交表
     */
    private List<HomeworkSubmissionInfo> homeworkSubmissions;
    
    /**
     * 测验完成情况
     * TODO: 需要查询测验提交表
     */
    private List<ExamSubmissionInfo> examSubmissions;
    
    /**
     * 学生基本信息
     */
    @Data
    public static class StudentInfo {
        private Long studentId;
        private Long userId;
        private String studentNumber;
        private String studentName;
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
        private String school;
        private String college;
        private String major;
    }
    
    /**
     * 学习记录信息
     */
    @Data
    public static class LearningRecordInfo {
        private Long id;
        private Long chapterId;
        private String chapterName;
        private Long videoId;
        private String videoName;
        private LocalDateTime studyTime;
        private Integer duration;
        private BigDecimal progress;
    }
    
    /**
     * 笔记信息
     */
    @Data
    public static class NoteInfo {
        private Long id;
        private Long chapterId;
        private String chapterName;
        private Long videoId;
        private String videoName;
        private String content;
        private LocalDateTime createdTime;
    }
    
    /**
     * 作业提交信息
     */
    @Data
    public static class HomeworkSubmissionInfo {
        private Long homeworkId;
        private String homeworkTitle;
        private String status;
        private BigDecimal score;
        private LocalDateTime submitTime;
    }
    
    /**
     * 测验提交信息
     */
    @Data
    public static class ExamSubmissionInfo {
        private Long examId;
        private String examTitle;
        private String status;
        private BigDecimal score;
        private LocalDateTime submitTime;
    }
}
















