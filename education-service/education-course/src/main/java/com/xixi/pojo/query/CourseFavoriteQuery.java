package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏查询条件
 */
@Data
public class CourseFavoriteQuery {

    /** 页码 */
    private Integer pageNum = 1;

    /** 页大小 */
    private Integer pageSize = 10;

    /** 课程ID */
    private Long courseId;

    /** 用户ID */
    private Long userId;

    /** 开始时间（按创建时间） */
    private LocalDateTime beginTime;

    /** 结束时间（按创建时间） */
    private LocalDateTime endTime;
}
