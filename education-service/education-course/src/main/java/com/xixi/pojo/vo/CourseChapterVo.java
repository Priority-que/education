package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程章节返回对象
 */
@Data
public class CourseChapterVo {
    private Long id;
    private Long courseId;
    private String chapterName;
    private String chapterDescription;
    private Integer sortOrder;
    private Integer duration;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
