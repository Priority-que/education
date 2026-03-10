package com.xixi.pojo.query;

import lombok.Data;

/**
 * 课程章节查询条件
 */
@Data
public class CourseChapterQuery {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节名称（模糊查询）
     */
    private String chapterName;
}
