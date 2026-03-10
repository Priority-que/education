package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("learning_statistics")
public class LearningStatistics {
    
    /**
     * 统计ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 当日学习总时长(秒)
     */
    private Integer totalStudyTime;
    
    /**
     * 当日学习课程数
     */
    private Integer coursesStudied;
    
    /**
     * 当日观看视频数
     */
    private Integer videosWatched;
    
    /**
     * 当日记笔记数
     */
    private Integer notesTaken;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

