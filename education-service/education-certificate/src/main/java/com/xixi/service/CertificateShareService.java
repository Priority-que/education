package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.certificate.CertificateShareCreateDto;
import com.xixi.pojo.query.certificate.CertificateShareMyQuery;
import com.xixi.pojo.vo.certificate.CertificatePublicShareVo;
import com.xixi.pojo.vo.certificate.CertificateShareVo;
import com.xixi.web.Result;

public interface CertificateShareService {
    Result createShare(CertificateShareCreateDto dto, Long studentId);

    Result revokeShare(Long shareId, Long studentId);

    IPage<CertificateShareVo> getMySharePage(CertificateShareMyQuery query, Long studentId);

    CertificatePublicShareVo getPublicShareDetail(String shareToken);
}

