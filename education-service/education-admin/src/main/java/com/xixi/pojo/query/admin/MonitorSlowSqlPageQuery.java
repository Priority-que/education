package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * 慢 SQL 分页查询参数。
 */
@Data
public class MonitorSlowSqlPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
}
