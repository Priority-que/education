package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 课程分类传输对象
 */
@Data
public class CourseCategoryDto {
    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Boolean status;
}
