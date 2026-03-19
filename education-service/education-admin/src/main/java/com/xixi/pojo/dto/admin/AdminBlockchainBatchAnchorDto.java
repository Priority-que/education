package com.xixi.pojo.dto.admin;

import lombok.Data;

import java.util.List;

/**
 * 管理端批量触发证书上链请求参数。
 */
@Data
public class AdminBlockchainBatchAnchorDto {
    private List<Long> certificateIds;
}
