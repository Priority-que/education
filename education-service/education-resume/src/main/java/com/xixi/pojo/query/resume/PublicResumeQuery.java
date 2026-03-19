package com.xixi.pojo.query.resume;

import lombok.Data;

/**
 * 6.2 公开简历分页查询参数。
 */
@Data
public class PublicResumeQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String major;
    private String degree;
}
