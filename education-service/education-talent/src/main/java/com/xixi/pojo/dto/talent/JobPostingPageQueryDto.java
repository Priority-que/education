package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 岗位分页查询参数。
 */
@Data
public class JobPostingPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String status;
    private String jobType;
    private String keyword;
}
