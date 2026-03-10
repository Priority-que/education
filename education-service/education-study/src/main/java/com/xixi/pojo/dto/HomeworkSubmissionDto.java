package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 作业提交DTO
 */
@Data
public class HomeworkSubmissionDto {
    
    /**
     * 作业ID
     */
    private Long homeworkId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 提交内容
     */
    private String submissionContent;
    
    /**
     * 附件地址
     */
    private String attachmentUrl;
}

