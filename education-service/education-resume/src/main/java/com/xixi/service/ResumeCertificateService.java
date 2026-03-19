package com.xixi.service;

import com.xixi.entity.ResumeCertificate;
import com.xixi.pojo.dto.resume.ResumeCertificateBindDto;
import com.xixi.web.Result;

import java.util.List;

public interface ResumeCertificateService {
    Result bindCertificate(ResumeCertificateBindDto dto, Long studentId);

    Result unbindCertificate(Long id, Long studentId);

    List<ResumeCertificate> listCertificates(Long resumeId, Long studentId);
}
