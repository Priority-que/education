package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.EnterpriseVerification;
import com.xixi.entity.Enterprises;
import com.xixi.entity.Users;
import com.xixi.exception.BizException;
import com.xixi.mapper.EnterpriseVerificationMapper;
import com.xixi.mapper.EnterprisesMapper;
import com.xixi.mapper.UsersMapper;
import com.xixi.openfeign.admin.EducationAdminInternalClient;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.pojo.dto.EnterpriseVerificationApplyDto;
import com.xixi.pojo.dto.EnterpriseVerificationAuditDto;
import com.xixi.pojo.query.EnterpriseVerificationHistoryQuery;
import com.xixi.pojo.vo.EnterpriseVerificationVo;
import com.xixi.service.EnterpriseVerificationService;
import com.xixi.service.RedisTokenService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseVerificationServiceImpl implements EnterpriseVerificationService {
    private static final String AUDIT_TYPE_ENTERPRISE = "ENTERPRISE";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    private static final String MESSAGE_RELATED_TYPE_ENTERPRISE_VERIFICATION = "ENTERPRISE_VERIFICATION";
    private static final String EVENT_CODE_ENTERPRISE_VERIFICATION_APPROVED = "ENTERPRISE_VERIFICATION_APPROVED";
    private static final String EVENT_CODE_ENTERPRISE_VERIFICATION_REJECTED = "ENTERPRISE_VERIFICATION_REJECTED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String DELIVER_MODE_MQ = "MQ";
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final EnterpriseVerificationMapper enterpriseVerificationMapper;
    private final EnterprisesMapper enterprisesMapper;
    private final UsersMapper usersMapper;
    private final EducationAdminInternalClient educationAdminInternalClient;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final RedisTokenService redisTokenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result apply(EnterpriseVerificationApplyDto dto, Long userId) {
        Enterprises enterprise = ensureEnterpriseByUserId(userId, dto);
        EnterpriseVerification current = enterpriseVerificationMapper.selectCurrentByEnterprise(enterprise.getId());
        if (current != null && STATUS_PENDING.equals(current.getStatus())) {
            createOrSyncAdminAuditRecordOnApply(current, enterprise, userId);
            return Result.success("already has pending application", toVo(current));
        }

        LocalDateTime now = LocalDateTime.now();
        EnterpriseVerification verification = new EnterpriseVerification();
        verification.setEnterpriseId(enterprise.getId());
        verification.setApplicationNo(generateApplicationNo());
        verification.setApplyContent(buildApplyContent(dto, enterprise));
        verification.setStatus(STATUS_PENDING);
        verification.setSubmittedTime(now);
        verification.setCreatedTime(now);
        verification.setUpdatedTime(now);
        enterpriseVerificationMapper.insert(verification);

        enterprise.setVerificationStatus(1);
        enterprise.setUpdatedTime(now);
        enterprisesMapper.updateById(enterprise);
        createOrSyncAdminAuditRecordOnApply(verification, enterprise, userId);
        return Result.success("apply success", toVo(verification));
    }

    @Override
    public EnterpriseVerificationVo current(Long userId) {
        Enterprises enterprise = findEnterpriseByUserId(userId);
        if (enterprise == null) {
            return null;
        }
        EnterpriseVerification verification = enterpriseVerificationMapper.selectCurrentByEnterprise(enterprise.getId());
        return verification == null ? null : toVo(verification);
    }

    @Override
    public IPage<EnterpriseVerificationVo> historyMyPage(EnterpriseVerificationHistoryQuery query, Long userId) {
        Enterprises enterprise = findEnterpriseByUserId(userId);
        if (enterprise == null) {
            EnterpriseVerificationHistoryQuery safeQuery = query == null ? new EnterpriseVerificationHistoryQuery() : query;
            long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
            long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
            return new Page<>(pageNum, pageSize, 0);
        }
        return historyPageByEnterprise(query, enterprise.getId());
    }

    @Override
    public IPage<EnterpriseVerificationVo> historyAdminPage(EnterpriseVerificationHistoryQuery query, Long userId, Integer userRole) {
        requireAdminContext(userId, userRole);
        return historyPageByEnterprise(query, null);
    }

    @Override
    public IPage<EnterpriseVerificationVo> historyPage(EnterpriseVerificationHistoryQuery query, Long userId, Integer userRole) {
        if (hasAdminPermission(userId, userRole)) {
            return historyAdminPage(query, userId, userRole);
        }
        return historyMyPage(query, userId);
    }

    private IPage<EnterpriseVerificationVo> historyPageByEnterprise(EnterpriseVerificationHistoryQuery query, Long enterpriseId) {
        EnterpriseVerificationHistoryQuery safeQuery = query == null ? new EnterpriseVerificationHistoryQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();

        IPage<EnterpriseVerification> entityPage = enterpriseVerificationMapper.selectHistoryPage(
                new Page<>(pageNum, pageSize),
                enterpriseId
        );
        Page<EnterpriseVerificationVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<EnterpriseVerificationVo> records = entityPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result audit(Long applicationId, EnterpriseVerificationAuditDto dto, Long auditorId, Integer auditorRole) {
        requireAdminAuditContext(auditorId, auditorRole);
        if (applicationId == null) {
            throw new BizException(400, "applicationId cannot be null");
        }
        if (dto == null || !StringUtils.hasText(dto.getStatus())) {
            throw new BizException(400, "status cannot be null");
        }

        String targetStatus = normalizeStatus(dto.getStatus());
        if (!STATUS_APPROVED.equals(targetStatus) && !STATUS_REJECTED.equals(targetStatus)) {
            throw new BizException(400, "status only supports APPROVED or REJECTED");
        }

        EnterpriseVerification verification = enterpriseVerificationMapper.selectById(applicationId);
        if (verification == null) {
            throw new BizException(404, "application not found");
        }
        if (!STATUS_PENDING.equals(verification.getStatus())) {
            throw new BizException(409, "application already audited");
        }

        LocalDateTime now = LocalDateTime.now();
        verification.setStatus(targetStatus);
        verification.setAuditReason(trimToNull(dto.getAuditReason()));
        verification.setAuditedTime(now);
        verification.setAuditorId(auditorId);
        verification.setUpdatedTime(now);
        enterpriseVerificationMapper.updateById(verification);
        syncAdminAuditRecordOnDecision(verification, auditorId);

        Enterprises enterprise = enterprisesMapper.selectById(verification.getEnterpriseId());
        if (enterprise != null) {
            enterprise.setVerificationStatus(STATUS_APPROVED.equals(targetStatus) ? 2 : 0);
            enterprise.setUpdatedTime(now);
            enterprisesMapper.updateById(enterprise);
        }

        if (STATUS_APPROVED.equals(targetStatus)) {
            if (enterprise == null || enterprise.getUserId() == null) {
                throw new BizException(500, "enterprise mapping not found");
            }
            switchRoleAndForceRelogin(enterprise.getUserId(), RoleConstants.ENTERPRISE);
        }
        if (enterprise != null && enterprise.getUserId() != null) {
            sendAuditNotificationToEnterprise(enterprise.getUserId(), verification, auditorId, targetStatus);
        }

        return Result.success("audit success", toVo(verification));
    }

    private void requireAdminAuditContext(Long auditorId, Integer auditorRole) {
        requireAdminContext(auditorId, auditorRole);
    }

    private Enterprises ensureEnterpriseByUserId(Long userId, EnterpriseVerificationApplyDto dto) {
        if (userId == null) {
            throw new BizException(401, "user not logged in");
        }

        Enterprises enterprise = findEnterpriseByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (enterprise == null) {
            enterprise = new Enterprises();
            enterprise.setUserId(userId);
            fillEnterpriseProfile(enterprise, dto);
            enterprise.setVerificationStatus(0);
            enterprise.setCreatedTime(now);
            enterprise.setUpdatedTime(now);
            enterprisesMapper.insert(enterprise);
            return enterprise;
        }

        fillEnterpriseProfile(enterprise, dto);
        enterprise.setUpdatedTime(now);
        enterprisesMapper.updateById(enterprise);
        return enterprise;
    }

    private Enterprises findEnterpriseByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "user not logged in");
        }
        return enterprisesMapper.selectOne(new LambdaQueryWrapper<Enterprises>()
                .eq(Enterprises::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void fillEnterpriseProfile(Enterprises enterprise, EnterpriseVerificationApplyDto dto) {
        if (enterprise == null || dto == null) {
            return;
        }
        String companyName = trimToNull(dto.getCompanyName());
        if (companyName != null) {
            enterprise.setCompanyName(companyName);
        }
        String industry = trimToNull(dto.getIndustry());
        if (industry != null) {
            enterprise.setIndustry(industry);
        }
        String companySize = trimToNull(dto.getCompanySize());
        if (companySize != null) {
            enterprise.setCompanySize(companySize);
        }
        String companyAddress = trimToNull(dto.getCompanyAddress());
        if (companyAddress != null) {
            enterprise.setCompanyAddress(companyAddress);
        }
        String contactPerson = trimToNull(dto.getContactPerson());
        if (contactPerson != null) {
            enterprise.setContactPerson(contactPerson);
        }
        String companyIntroduction = trimToNull(dto.getCompanyIntroduction());
        if (companyIntroduction != null) {
            enterprise.setCompanyIntroduction(companyIntroduction);
        }
    }

    private String buildApplyContent(EnterpriseVerificationApplyDto dto, Enterprises enterprise) {
        String raw = trimToNull(dto == null ? null : dto.getApplyContent());
        if (raw != null) {
            return raw;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "companyName", enterprise == null ? null : enterprise.getCompanyName());
        putIfHasText(payload, "industry", enterprise == null ? null : enterprise.getIndustry());
        putIfHasText(payload, "companySize", enterprise == null ? null : enterprise.getCompanySize());
        putIfHasText(payload, "companyAddress", enterprise == null ? null : enterprise.getCompanyAddress());
        putIfHasText(payload, "contactPerson", enterprise == null ? null : enterprise.getContactPerson());
        putIfHasText(payload, "companyIntroduction", enterprise == null ? null : enterprise.getCompanyIntroduction());
        return payload.isEmpty() ? null : JSONUtil.toJsonStr(payload);
    }

    private void putIfHasText(Map<String, Object> payload, String key, String value) {
        String normalized = trimToNull(value);
        if (normalized != null) {
            payload.put(key, normalized);
        }
    }

    private void switchRoleAndForceRelogin(Long userId, int targetRole) {
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "user not found");
        }
        if (user.getRole() == null || user.getRole() != targetRole) {
            user.setRole(targetRole);
            user.setUpdatedTime(LocalDateTime.now());
            usersMapper.updateById(user);
        }
        redisTokenService.incrTokenVersion(userId);
        redisTokenService.removeAllRefreshTokenForUser(userId);
    }

    private void createOrSyncAdminAuditRecordOnApply(EnterpriseVerification verification, Enterprises enterprise, Long userId) {
        if (verification == null || verification.getId() == null || userId == null) {
            return;
        }
        Users applicant = usersMapper.selectById(userId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditType", AUDIT_TYPE_ENTERPRISE);
        payload.put("targetId", verification.getId());
        payload.put("targetName", resolveAuditTargetName(enterprise, applicant));
        payload.put("applicantId", userId);
        payload.put("applicantName", resolveApplicantName(applicant));
        Result result = educationAdminInternalClient.createAudit(payload);
        requireFeignSuccess(result, "create admin audit record failed");
    }

    private void syncAdminAuditRecordOnDecision(EnterpriseVerification verification, Long auditorId) {
        if (verification == null || verification.getId() == null) {
            return;
        }
        Users auditor = auditorId == null ? null : usersMapper.selectById(auditorId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditType", AUDIT_TYPE_ENTERPRISE);
        payload.put("targetId", verification.getId());
        payload.put("auditStatus", verification.getStatus());
        payload.put("auditorId", auditorId);
        payload.put("auditorName", resolveApplicantName(auditor));
        if (STATUS_REJECTED.equals(verification.getStatus())) {
            payload.put("rejectReason", trimToNull(verification.getAuditReason()));
        } else {
            payload.put("auditOpinion", trimToNull(verification.getAuditReason()));
        }
        payload.put("auditTime", verification.getAuditedTime());
        Result result = educationAdminInternalClient.syncAudit(payload);
        requireFeignSuccess(result, "sync admin audit record failed");
    }

    private void sendAuditNotificationToEnterprise(
            Long userId,
            EnterpriseVerification verification,
            Long auditorId,
            String auditStatus
    ) {
        if (userId == null || verification == null || verification.getId() == null) {
            return;
        }
        String eventCode = STATUS_APPROVED.equals(auditStatus)
                ? EVENT_CODE_ENTERPRISE_VERIFICATION_APPROVED
                : EVENT_CODE_ENTERPRISE_VERIFICATION_REJECTED;

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildEnterpriseVerificationEventId(verification.getId(), eventCode));
        payload.setEventCode(eventCode);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(userId));
        payload.setParams(Map.of(
                "application_no", verification.getApplicationNo() == null ? "" : verification.getApplicationNo(),
                "audit_reason", verification.getAuditReason() == null ? "" : verification.getAuditReason()
        ));
        payload.setMessageType(MESSAGE_TYPE_SYSTEM);
        payload.setRelatedId(verification.getId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_ENTERPRISE_VERIFICATION);
        payload.setPriority(STATUS_APPROVED.equals(auditStatus) ? 1 : 0);
        payload.setDeliverMode(DELIVER_MODE_MQ);
        payload.setOperatorId(auditorId);
        payload.setOperatorRole(RoleConstants.ADMIN);

        Result result = educationMessageInternalClient.triggerEvent(
                auditorId,
                RoleConstants.ADMIN,
                payload
        );
        requireFeignSuccess(result, "trigger enterprise verification notification failed");
    }

    private String buildEnterpriseVerificationEventId(Long verificationId, String eventCode) {
        return eventCode + "_" + verificationId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveAuditTargetName(Enterprises enterprise, Users applicant) {
        String companyName = trimToNull(enterprise == null ? null : enterprise.getCompanyName());
        if (companyName != null) {
            return companyName;
        }
        String realName = trimToNull(applicant == null ? null : applicant.getRealName());
        if (realName != null) {
            return realName;
        }
        String username = trimToNull(applicant == null ? null : applicant.getUsername());
        if (username != null) {
            return username;
        }
        return "enterprise-" + (enterprise == null ? "unknown" : enterprise.getId());
    }

    private String resolveApplicantName(Users user) {
        String realName = trimToNull(user == null ? null : user.getRealName());
        if (realName != null) {
            return realName;
        }
        String nickname = trimToNull(user == null ? null : user.getNickname());
        if (nickname != null) {
            return nickname;
        }
        String username = trimToNull(user == null ? null : user.getUsername());
        if (username != null) {
            return username;
        }
        return null;
    }

    private void requireFeignSuccess(Result result, String errorMessage) {
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(500, errorMessage);
        }
    }

    private EnterpriseVerificationVo toVo(EnterpriseVerification entity) {
        EnterpriseVerificationVo vo = BeanUtil.copyProperties(entity, EnterpriseVerificationVo.class);
        vo.setApplicationId(entity.getId());
        return vo;
    }

    private String generateApplicationNo() {
        return "EV-" + LocalDateTime.now().format(NO_FORMATTER) + "-" + RandomUtil.randomStringUpper(4);
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (STATUS_PENDING.equals(normalized)
                || STATUS_APPROVED.equals(normalized)
                || STATUS_REJECTED.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private void requireAdminContext(Long userId, Integer userRole) {
        if (userId == null) {
            throw new BizException(401, "user not logged in");
        }
        if (!hasAdminPermission(userId, userRole)) {
            throw new BizException(403, "only admin can operate");
        }
    }

    private boolean hasAdminPermission(Long userId, Integer userRole) {
        if (userRole != null && userRole == RoleConstants.ADMIN) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        Users user = usersMapper.selectById(userId);
        return user != null && user.getRole() != null && user.getRole() == RoleConstants.ADMIN;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
