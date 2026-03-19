package com.xixi.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习笔记VO
 */
@Data
public class StudyNoteVo {
    
    /**
     * 笔记ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 章节名称
     */
    private String chapterName;
    
    /**
     * 视频ID
     */
    private Long videoId;
    
    /**
     * 视频名称
     */
    private String videoName;
    
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
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
