package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Certificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.CertificateMapper;
import com.xixi.pojo.dto.certificate.CertificateInternalIssueFromGradeDto;
import com.xixi.pojo.dto.certificate.CertificateInternalValidateIdsDto;
import com.xixi.pojo.dto.certificate.CertificateIssueDto;
import com.xixi.pojo.vo.certificate.CertificateIdValidationItemVo;
import com.xixi.pojo.vo.certificate.CertificateInternalStudentVo;
import com.xixi.pojo.vo.certificate.CertificateInternalValidateIdsVo;
import com.xixi.service.CertificateInternalService;
import com.xixi.service.TeacherCertificateService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 证书内部接口服务实现（10.1~10.3）。
 */
@Service
@RequiredArgsConstructor
public class CertificateInternalServiceImpl implements CertificateInternalService {
    private static final String STATUS_ISSUED = "ISSUED";
    private static final String DEFAULT_CERTIFICATE_NAME = "课程结业证书";

    private final TeacherCertificateService teacherCertificateService;
    private final CertificateMapper certificateMapper;

    @Override
    @MethodPurpose("10.1：学习服务触发成绩达标发证，复用教师发证逻辑")
    public Result issueFromGrade(CertificateInternalIssueFromGradeDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId() <= 0) {
            throw new BizException(400, "studentId不能为空");
        }
        if (dto.getCourseId() == null || dto.getCourseId() <= 0) {
            throw new BizException(400, "courseId不能为空");
        }
        if (dto.getTeacherId() == null || dto.getTeacherId() <= 0) {
            throw new BizException(400, "teacherId不能为空");
        }

        CertificateIssueDto issueDto = new CertificateIssueDto();
        issueDto.setStudentId(dto.getStudentId());
        issueDto.setCourseId(dto.getCourseId());
        issueDto.setTeacherId(dto.getTeacherId());
        issueDto.setCertificateName(StringUtils.hasText(dto.getCertificateName()) ? dto.getCertificateName() : DEFAULT_CERTIFICATE_NAME);
        issueDto.setIssuingAuthority(dto.getIssuingAuthority());
        issueDto.setIssuingDate(dto.getIssuingDate() == null ? LocalDate.now() : dto.getIssuingDate());
        issueDto.setExpiryDate(dto.getExpiryDate());
        issueDto.setCertificateContent(dto.getCertificateContent());
        issueDto.setMetadataJson(buildMetadataJson(dto));
        issueDto.setFileUrl(dto.getFileUrl());
        issueDto.setThumbnailUrl(dto.getThumbnailUrl());
        return teacherCertificateService.issueCertificate(issueDto, dto.getTeacherId());
    }

    @Override
    @MethodPurpose("10.2：内部查询学生证书列表")
    public List<CertificateInternalStudentVo> listStudentCertificates(Long studentId, String status) {
        if (studentId == null || studentId <= 0) {
            throw new BizException(400, "studentId不能为空");
        }
        String normalizedStatus = StringUtils.hasText(status) ? status.trim().toUpperCase() : null;

        return certificateMapper.selectByStudentIdAndStatus(studentId, normalizedStatus).stream()
                .map(item -> BeanUtil.copyProperties(item, CertificateInternalStudentVo.class))
                .toList();
    }

    @Override
    @MethodPurpose("10.3：内部批量校验证书ID是否有效且归属学生")
    public CertificateInternalValidateIdsVo validateCertificateIds(CertificateInternalValidateIdsDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId() <= 0) {
            throw new BizException(400, "studentId不能为空");
        }
        List<Long> certificateIds = normalizeCertificateIds(dto.getCertificateIds());
        if (certificateIds.isEmpty()) {
            CertificateInternalValidateIdsVo emptyVo = new CertificateInternalValidateIdsVo();
            emptyVo.setTotalCount(0);
            emptyVo.setValidCount(0);
            emptyVo.setInvalidCount(0);
            emptyVo.setValidIds(Collections.emptyList());
            emptyVo.setInvalidItems(Collections.emptyList());
            return emptyVo;
        }

        Map<Long, Certificate> certificateMap = certificateMapper.selectByIds(certificateIds)
                .stream()
                .collect(Collectors.toMap(Certificate::getId, Function.identity(), (a, b) -> a));

        boolean requireIssued = dto.getRequireIssued() == null || dto.getRequireIssued();
        List<Long> validIds = new ArrayList<>();
        List<CertificateIdValidationItemVo> invalidItems = new ArrayList<>();

        for (Long certificateId : certificateIds) {
            Certificate certificate = certificateMap.get(certificateId);
            if (certificate == null) {
                invalidItems.add(invalidItem(certificateId, "NOT_FOUND", null, null));
                continue;
            }
            if (!Objects.equals(certificate.getStudentId(), dto.getStudentId())) {
                invalidItems.add(invalidItem(certificateId, "OWNER_MISMATCH", certificate.getStudentId(), certificate.getStatus()));
                continue;
            }
            if (requireIssued && !STATUS_ISSUED.equalsIgnoreCase(certificate.getStatus())) {
                invalidItems.add(invalidItem(certificateId, "STATUS_INVALID", certificate.getStudentId(), certificate.getStatus()));
                continue;
            }
            validIds.add(certificateId);
        }

        CertificateInternalValidateIdsVo vo = new CertificateInternalValidateIdsVo();
        vo.setTotalCount(certificateIds.size());
        vo.setValidCount(validIds.size());
        vo.setInvalidCount(invalidItems.size());
        vo.setValidIds(validIds);
        vo.setInvalidItems(invalidItems);
        return vo;
    }

    @MethodPurpose("构建内部发证元数据JSON")
    private String buildMetadataJson(CertificateInternalIssueFromGradeDto dto) {
        if (StringUtils.hasText(dto.getMetadataJson())) {
            return dto.getMetadataJson().trim();
        }
        if (dto.getFinalScore() == null) {
            return null;
        }
        return JSONUtil.toJsonStr(Map.of("finalScore", dto.getFinalScore()));
    }

    @MethodPurpose("清洗并去重证书ID列表")
    private List<Long> normalizeCertificateIds(List<Long> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long rawId : rawIds) {
            if (rawId != null && rawId > 0) {
                set.add(rawId);
            }
        }
        return new ArrayList<>(set);
    }

    @MethodPurpose("构建无效证书ID结果项")
    private CertificateIdValidationItemVo invalidItem(Long certificateId, String reason, Long ownerStudentId, String status) {
        CertificateIdValidationItemVo item = new CertificateIdValidationItemVo();
        item.setCertificateId(certificateId);
        item.setValid(false);
        item.setReason(reason);
        item.setOwnerStudentId(ownerStudentId);
        item.setStatus(status);
        return item;
    }
}
