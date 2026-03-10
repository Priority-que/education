package com.xixi.pojo.query;

import lombok.Data;

/**
 * 学习笔记查询条件
 */
@Data
public class StudyNoteQuery {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
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
     * 是否只查询公开笔记（用于查看课程公开笔记）
     */
    private Boolean onlyPublic;
}
