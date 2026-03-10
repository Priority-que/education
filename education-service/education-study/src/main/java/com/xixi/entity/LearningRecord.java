package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("learning_record")
public class LearningRecord {
    
    /**
     * 学习记录ID
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
     * 开始学习时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束学习时间
     */
    private LocalDateTime endTime;
    
    /**
     * 本次学习时长(秒)
     */
    private Integer duration;
    
    /**
     * 视频进度百分比
     */
    private BigDecimal videoProgress;

    /**
     * 上次学习位置(秒)
     */
    private Integer lastPosition;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

