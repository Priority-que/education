package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.certificate.CertificateIssueDto;
import com.xixi.pojo.dto.certificate.CertificateRevokeDto;
import com.xixi.pojo.query.certificate.CertificateTeacherIssuedQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateTeacherIssuedVo;
import com.xixi.web.Result;

public interface TeacherCertificateService {
    Result issueCertificate(CertificateIssueDto dto, Long teacherId);

    /**
     * 教师撤销证书并记录撤销原因。
     */
    Result revokeCertificate(Long certificateId, CertificateRevokeDto dto, Long teacherId);

    IPage<CertificateTeacherIssuedVo> getTeacherIssuedPage(CertificateTeacherIssuedQuery query, Long teacherId);

    CertificateDetailVo getTeacherIssuedDetail(Long certificateId, Long teacherId);
}
