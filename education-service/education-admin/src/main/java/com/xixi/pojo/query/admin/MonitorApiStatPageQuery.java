package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * API 统计分页查询参数。
 */
@Data
public class MonitorApiStatPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
}
