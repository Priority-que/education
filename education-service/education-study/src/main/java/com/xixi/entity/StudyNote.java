package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_note")
public class StudyNote {
    
    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
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
     * 是否公开: 0-私有, 1-公开
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

