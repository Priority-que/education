package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseQuery {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    /**
     * 页大小
     */
    private Integer pageSize = 10;
    /**
     * 课程代码
     */
    private String courseCode;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 课程是否收费
     */
    private Boolean isFree;
    /**
     * 开始时间
     */
    private LocalDateTime beginTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;

    /**
     * 分类ID（按学科分类筛选）
     */
    private Long categoryId;

    /**
     * 关键词搜索（同时匹配课程名和简介）
     */
    private String keyword;

    /**
     * 排序字段：published_time-发布时间，view_count-热度，rating-评分
     */
    private String sortBy;

    /**
     * 排序方向：ASC-升序，DESC-降序
     */
    private String sortOrder;

    /**
     * 教师ID（用于教师端按归属教师过滤）
     */
    private Long teacherId;
}
