package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 创建学习笔记DTO
 */
@Data
public class StudyNoteCreateDto {
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 视频ID
     */
    private Long videoId;
    
    /**
     * 笔记标题
     */
    private String noteTitle;
    
    /**
     * 笔记内容
     */
    private String noteContent;
    
    /**
     * 视频时间点(秒)
     */
    private Integer videoTimestamp;
    
    /**
     * 是否公开: true-公开, false-私有
     */
    private Boolean isPublic;
}

