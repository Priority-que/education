package com.xixi.service;

import com.xixi.pojo.vo.certificate.CertificateBlockchainVo;

public interface CertificateBlockchainService {
    CertificateBlockchainVo getCertificateBlockchain(Long certificateId, Long userId, Integer userRole);
}

