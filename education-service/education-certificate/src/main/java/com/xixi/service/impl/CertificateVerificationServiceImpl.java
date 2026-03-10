package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.Certificate;
import com.xixi.entity.CertificateShare;
import com.xixi.entity.CertificateVerification;
import com.xixi.exception.BizException;
import com.xixi.mapper.CertificateMapper;
import com.xixi.mapper.CertificateShareMapper;
import com.xixi.mapper.CertificateVerificationMapper;
import com.xixi.mq.CertificateVerificationChangedEventProducer;
import com.xixi.pojo.dto.certificate.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByQrcodeDto;
import com.xixi.pojo.query.certificate.CertificateVerifyHistoryQuery;
import com.xixi.pojo.vo.certificate.CertificateVerifyBatchResultVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyHistoryVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyReportVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyResultVo;
import com.xixi.service.CertificateVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 证书验证服务实现（7.1~7.5）。
 */
@Service
@RequiredArgsConstructor
public class CertificateVerificationServiceImpl implements CertificateVerificationService {
    private static final String VERIFIER_TYPE_ENTERPRISE = "ENTERPRISE";
    private static final String VERIFIER_TYPE_ADMIN = "ADMIN";
    private static final String VERIFIER_TYPE_PUBLIC = "PUBLIC";

    private static final String VERIFY_METHOD_NUMBER = "NUMBER";
    private static final String VERIFY_METHOD_QRCODE = "QRCODE";
    private static final String VERIFY_METHOD_BATCH = "BATCH";

    private static final String VERIFY_RESULT_VALID = "VALID";
    private static final String VERIFY_RESULT_INVALID = "INVALID";
    private static final String VERIFY_RESULT_REVOKED = "REVOKED";
    private static final String VERIFY_RESULT_EXPIRED = "EXPIRED";

    private static final String CERTIFICATE_STATUS_ISSUED = "ISSUED";
    private static final String CERTIFICATE_STATUS_REVOKED = "REVOKED";
    private static final String CERTIFICATE_STATUS_EXPIRED = "EXPIRED";

    private final CertificateMapper certificateMapper;
    private final CertificateShareMapper certificateShareMapper;
    private final CertificateVerificationMapper certificateVerificationMapper;
    private final CertificateVerificationChangedEventProducer certificateVerificationChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7.1：按证书编号执行验证并记录验证日志")
    public CertificateVerifyResultVo verifyByNumber(
            CertificateVerifyByNumberDto dto,
            Long verifierId,
            Integer verifierRole,
            String ipAddress,
            String userAgent
    ) {
        if (dto == null || !StringUtils.hasText(dto.getCertificateNumber())) {
            throw new BizException(400, "certificateNumber不能为空");
        }
        VerifierContext verifierContext = buildVerifierContext(verifierId, verifierRole, ipAddress, userAgent);
        return verifyByNumberInternal(dto.getCertificateNumber().trim(), verifierContext, VERIFY_METHOD_NUMBER);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7.2：按二维码分享令牌执行验证并记录验证日志")
    public CertificateVerifyResultVo verifyByQrcode(
            CertificateVerifyByQrcodeDto dto,
            Long verifierId,
            Integer verifierRole,
            String ipAddress,
            String userAgent
    ) {
        if (dto == null || !StringUtils.hasText(dto.getShareToken())) {
            throw new BizException(400, "shareToken不能为空");
        }
        String shareToken = dto.getShareToken().trim();
        VerifierContext verifierContext = buildVerifierContext(verifierId, verifierRole, ipAddress, userAgent);

        CertificateShare share = certificateShareMapper.selectByShareToken(shareToken);
        if (share == null) {
            CertificateVerification verification = saveVerificationLog(
                    null,
                    null,
                    verifierContext.verifierId,
                    verifierContext.verifierType,
                    VERIFY_METHOD_QRCODE,
                    VERIFY_RESULT_INVALID,
                    verifierContext.ipAddress,
                    verifierContext.userAgent
            );
            return toVerifyResultVo(verification, null, "二维码无效或不存在");
        }

        Certificate certificate = certificateMapper.selectById(share.getCertificateId());
        String certificateNumber = certificate == null ? null : certificate.getCertificateNumber();
        if (!Boolean.TRUE.equals(share.getIsActive()) || isExpired(share.getExpiryTime())) {
            CertificateVerification verification = saveVerificationLog(
                    share.getCertificateId(),
                    certificateNumber,
                    verifierContext.verifierId,
                    verifierContext.verifierType,
                    VERIFY_METHOD_QRCODE,
                    VERIFY_RESULT_INVALID,
                    verifierContext.ipAddress,
                    verifierContext.userAgent
            );
            if (isExpired(share.getExpiryTime()) && Boolean.TRUE.equals(share.getIsActive())) {
                share.setIsActive(false);
                share.setUpdatedTime(LocalDateTime.now());
                certificateShareMapper.updateById(share);
            }
            return toVerifyResultVo(verification, certificate, "二维码已失效或已过期");
        }

        String verificationResult = resolveVerificationResult(certificate);
        String message = toResultMessage(verificationResult);
        CertificateVerification verification = saveVerificationLog(
                share.getCertificateId(),
                certificateNumber,
                verifierContext.verifierId,
                verifierContext.verifierType,
                VERIFY_METHOD_QRCODE,
                verificationResult,
                verifierContext.ipAddress,
                verifierContext.userAgent
        );
        if (certificate != null) {
            increaseCertificateVerificationStats(certificate.getId());
        }
        return toVerifyResultVo(verification, certificate, message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7.3：批量按证书编号执行验证并返回统计结果")
    public CertificateVerifyBatchResultVo verifyBatch(
            CertificateVerifyBatchDto dto,
            Long verifierId,
            Integer verifierRole,
            String ipAddress,
            String userAgent
    ) {
        if (dto == null || dto.getCertificateNumbers() == null || dto.getCertificateNumbers().isEmpty()) {
            throw new BizException(400, "certificateNumbers不能为空");
        }
        if (dto.getCertificateNumbers().size() > 200) {
            throw new BizException(400, "单次批量验证最多200条");
        }
        VerifierContext verifierContext = buildVerifierContext(verifierId, verifierRole, ipAddress, userAgent);
        List<CertificateVerifyResultVo> results = dto.getCertificateNumbers().stream()
                .map(number -> verifyByNumberInternal(number, verifierContext, VERIFY_METHOD_BATCH))
                .toList();

        CertificateVerifyBatchResultVo vo = new CertificateVerifyBatchResultVo();
        vo.setTotalCount(results.size());
        vo.setValidCount((int) results.stream().filter(item -> VERIFY_RESULT_VALID.equals(item.getVerificationResult())).count());
        vo.setInvalidCount((int) results.stream().filter(item -> VERIFY_RESULT_INVALID.equals(item.getVerificationResult())).count());
        vo.setRevokedCount((int) results.stream().filter(item -> VERIFY_RESULT_REVOKED.equals(item.getVerificationResult())).count());
        vo.setExpiredCount((int) results.stream().filter(item -> VERIFY_RESULT_EXPIRED.equals(item.getVerificationResult())).count());
        vo.setResults(results);
        return vo;
    }

    @Override
    @MethodPurpose("7.4：分页查询验证历史（企业仅可查看自己的历史，管理员可查看全部）")
    public IPage<CertificateVerifyHistoryVo> getVerifyHistory(CertificateVerifyHistoryQuery query, Long verifierId, Integer verifierRole) {
        verifyHistoryPermission(verifierId, verifierRole);
        CertificateVerifyHistoryQuery safeQuery = query == null ? new CertificateVerifyHistoryQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        Boolean isAdmin = RoleConstants.ADMIN == verifierRole.intValue();
        String verificationResult = StringUtils.hasText(safeQuery.getVerificationResult())
                ? safeQuery.getVerificationResult().trim().toUpperCase()
                : null;
        String verificationMethod = StringUtils.hasText(safeQuery.getVerificationMethod())
                ? safeQuery.getVerificationMethod().trim().toUpperCase()
                : null;
        Page<CertificateVerification> entityPage = (Page<CertificateVerification>) certificateVerificationMapper.selectVerifyHistoryPage(
                new Page<>(pageNum, pageSize),
                isAdmin,
                verifierId,
                VERIFIER_TYPE_ENTERPRISE,
                verificationResult,
                verificationMethod
        );
        Map<Long, Certificate> certificateMap = queryCertificateMap(entityPage.getRecords().stream()
                .map(CertificateVerification::getCertificateId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        Page<CertificateVerifyHistoryVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(item -> toHistoryVo(item, certificateMap.get(item.getCertificateId())))
                .toList());
        return voPage;
    }

    @Override
    @MethodPurpose("7.5：查询验证报告详情（企业仅可查看自己的报告，管理员可查看全部）")
    public CertificateVerifyReportVo getVerifyReport(Long verificationId, Long verifierId, Integer verifierRole) {
        verifyHistoryPermission(verifierId, verifierRole);
        if (verificationId == null) {
            throw new BizException(400, "verificationId不能为空");
        }

        CertificateVerification verification = certificateVerificationMapper.selectById(verificationId);
        if (verification == null) {
            throw new BizException(404, "验证记录不存在");
        }
        if (RoleConstants.ENTERPRISE == verifierRole.intValue()) {
            if (!Objects.equals(verification.getVerifierId(), verifierId)
                    || !VERIFIER_TYPE_ENTERPRISE.equals(verification.getVerifierType())) {
                throw new BizException(403, "无权限查看该验证报告");
            }
        }

        Certificate certificate = verification.getCertificateId() == null ? null : certificateMapper.selectById(verification.getCertificateId());
        return toReportVo(verification, certificate);
    }

    @MethodPurpose("按证书编号执行验证并落库日志")
    private CertificateVerifyResultVo verifyByNumberInternal(String certificateNumber, VerifierContext verifierContext, String verificationMethod) {
        if (!StringUtils.hasText(certificateNumber)) {
            CertificateVerification verification = saveVerificationLog(
                    null,
                    null,
                    verifierContext.verifierId,
                    verifierContext.verifierType,
                    verificationMethod,
                    VERIFY_RESULT_INVALID,
                    verifierContext.ipAddress,
                    verifierContext.userAgent
            );
            return toVerifyResultVo(verification, null, "证书编号为空");
        }
        String normalizedNumber = certificateNumber.trim();
        Certificate certificate = certificateMapper.selectByCertificateNumber(normalizedNumber);
        String result = resolveVerificationResult(certificate);
        CertificateVerification verification = saveVerificationLog(
                certificate == null ? null : certificate.getId(),
                normalizedNumber,
                verifierContext.verifierId,
                verifierContext.verifierType,
                verificationMethod,
                result,
                verifierContext.ipAddress,
                verifierContext.userAgent
        );
        if (certificate != null) {
            increaseCertificateVerificationStats(certificate.getId());
        }
        return toVerifyResultVo(verification, certificate, toResultMessage(result));
    }

    @MethodPurpose("构造验证者上下文（支持企业、管理员、公众）")
    private VerifierContext buildVerifierContext(Long verifierId, Integer verifierRole, String ipAddress, String userAgent) {
        if (verifierRole == null) {
            return new VerifierContext(null, VERIFIER_TYPE_PUBLIC, ipAddress, userAgent);
        }
        if (RoleConstants.ENTERPRISE == verifierRole) {
            return new VerifierContext(verifierId, VERIFIER_TYPE_ENTERPRISE, ipAddress, userAgent);
        }
        if (RoleConstants.ADMIN == verifierRole) {
            return new VerifierContext(verifierId, VERIFIER_TYPE_ADMIN, ipAddress, userAgent);
        }
        throw new BizException(403, "当前角色不支持证书验证");
    }

    @MethodPurpose("校验验证历史和报告访问权限")
    private void verifyHistoryPermission(Long verifierId, Integer verifierRole) {
        if (verifierRole == null) {
            throw new BizException(401, "未登录或角色缺失");
        }
        if (RoleConstants.ADMIN != verifierRole && RoleConstants.ENTERPRISE != verifierRole) {
            throw new BizException(403, "仅企业或管理员可访问该资源");
        }
        if (RoleConstants.ENTERPRISE == verifierRole && verifierId == null) {
            throw new BizException(401, "企业用户ID缺失");
        }
    }

    @MethodPurpose("解析证书验证结果")
    private String resolveVerificationResult(Certificate certificate) {
        if (certificate == null) {
            return VERIFY_RESULT_INVALID;
        }
        if (CERTIFICATE_STATUS_REVOKED.equalsIgnoreCase(certificate.getStatus())) {
            return VERIFY_RESULT_REVOKED;
        }
        if (CERTIFICATE_STATUS_EXPIRED.equalsIgnoreCase(certificate.getStatus())
                || isExpired(certificate.getExpiryDate())) {
            return VERIFY_RESULT_EXPIRED;
        }
        if (!CERTIFICATE_STATUS_ISSUED.equalsIgnoreCase(certificate.getStatus())) {
            return VERIFY_RESULT_INVALID;
        }
        return VERIFY_RESULT_VALID;
    }

    @MethodPurpose("判断时间戳是否已过期")
    private boolean isExpired(LocalDateTime expiryTime) {
        return expiryTime != null && expiryTime.isBefore(LocalDateTime.now());
    }

    @MethodPurpose("判断日期是否已过期")
    private boolean isExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    @MethodPurpose("写入验证日志并发布验证事件")
    private CertificateVerification saveVerificationLog(
            Long certificateId,
            String certificateNumber,
            Long verifierId,
            String verifierType,
            String verificationMethod,
            String verificationResult,
            String ipAddress,
            String userAgent
    ) {
        CertificateVerification verification = new CertificateVerification();
        verification.setCertificateId(certificateId);
        verification.setCertificateNumber(certificateNumber);
        verification.setVerifierId(verifierId);
        verification.setVerifierType(verifierType);
        verification.setVerificationMethod(verificationMethod);
        verification.setVerificationResult(verificationResult);
        verification.setVerificationTime(LocalDateTime.now());
        verification.setIpAddress(trimToNull(ipAddress));
        verification.setUserAgent(trimToNull(userAgent));
        verification.setCreatedTime(LocalDateTime.now());
        certificateVerificationMapper.insert(verification);

        certificateVerificationChangedEventProducer.publish(
                verification.getId(),
                verification.getCertificateId(),
                verification.getCertificateNumber(),
                verification.getVerificationMethod(),
                verification.getVerificationResult(),
                verification.getVerifierType()
        );
        return verification;
    }

    @MethodPurpose("累加证书验证次数并更新最后验证时间")
    private void increaseCertificateVerificationStats(Long certificateId) {
        if (certificateId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        certificateMapper.increaseVerificationStats(certificateId, now, now);
    }

    @MethodPurpose("批量查询证书映射")
    private Map<Long, Certificate> queryCertificateMap(List<Long> certificateIds) {
        if (certificateIds == null || certificateIds.isEmpty()) {
            return Map.of();
        }
        return certificateMapper.selectByIds(certificateIds)
                .stream()
                .collect(Collectors.toMap(Certificate::getId, Function.identity(), (a, b) -> a));
    }

    @MethodPurpose("构建单次验证结果视图对象")
    private CertificateVerifyResultVo toVerifyResultVo(CertificateVerification verification, Certificate certificate, String message) {
        CertificateVerifyResultVo vo = new CertificateVerifyResultVo();
        vo.setVerificationId(verification.getId());
        vo.setVerificationMethod(verification.getVerificationMethod());
        vo.setVerificationResult(verification.getVerificationResult());
        vo.setVerificationTime(verification.getVerificationTime());
        vo.setVerifierType(verification.getVerifierType());
        vo.setCertificateId(verification.getCertificateId());
        vo.setCertificateNumber(verification.getCertificateNumber());
        vo.setMessage(message);
        if (certificate != null) {
            vo.setCertificateName(certificate.getCertificateName());
            vo.setCertificateStatus(certificate.getStatus());
            vo.setIssuingDate(certificate.getIssuingDate());
            vo.setExpiryDate(certificate.getExpiryDate());
            vo.setHash(certificate.getHash());
            vo.setBlockHeight(certificate.getBlockHeight());
            vo.setTransactionHash(certificate.getTransactionHash());
        }
        return vo;
    }

    @MethodPurpose("构建验证历史分页项")
    private CertificateVerifyHistoryVo toHistoryVo(CertificateVerification verification, Certificate certificate) {
        CertificateVerifyHistoryVo vo = new CertificateVerifyHistoryVo();
        vo.setId(verification.getId());
        vo.setCertificateId(verification.getCertificateId());
        vo.setCertificateNumber(verification.getCertificateNumber());
        vo.setVerifierId(verification.getVerifierId());
        vo.setVerifierType(verification.getVerifierType());
        vo.setVerificationMethod(verification.getVerificationMethod());
        vo.setVerificationResult(verification.getVerificationResult());
        vo.setVerificationTime(verification.getVerificationTime());
        vo.setIpAddress(verification.getIpAddress());
        if (certificate != null) {
            vo.setCertificateName(certificate.getCertificateName());
        }
        return vo;
    }

    @MethodPurpose("构建验证报告详情对象")
    private CertificateVerifyReportVo toReportVo(CertificateVerification verification, Certificate certificate) {
        CertificateVerifyReportVo vo = new CertificateVerifyReportVo();
        vo.setVerificationId(verification.getId());
        vo.setCertificateId(verification.getCertificateId());
        vo.setCertificateNumber(verification.getCertificateNumber());
        vo.setVerifierId(verification.getVerifierId());
        vo.setVerifierType(verification.getVerifierType());
        vo.setVerificationMethod(verification.getVerificationMethod());
        vo.setVerificationResult(verification.getVerificationResult());
        vo.setVerificationTime(verification.getVerificationTime());
        vo.setIpAddress(verification.getIpAddress());
        vo.setUserAgent(verification.getUserAgent());
        if (certificate != null) {
            vo.setCertificateName(certificate.getCertificateName());
            vo.setCertificateStatus(certificate.getStatus());
            vo.setIssuingDate(certificate.getIssuingDate());
            vo.setExpiryDate(certificate.getExpiryDate());
            vo.setIssuingAuthority(certificate.getIssuingAuthority());
            vo.setHash(certificate.getHash());
            vo.setBlockHeight(certificate.getBlockHeight());
            vo.setTransactionHash(certificate.getTransactionHash());
        }
        return vo;
    }

    @MethodPurpose("将验证结果枚举转换为提示语")
    private String toResultMessage(String verificationResult) {
        if (VERIFY_RESULT_VALID.equals(verificationResult)) {
            return "证书验证通过";
        }
        if (VERIFY_RESULT_REVOKED.equals(verificationResult)) {
            return "证书已撤销";
        }
        if (VERIFY_RESULT_EXPIRED.equals(verificationResult)) {
            return "证书已过期";
        }
        return "证书无效";
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 验证调用上下文。
     */
    private record VerifierContext(Long verifierId, String verifierType, String ipAddress, String userAgent) {
    }
}
