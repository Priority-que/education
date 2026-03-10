package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 更新学习笔记DTO
 */
@Data
public class StudyNoteUpdateDto {
    
    /**
     * 笔记ID
     */
    private Long noteId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 笔记标题
     */
    private String noteTitle;
    
    /**
     * 笔记内容
     */
    private String noteContent;
    
    /**
     * 是否公开: true-公开, false-私有
     */
    private Boolean isPublic;
}
