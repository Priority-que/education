package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程讨论 VO
 */
@Data
public class CourseDiscussionVo {

    /** 讨论ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 章节ID */
    private Long chapterId;

    /** 用户ID */
    private Long userId;

    /** 父评论ID */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 回复数 */
    private Integer replyCount;

    /** 是否教师回复 */
    private Boolean isTeacherReply;

    /** 状态: 0-隐藏, 1-显示 */
    private Boolean status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
