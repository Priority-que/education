package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.query.certificate.CertificateMyQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateMyPageVo;

public interface StudentCertificateService {
    IPage<CertificateMyPageVo> getMyCertificatePage(CertificateMyQuery query, Long studentId);

    CertificateDetailVo getMyCertificateDetail(Long certificateId, Long studentId);

    CertificateDetailVo getMyCertificateByNumber(String certificateNumber, Long studentId);
}

