package com.xixi.pojo.dto.admin;

import lombok.Data;

import java.util.List;

/**
 * 批量更新系统配置请求参数。
 */
@Data
public class SystemConfigBatchUpdateDto {
    private List<SystemConfigUpdateDto> items;
}
