package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_video")
public class CourseVideo {
    
    /**
     * 视频ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 视频名称
     */
    private String videoName;
    
    /**
     * 视频地址
     */

    private String videoUrl;
    
    /**
     * 封面图
     */

    private String coverImage;
    
    /**
     * 视频时长(秒)
     */

    private Integer duration;
    
    /**
     * 文件大小(字节)
     */

    private Long fileSize;
    
    /**
     * 视频格式
     */

    private String videoFormat;
    
    /**
     * 分辨率
     */

    private String resolution;

    /**
     * 播放类型：SINGLE / HLS
     */
    private String playbackType;

    /**
     * HLS 主播放清单地址
     */
    private String masterPlaylistUrl;

    /**
     * 清晰度列表(JSON)
     */
    private String renditions;

    /**
     * 默认清晰度
     */
    private String defaultResolution;
    
    /**
     * 排序
     */

    private Integer sortOrder;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Boolean status;
    
    /**
     * 播放次数
     */
    private Integer viewCount;
    
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
