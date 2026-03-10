package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 学生端岗位广场分页参数。
 */
@Data
public class JobPublicPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String jobType;
    private String city;
    private String educationRequirement;
    private Boolean onlyOpen = true;
}
