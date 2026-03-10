package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.certificate.CertificateBatchAnchorDto;
import com.xixi.pojo.vo.certificate.CertificateAdminBlockchainPageVo;
import com.xixi.pojo.vo.certificate.CertificateAnchorResultVo;
import com.xixi.web.Result;

public interface CertificateAdminBlockchainService {
    IPage<CertificateAdminBlockchainPageVo> getCertificatePage(
            Long pageNum,
            Long pageSize,
            String certificateNumber,
            String status,
            Long blockHeight
    );

    Result anchorCertificate(Long certificateId, Long operatorId);

    Result anchorBatchCertificates(CertificateBatchAnchorDto dto, Long operatorId);

    CertificateAnchorResultVo queryAnchoredResult(Long certificateId);
}
