package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 课程章节传输对象
 */
@Data
public class CourseChapterDto {
    private Long id;
    private Long courseId;
    private String chapterName;
    private String chapterDescription;
    private Integer sortOrder;
    private Integer duration;
}
