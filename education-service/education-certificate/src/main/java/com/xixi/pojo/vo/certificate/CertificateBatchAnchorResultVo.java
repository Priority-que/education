package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.util.List;

/**
 * 批量上链结果。
 */
@Data
public class CertificateBatchAnchorResultVo {
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private List<CertificateAnchorResultVo> successItems;
    private List<CertificateBatchAnchorFailItemVo> failedItems;
}

