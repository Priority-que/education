package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程学习详情VO
 */
@Data
public class StudentCourseDetailVo {
    /**
     * 选课ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 课程封面
     */
    private String courseCover;
    
    /**
     * 课程描述
     */
    private String courseDescription;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
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
     * 学生评分
     */
    private BigDecimal rating;
    
    /**
     * 学习心得
     */
    private String reviewContent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 学习进度详情（各章节完成情况）
     * TODO: 需要调用课程服务获取章节信息
     */
    private List<ChapterProgressVo> chapterProgressList;
    
    /**
     * 作业完成情况
     * TODO: 需要查询作业提交表
     */
    private List<HomeworkProgressVo> homeworkProgressList;
    
    /**
     * 测验完成情况
     * TODO: 需要查询测验提交表
     */
    private List<ExamProgressVo> examProgressList;
    
    /**
     * 成绩信息
     * TODO: 需要查询成绩表
     */
    private GradeVo gradeInfo;
    
    /**
     * 章节进度VO
     */
    @Data
    public static class ChapterProgressVo {
        private Long chapterId;
        private String chapterName;
        private BigDecimal progressPercentage;
        private Integer studyTime;
    }
    
    /**
     * 作业进度VO
     */
    @Data
    public static class HomeworkProgressVo {
        private Long homeworkId;
        private String homeworkTitle;
        private String status;
        private BigDecimal score;
    }
    
    /**
     * 测验进度VO
     */
    @Data
    public static class ExamProgressVo {
        private Long examId;
        private String examTitle;
        private String status;
        private BigDecimal score;
    }
    
    /**
     * 成绩VO
     */
    @Data
    public static class GradeVo {
        private BigDecimal attendanceScore;
        private BigDecimal homeworkScore;
        private BigDecimal examScore;
        private BigDecimal finalScore;
        private String gradeLevel;
        private BigDecimal gpa;
    }
}

















