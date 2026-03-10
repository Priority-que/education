package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * 服务监控分页查询参数。
 */
@Data
public class MonitorServicePageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String serviceName;
    private String status;
}
