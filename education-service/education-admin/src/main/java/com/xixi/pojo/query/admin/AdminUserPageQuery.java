package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * 管理端用户分页查询参数。
 */
@Data
public class AdminUserPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
    private String userRole;
    /**
     * 0-禁用，1-启用。
     */
    private Integer status;
}
