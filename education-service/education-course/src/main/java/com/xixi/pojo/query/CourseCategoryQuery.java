package com.xixi.pojo.query;

import lombok.Data;

/**
 * 课程分类查询条件
 */
@Data
public class CourseCategoryQuery {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 启用状态
     */
    private Boolean status;

    /**
     * 父分类ID
     */
    private Long parentId;
}
