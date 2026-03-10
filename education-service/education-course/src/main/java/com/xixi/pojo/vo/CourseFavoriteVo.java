package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏 VO
 */
@Data
public class CourseFavoriteVo {

    /** 收藏ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 用户ID */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
