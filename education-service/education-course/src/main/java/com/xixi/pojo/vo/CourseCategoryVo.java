package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程分类返回对象
 */
@Data
public class CourseCategoryVo {
    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Boolean status;
    private LocalDateTime createdTime;
}
