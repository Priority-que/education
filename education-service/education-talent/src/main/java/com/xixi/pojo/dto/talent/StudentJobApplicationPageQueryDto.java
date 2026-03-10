package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 学生端投递分页参数。
 */
@Data
public class StudentJobApplicationPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String status;
    private String keyword;
}
