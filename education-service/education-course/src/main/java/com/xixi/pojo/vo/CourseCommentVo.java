package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程评价 VO
 */
@Data
public class CourseCommentVo {

    /** 评价ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 视频ID（可选，未传表示课程维度评论） */
    private Long videoId;

    /** 用户ID */
    private Long userId;

    /** 评分 */
    private BigDecimal rating;

    /** 评价内容 */
    private String commentContent;

    /** 点赞数 */
    private Integer likeCount;

    /** 回复数 */
    private Integer replyCount;

    /** 是否匿名: 0-否, 1-是 */
    private Boolean isAnonymous;

    /** 状态: 0-隐藏, 1-显示 */
    private Boolean status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
