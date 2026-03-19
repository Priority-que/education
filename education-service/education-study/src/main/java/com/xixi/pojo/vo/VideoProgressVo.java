package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 视频学习进度VO
 */
@Data
public class VideoProgressVo {
    
    /**
     * 视频ID
     */
    private Long videoId;
    
    /**
     * 视频进度百分比
     */
    private BigDecimal videoProgress;
    
    /**
     * 上次学习位置(秒)
     */
    private Integer lastPosition;
}
