package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户分页项。
 */
@Data
public class AdminUserPageVo {
    private Long userId;
    private String userName;
    private String userRole;
    private Integer operationCount;
    private LocalDateTime lastOperationTime;
    /**
     * 0-禁用，1-启用。
     */
    private Integer status;
}
