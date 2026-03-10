package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交VO
 */
@Data
public class HomeworkSubmissionVo {
    
    /**
     * 提交ID
     */
    private Long id;
    
    /**
     * 作业ID
     */
    private Long homeworkId;
    
    /**
     * 作业标题
     */
    private String homeworkTitle;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNumber;
    
    /**
     * 提交内容
     */
    private String submissionContent;
    
    /**
     * 附件地址
     */
    private String attachmentUrl;
    
    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 教师反馈
     */
    private String feedback;
    
    /**
     * 批改教师ID
     */
    private Long gradedBy;
    
    /**
     * 批改时间
     */
    private LocalDateTime gradedTime;
    
    /**
     * 状态
     */
    private String status;

    /**
     * 作业总分
     */
    private Integer homeworkTotalScore;

    /**
     * 批注模式
     */
    private String annotationMode;

    /**
     * 批注内容
     */
    private String annotationContent;

    /**
     * 结构化批注JSON
     */
    private String annotationDataJson;

    /**
     * 批注附件列表
     */
    private List<String> annotationAttachments;

    /**
     * 是否含批注
     */
    private Boolean hasAnnotation;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
