package com.xixi.pojo.dto.certificate;

import lombok.Data;

import java.util.List;

/**
 * 管理端批量上链请求参数。
 */
@Data
public class CertificateBatchAnchorDto {
    private List<Long> certificateIds;
}

