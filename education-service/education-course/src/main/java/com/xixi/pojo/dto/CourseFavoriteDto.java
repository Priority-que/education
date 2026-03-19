package com.xixi.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏 DTO
 */
@Data
public class CourseFavoriteDto {

    /** 收藏ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 用户ID */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
