package com.xixi.service;

import com.xixi.pojo.dto.certificate.CertificateInternalIssueFromGradeDto;
import com.xixi.pojo.dto.certificate.CertificateInternalValidateIdsDto;
import com.xixi.pojo.vo.certificate.CertificateInternalStudentVo;
import com.xixi.pojo.vo.certificate.CertificateInternalValidateIdsVo;
import com.xixi.web.Result;

import java.util.List;

public interface CertificateInternalService {
    Result issueFromGrade(CertificateInternalIssueFromGradeDto dto);

    List<CertificateInternalStudentVo> listStudentCertificates(Long studentId, String status);

    CertificateInternalValidateIdsVo validateCertificateIds(CertificateInternalValidateIdsDto dto);
}

