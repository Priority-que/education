package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习记录VO
 */
@Data
public class LearningRecordVo {
    
    /**
     * 学习记录ID
     */
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
    private LocalDateTime createdTime;
}
