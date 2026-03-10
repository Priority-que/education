package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 学习笔记评论参数
 */
@Data
public class StudyNoteCommentDto {

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 评论学生ID
     */
    private Long studentId;

    /**
     * 评论内容
     */
    private String commentContent;
}
