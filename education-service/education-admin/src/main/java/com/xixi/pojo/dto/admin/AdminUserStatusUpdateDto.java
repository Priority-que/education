package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 用户状态更新请求参数。
 */
@Data
public class AdminUserStatusUpdateDto {
    /**
     * 0-禁用，1-启用。
     */
    private Integer status;
}
