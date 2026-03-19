package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.certificate.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByQrcodeDto;
import com.xixi.pojo.query.certificate.CertificateVerifyHistoryQuery;
import com.xixi.pojo.vo.certificate.CertificateVerifyBatchResultVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyHistoryVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyReportVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyResultVo;

public interface CertificateVerificationService {
    CertificateVerifyResultVo verifyByNumber(CertificateVerifyByNumberDto dto, Long verifierId, Integer verifierRole, String ipAddress, String userAgent);

    CertificateVerifyResultVo verifyByQrcode(CertificateVerifyByQrcodeDto dto, Long verifierId, Integer verifierRole, String ipAddress, String userAgent);

    CertificateVerifyBatchResultVo verifyBatch(CertificateVerifyBatchDto dto, Long verifierId, Integer verifierRole, String ipAddress, String userAgent);

    IPage<CertificateVerifyHistoryVo> getVerifyHistory(CertificateVerifyHistoryQuery query, Long verifierId, Integer verifierRole);

    CertificateVerifyReportVo getVerifyReport(Long verificationId, Long verifierId, Integer verifierRole);
}

