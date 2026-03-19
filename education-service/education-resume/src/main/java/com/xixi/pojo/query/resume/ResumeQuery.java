package com.xixi.pojo.query.resume;

import lombok.Data;

/**
 * 5.4 我的简历分页查询参数。
 */
@Data
public class ResumeQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String visibility;
    private String keyword;
}
