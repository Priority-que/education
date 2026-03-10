package com.xixi.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 保存学习记录DTO
 */
@Data
public class LearningRecordSaveDto {
    
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
     * 视频进度百分比
     */
    private BigDecimal videoProgress;
    
    /**
     * 本次学习时长(秒)
     */
    private Integer duration;

    /**
     * 上次学习位置(秒)
     */
    private Integer lastPosition;
}
