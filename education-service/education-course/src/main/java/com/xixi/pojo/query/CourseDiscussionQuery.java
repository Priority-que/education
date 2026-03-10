package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程讨论查询条件
 */
@Data
public class CourseDiscussionQuery {

    /** 页码 */
    private Integer pageNum = 1;

    /** 页大小 */
    private Integer pageSize = 10;

    /** 课程ID */
    private Long courseId;

    /** 章节ID */
    private Long chapterId;

    /** 用户ID */
    private Long userId;

    /** 父评论ID（查某条下的回复） */
    private Long parentId;

    /** 是否教师回复 */
    private Boolean isTeacherReply;

    /** 状态: 0-隐藏, 1-显示 */
    private Boolean status;

    /** 开始时间（按创建时间） */
    private LocalDateTime beginTime;

    /** 结束时间（按创建时间） */
    private LocalDateTime endTime;
}
