package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交附件信息
 */
@Data
public class HomeworkSubmissionAttachmentVo {

    /**
     * 提交ID
     */
    private Long submissionId;

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
     * 附件地址
     */
    private String attachmentUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;
}
