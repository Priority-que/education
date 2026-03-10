package com.xixi.pojo.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程评价查询条件
 */
@Data
public class CourseCommentQuery {

    /** 页码 */
    private Integer pageNum = 1;

    /** 页大小 */
    private Integer pageSize = 10;

    /** 课程ID */
    private Long courseId;

    /** 视频ID（可选，未传表示课程维度评论） */
    private Long videoId;

    /** 用户ID */
    private Long userId;

    /** 评分（筛选等于该评分的） */
    private BigDecimal rating;

    /** 状态: 0-隐藏, 1-显示 */
    private Boolean status;

    /** 开始时间（按创建时间） */
    private LocalDateTime beginTime;

    /** 结束时间（按创建时间） */
    private LocalDateTime endTime;
}
