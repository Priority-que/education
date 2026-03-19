package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.BlockchainRecord;
import com.xixi.entity.Certificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.BlockchainRecordMapper;
import com.xixi.mapper.CertificateMapper;
import com.xixi.mq.CertificateChangedEventProducer;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.pojo.dto.certificate.CertificateIssueDto;
import com.xixi.pojo.dto.certificate.CertificateRevokeDto;
import com.xixi.pojo.query.certificate.CertificateTeacherIssuedQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateTeacherIssuedVo;
import com.xixi.service.TeacherCertificateService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 教师证书管理服务实现（8.1~8.3）。
 */
@Service
@RequiredArgsConstructor
public class TeacherCertificateServiceImpl implements TeacherCertificateService {
    private static final String STATUS_ISSUED = "ISSUED";
    private static final String STATUS_REVOKED = "REVOKED";
    private static final String EVENT_CODE_CERTIFICATE_ISSUED = "CERTIFICATE_ISSUED";
    private static final String EVENT_CODE_CERTIFICATE_REVOKED = "CERTIFICATE_REVOKED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String MESSAGE_TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String RELATED_TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String DELIVER_MODE_MQ = "MQ";
    private static final String DEFAULT_ISSUING_AUTHORITY = "教育区块链平台";
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMM");

    private final CertificateMapper certificateMapper;
    private final BlockchainRecordMapper blockchainRecordMapper;
    private final CertificateChangedEventProducer certificateChangedEventProducer;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final EducationUserStudentClient educationUserStudentClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("8.1：教师手动颁发证书")
    public Result issueCertificate(CertificateIssueDto dto, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        validateIssueDto(dto, validTeacherId);

        Long issuedCount = certificateMapper.countByStudentAndCourseAndStatus(dto.getStudentId(), dto.getCourseId(), STATUS_ISSUED);
        if (issuedCount != null && issuedCount > 0) {
            throw new BizException(409, "该学生在当前课程已存在有效证书");
        }

        String certificateNumber = generateUniqueCertificateNumber(dto.getCourseId());
        String previousHash = queryLatestCertificateHash();
        String certificateHash = buildCertificateHash(certificateNumber, dto.getStudentId(), dto.getCourseId(), validTeacherId);

        Certificate certificate = new Certificate();
        certificate.setCertificateNumber(certificateNumber);
        certificate.setStudentId(dto.getStudentId());
        certificate.setCourseId(dto.getCourseId());
        certificate.setTeacherId(validTeacherId);
        certificate.setCertificateName(dto.getCertificateName().trim());
        certificate.setIssuingAuthority(StringUtils.hasText(dto.getIssuingAuthority())
                ? dto.getIssuingAuthority().trim()
                : DEFAULT_ISSUING_AUTHORITY);
        certificate.setIssuingDate(dto.getIssuingDate() == null ? LocalDate.now() : dto.getIssuingDate());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setCertificateContent(trimToNull(dto.getCertificateContent()));
        certificate.setMetadataJson(trimToNull(dto.getMetadataJson()));
        certificate.setFileUrl(trimToNull(dto.getFileUrl()));
        certificate.setThumbnailUrl(trimToNull(dto.getThumbnailUrl()));
        certificate.setHash(certificateHash);
        certificate.setPreviousHash(previousHash);
        certificate.setStatus(STATUS_ISSUED);
        certificate.setVerificationCount(0);
        certificate.setCreatedTime(LocalDateTime.now());
        certificate.setUpdatedTime(LocalDateTime.now());
        certificateMapper.insert(certificate);

        certificateChangedEventProducer.publish(
                CertificateChangedEventProducer.EVENT_ISSUE,
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getStudentId(),
                certificate.getTeacherId(),
                certificate.getStatus()
        );
        triggerCertificateIssuedEvent(certificate, validTeacherId);
        return Result.success("证书颁发成功", Map.of(
                "certificateId", certificate.getId(),
                "certificateNumber", certificate.getCertificateNumber()
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("8.2：教师撤销已颁发证书")
    public Result revokeCertificate(Long certificateId, CertificateRevokeDto dto, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        if (certificateId == null) {
            throw new BizException(400, "certificateId不能为空");
        }

        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!Objects.equals(certificate.getTeacherId(), validTeacherId)) {
            throw new BizException(403, "无权限撤销非本人课程证书");
        }
        if (STATUS_REVOKED.equalsIgnoreCase(certificate.getStatus())) {
            return Result.success("证书已是撤销状态");
        }

        String revokeReason = requireRevokeReason(dto);
        LocalDateTime now = LocalDateTime.now();
        certificate.setStatus(STATUS_REVOKED);
        certificate.setMetadataJson(appendRevokeReasonToMetadata(certificate.getMetadataJson(), revokeReason, validTeacherId, now));
        certificate.setUpdatedTime(now);
        certificateMapper.updateById(certificate);

        certificateChangedEventProducer.publish(
                CertificateChangedEventProducer.EVENT_REVOKE,
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getStudentId(),
                certificate.getTeacherId(),
                certificate.getStatus()
        );
        triggerCertificateRevokedEvent(certificate, revokeReason, validTeacherId);
        return Result.success("证书撤销成功");
    }

    @Override
    @MethodPurpose("8.3：分页查询教师已颁发证书列表")
    public IPage<CertificateTeacherIssuedVo> getTeacherIssuedPage(CertificateTeacherIssuedQuery query, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        CertificateTeacherIssuedQuery safeQuery = query == null ? new CertificateTeacherIssuedQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        String status = StringUtils.hasText(safeQuery.getStatus()) ? safeQuery.getStatus().trim().toUpperCase() : null;
        Page<Certificate> entityPage = (Page<Certificate>) certificateMapper.selectTeacherIssuedPage(
                new Page<>(pageNum, pageSize),
                validTeacherId,
                safeQuery.getCourseId(),
                status
        );
        Page<CertificateTeacherIssuedVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, CertificateTeacherIssuedVo.class))
                .toList());
        return voPage;
    }

    @Override
    @MethodPurpose("8.3-扩展：教师查看自己已颁发证书详情")
    public CertificateDetailVo getTeacherIssuedDetail(Long certificateId, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        Certificate certificate = requireTeacherOwnedCertificate(certificateId, validTeacherId);
        return toDetailVo(certificate);
    }

    @MethodPurpose("校验并返回当前教师ID")
    private Long requireTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return teacherId;
    }

    @MethodPurpose("按证书ID查询并校验教师归属")
    private Certificate requireTeacherOwnedCertificate(Long certificateId, Long teacherId) {
        if (certificateId == null) {
            throw new BizException(400, "证书ID不能为空");
        }
        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!Objects.equals(certificate.getTeacherId(), teacherId)) {
            throw new BizException(403, "无权限查看他人证书");
        }
        return certificate;
    }

    @MethodPurpose("校验颁发证书请求参数")
    private void validateIssueDto(CertificateIssueDto dto, Long teacherId) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId() <= 0) {
            throw new BizException(400, "studentId不能为空");
        }
        if (dto.getCourseId() == null || dto.getCourseId() <= 0) {
            throw new BizException(400, "courseId不能为空");
        }
        if (dto.getTeacherId() != null && !Objects.equals(dto.getTeacherId(), teacherId)) {
            throw new BizException(403, "teacherId与当前登录教师不一致");
        }
        if (!StringUtils.hasText(dto.getCertificateName())) {
            throw new BizException(400, "certificateName不能为空");
        }
        if (dto.getExpiryDate() != null && dto.getIssuingDate() != null && dto.getExpiryDate().isBefore(dto.getIssuingDate())) {
            throw new BizException(400, "expiryDate不能早于issuingDate");
        }
    }

    @MethodPurpose("生成唯一证书编号")
    private String generateUniqueCertificateNumber(Long courseId) {
        String datePart = LocalDate.now().format(NUMBER_DATE);
        for (int i = 0; i < 10; i++) {
            String candidate = "CERT-" + datePart + "-" + courseId + "-" + RandomUtil.randomStringUpper(6);
            Long count = certificateMapper.countByCertificateNumber(candidate);
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new BizException(500, "证书编号生成失败，请重试");
    }

    @MethodPurpose("将证书实体转换为详情视图对象并补全区块链记录")
    private CertificateDetailVo toDetailVo(Certificate certificate) {
        CertificateDetailVo detailVo = BeanUtil.copyProperties(certificate, CertificateDetailVo.class);
        if (certificate.getBlockHeight() != null) {
            BlockchainRecord blockchainRecord = blockchainRecordMapper.selectByBlockHeight(certificate.getBlockHeight());
            detailVo.setBlockchainRecord(blockchainRecord);
        }
        return detailVo;
    }

    @MethodPurpose("查询最近一条证书哈希作为前置哈希")
    private String queryLatestCertificateHash() {
        return certificateMapper.selectLatestHash();
    }

    @MethodPurpose("构建证书哈希值")
    private String buildCertificateHash(String certificateNumber, Long studentId, Long courseId, Long teacherId) {
        String payload = certificateNumber + "|" + studentId + "|" + courseId + "|" + teacherId + "|" + System.nanoTime();
        return DigestUtil.sha256Hex(payload);
    }

    @MethodPurpose("校验撤销参数并返回撤销原因")
    private String requireRevokeReason(CertificateRevokeDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getRevokeReason())) {
            throw new BizException(400, "revokeReason不能为空");
        }
        return dto.getRevokeReason().trim();
    }

    @MethodPurpose("将撤销原因写入证书元数据")
    private String appendRevokeReasonToMetadata(String metadataJson, String revokeReason, Long teacherId, LocalDateTime revokeTime) {
        JSONObject metadata = parseMetadata(metadataJson);
        metadata.set("revokeReason", revokeReason);
        metadata.set("revokedByTeacherId", teacherId);
        metadata.set("revokedTime", revokeTime.toString());
        return metadata.toString();
    }

    @MethodPurpose("解析证书元数据，解析失败时保留原始内容")
    private JSONObject parseMetadata(String metadataJson) {
        if (!StringUtils.hasText(metadataJson)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(metadataJson);
        } catch (Exception ignore) {
            JSONObject metadata = new JSONObject();
            metadata.set("originalMetadata", metadataJson);
            return metadata;
        }
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @MethodPurpose("触发证书颁发通知事件")
    private void triggerCertificateIssuedEvent(Certificate certificate, Long teacherId) {
        if (certificate == null || certificate.getId() == null || certificate.getStudentId() == null) {
            return;
        }
        Long studentUserId = resolveStudentUserId(certificate.getStudentId());
        if (studentUserId == null || studentUserId <= 0) {
            return;
        }

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildCertificateEventId(EVENT_CODE_CERTIFICATE_ISSUED, certificate.getId()));
        payload.setEventCode(EVENT_CODE_CERTIFICATE_ISSUED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(studentUserId));
        payload.setParams(Map.of(
                "certificate_name", certificate.getCertificateName() == null ? "" : certificate.getCertificateName(),
                "certificate_number", certificate.getCertificateNumber() == null ? "" : certificate.getCertificateNumber(),
                "issuing_date", certificate.getIssuingDate() == null ? "" : certificate.getIssuingDate()
        ));
        payload.setMessageType(MESSAGE_TYPE_CERTIFICATE);
        payload.setRelatedId(certificate.getId());
        payload.setRelatedType(RELATED_TYPE_CERTIFICATE);
        payload.setPriority(1);
        payload.setDeliverMode(DELIVER_MODE_MQ);
        payload.setOperatorId(teacherId);
        payload.setOperatorRole(RoleConstants.TEACHER);

        Result result = educationMessageInternalClient.triggerEvent(teacherId, RoleConstants.TEACHER, payload);
        requireTriggerSuccess(result, "触发证书颁发通知失败");
    }

    @MethodPurpose("触发证书撤销通知事件")
    private void triggerCertificateRevokedEvent(Certificate certificate, String revokeReason, Long teacherId) {
        if (certificate == null || certificate.getId() == null || certificate.getStudentId() == null) {
            return;
        }
        Long studentUserId = resolveStudentUserId(certificate.getStudentId());
        if (studentUserId == null || studentUserId <= 0) {
            return;
        }

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildCertificateEventId(EVENT_CODE_CERTIFICATE_REVOKED, certificate.getId()));
        payload.setEventCode(EVENT_CODE_CERTIFICATE_REVOKED);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(studentUserId));
        payload.setParams(Map.of(
                "certificate_name", certificate.getCertificateName() == null ? "" : certificate.getCertificateName(),
                "certificate_number", certificate.getCertificateNumber() == null ? "" : certificate.getCertificateNumber(),
                "revoke_reason", revokeReason == null ? "" : revokeReason
        ));
        payload.setMessageType(MESSAGE_TYPE_CERTIFICATE);
        payload.setRelatedId(certificate.getId());
        payload.setRelatedType(RELATED_TYPE_CERTIFICATE);
        payload.setPriority(1);
        payload.setDeliverMode(DELIVER_MODE_MQ);
        payload.setOperatorId(teacherId);
        payload.setOperatorRole(RoleConstants.TEACHER);

        Result result = educationMessageInternalClient.triggerEvent(teacherId, RoleConstants.TEACHER, payload);
        requireTriggerSuccess(result, "触发证书撤销通知失败");
    }

    @MethodPurpose("按studentId解析userId")
    @SuppressWarnings("unchecked")
    private Long resolveStudentUserId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return null;
        }
        Result studentResult = educationUserStudentClient.getStudentById(studentId);
        if (studentResult == null || studentResult.getCode() == null || studentResult.getCode() != 200 || studentResult.getData() == null) {
            return null;
        }
        Object data = studentResult.getData();
        if (!(data instanceof Map<?, ?> dataMap)) {
            return null;
        }
        Object userId = dataMap.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @MethodPurpose("触发事件结果校验")
    private void requireTriggerSuccess(Result result, String message) {
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(500, message);
        }
    }

    @MethodPurpose("构建证书事件ID")
    private String buildCertificateEventId(String eventCode, Long certificateId) {
        return eventCode + "_" + certificateId + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
