package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.TeacherVerification;
import com.xixi.entity.Teachers;
import com.xixi.entity.Users;
import com.xixi.exception.BizException;
import com.xixi.mapper.TeacherVerificationMapper;
import com.xixi.mapper.TeachersMapper;
import com.xixi.mapper.UsersMapper;
import com.xixi.openfeign.admin.EducationAdminInternalClient;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.pojo.dto.TeacherVerificationApplyDto;
import com.xixi.pojo.dto.TeacherVerificationAuditDto;
import com.xixi.pojo.query.TeacherVerificationHistoryQuery;
import com.xixi.pojo.vo.TeacherVerificationVo;
import com.xixi.service.RedisTokenService;
import com.xixi.service.TeacherVerificationService;
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
public class TeacherVerificationServiceImpl implements TeacherVerificationService {
    private static final String AUDIT_TYPE_TEACHER = "TEACHER";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    private static final String MESSAGE_RELATED_TYPE_TEACHER_VERIFICATION = "TEACHER_VERIFICATION";
    private static final String EVENT_CODE_TEACHER_VERIFICATION_APPROVED = "TEACHER_VERIFICATION_APPROVED";
    private static final String EVENT_CODE_TEACHER_VERIFICATION_REJECTED = "TEACHER_VERIFICATION_REJECTED";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String DELIVER_MODE_MQ = "MQ";
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TeacherVerificationMapper teacherVerificationMapper;
    private final TeachersMapper teachersMapper;
    private final UsersMapper usersMapper;
    private final EducationAdminInternalClient educationAdminInternalClient;
    private final EducationMessageInternalClient educationMessageInternalClient;
    private final RedisTokenService redisTokenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result apply(TeacherVerificationApplyDto dto, Long userId) {
        Teachers teacher = ensureTeacherByUserId(userId, dto);
        TeacherVerification current = teacherVerificationMapper.selectCurrentByTeacher(teacher.getId());
        if (current != null && STATUS_PENDING.equals(current.getStatus())) {
            createOrSyncAdminAuditRecordOnApply(current, teacher, userId);
            return Result.success("already has pending application", toVo(current));
        }

        LocalDateTime now = LocalDateTime.now();
        TeacherVerification verification = new TeacherVerification();
        verification.setTeacherId(teacher.getId());
        verification.setApplicationNo(generateApplicationNo());
        verification.setApplyContent(buildApplyContent(dto, teacher));
        verification.setStatus(STATUS_PENDING);
        verification.setSubmittedTime(now);
        verification.setCreatedTime(now);
        verification.setUpdatedTime(now);
        teacherVerificationMapper.insert(verification);
        createOrSyncAdminAuditRecordOnApply(verification, teacher, userId);

        return Result.success("apply success", toVo(verification));
    }

    @Override
    public TeacherVerificationVo current(Long userId) {
        Teachers teacher = findTeacherByUserId(userId);
        if (teacher == null) {
            return null;
        }
        TeacherVerification verification = teacherVerificationMapper.selectCurrentByTeacher(teacher.getId());
        return verification == null ? null : toVo(verification);
    }

    @Override
    public IPage<TeacherVerificationVo> historyMyPage(TeacherVerificationHistoryQuery query, Long userId) {
        Teachers teacher = findTeacherByUserId(userId);
        if (teacher == null) {
            TeacherVerificationHistoryQuery safeQuery = query == null ? new TeacherVerificationHistoryQuery() : query;
            long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
            long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
            return new Page<>(pageNum, pageSize, 0);
        }
        return historyPageByTeacher(query, teacher.getId());
    }

    @Override
    public IPage<TeacherVerificationVo> historyAdminPage(TeacherVerificationHistoryQuery query, Long userId, Integer userRole) {
        requireAdminContext(userId, userRole);
        return historyPageByTeacher(query, null);
    }

    @Override
    public IPage<TeacherVerificationVo> historyPage(TeacherVerificationHistoryQuery query, Long userId, Integer userRole) {
        if (hasAdminPermission(userId, userRole)) {
            return historyAdminPage(query, userId, userRole);
        }
        return historyMyPage(query, userId);
    }

    private IPage<TeacherVerificationVo> historyPageByTeacher(TeacherVerificationHistoryQuery query, Long teacherId) {
        TeacherVerificationHistoryQuery safeQuery = query == null ? new TeacherVerificationHistoryQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();

        IPage<TeacherVerification> entityPage = teacherVerificationMapper.selectHistoryPage(
                new Page<>(pageNum, pageSize),
                teacherId
        );
        Page<TeacherVerificationVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<TeacherVerificationVo> records = entityPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result audit(Long applicationId, TeacherVerificationAuditDto dto, Long auditorId, Integer auditorRole) {
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

        TeacherVerification verification = teacherVerificationMapper.selectById(applicationId);
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
        teacherVerificationMapper.updateById(verification);
        syncAdminAuditRecordOnDecision(verification, auditorId);

        Teachers teacher = teachersMapper.selectById(verification.getTeacherId());
        if (STATUS_APPROVED.equals(targetStatus)) {
            if (teacher == null || teacher.getUserId() == null) {
                throw new BizException(500, "teacher mapping not found");
            }
            switchRoleAndForceRelogin(teacher.getUserId(), RoleConstants.TEACHER);
        }
        if (teacher != null && teacher.getUserId() != null) {
            sendAuditNotificationToTeacher(teacher.getUserId(), verification, auditorId, targetStatus);
        }

        return Result.success("audit success", toVo(verification));
    }

    private void requireAdminAuditContext(Long auditorId, Integer auditorRole) {
        requireAdminContext(auditorId, auditorRole);
    }

    private Teachers ensureTeacherByUserId(Long userId, TeacherVerificationApplyDto dto) {
        if (userId == null) {
            throw new BizException(401, "user not logged in");
        }

        Teachers teacher = findTeacherByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (teacher == null) {
            teacher = new Teachers();
            teacher.setUserId(userId);
            fillTeacherProfile(teacher, dto);
            teacher.setCreatedTime(now);
            teacher.setUpdatedTime(now);
            teachersMapper.insert(teacher);
            return teacher;
        }

        fillTeacherProfile(teacher, dto);
        teacher.setUpdatedTime(now);
        teachersMapper.updateById(teacher);
        return teacher;
    }

    private Teachers findTeacherByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "user not logged in");
        }
        return teachersMapper.selectOne(new LambdaQueryWrapper<Teachers>()
                .eq(Teachers::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void fillTeacherProfile(Teachers teacher, TeacherVerificationApplyDto dto) {
        if (teacher == null || dto == null) {
            return;
        }
        String teacherNumber = trimToNull(dto.getTeacherNumber());
        if (teacherNumber != null) {
            teacher.setTeacherNumber(teacherNumber);
        }
        String title = trimToNull(dto.getTitle());
        if (title != null) {
            teacher.setTitle(title);
        }
        String department = trimToNull(dto.getDepartment());
        if (department != null) {
            teacher.setDepartment(department);
        }
        String researchArea = trimToNull(dto.getResearchArea());
        if (researchArea != null) {
            teacher.setResearchArea(researchArea);
        }
        String introduction = trimToNull(dto.getIntroduction());
        if (introduction != null) {
            teacher.setIntroduction(introduction);
        }
    }

    private String buildApplyContent(TeacherVerificationApplyDto dto, Teachers teacher) {
        String raw = trimToNull(dto == null ? null : dto.getApplyContent());
        if (raw != null) {
            return raw;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "teacherNumber", teacher == null ? null : teacher.getTeacherNumber());
        putIfHasText(payload, "title", teacher == null ? null : teacher.getTitle());
        putIfHasText(payload, "department", teacher == null ? null : teacher.getDepartment());
        putIfHasText(payload, "researchArea", teacher == null ? null : teacher.getResearchArea());
        putIfHasText(payload, "introduction", teacher == null ? null : teacher.getIntroduction());
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

    private void createOrSyncAdminAuditRecordOnApply(TeacherVerification verification, Teachers teacher, Long userId) {
        if (verification == null || verification.getId() == null || userId == null) {
            return;
        }
        Users applicant = usersMapper.selectById(userId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditType", AUDIT_TYPE_TEACHER);
        payload.put("targetId", verification.getId());
        payload.put("targetName", resolveAuditTargetName(teacher, applicant));
        payload.put("applicantId", userId);
        payload.put("applicantName", resolveApplicantName(applicant));
        Result result = educationAdminInternalClient.createAudit(payload);
        requireFeignSuccess(result, "create admin audit record failed");
    }

    private void syncAdminAuditRecordOnDecision(TeacherVerification verification, Long auditorId) {
        if (verification == null || verification.getId() == null) {
            return;
        }
        Users auditor = auditorId == null ? null : usersMapper.selectById(auditorId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditType", AUDIT_TYPE_TEACHER);
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

    private void sendAuditNotificationToTeacher(
            Long userId,
            TeacherVerification verification,
            Long auditorId,
            String auditStatus
    ) {
        if (userId == null || verification == null || verification.getId() == null) {
            return;
        }
        String eventCode = STATUS_APPROVED.equals(auditStatus)
                ? EVENT_CODE_TEACHER_VERIFICATION_APPROVED
                : EVENT_CODE_TEACHER_VERIFICATION_REJECTED;

        TemplateTriggerEventRequest payload = new TemplateTriggerEventRequest();
        payload.setEventId(buildTeacherVerificationEventId(verification.getId(), eventCode));
        payload.setEventCode(eventCode);
        payload.setTargetType(TARGET_TYPE_USER);
        payload.setTargetValue(List.of(userId));
        payload.setParams(Map.of(
                "application_no", verification.getApplicationNo() == null ? "" : verification.getApplicationNo(),
                "audit_reason", verification.getAuditReason() == null ? "" : verification.getAuditReason()
        ));
        payload.setMessageType(MESSAGE_TYPE_SYSTEM);
        payload.setRelatedId(verification.getId());
        payload.setRelatedType(MESSAGE_RELATED_TYPE_TEACHER_VERIFICATION);
        payload.setPriority(STATUS_APPROVED.equals(auditStatus) ? 1 : 0);
        payload.setDeliverMode(DELIVER_MODE_MQ);
        payload.setOperatorId(auditorId);
        payload.setOperatorRole(RoleConstants.ADMIN);

        Result result = educationMessageInternalClient.triggerEvent(
                auditorId,
                RoleConstants.ADMIN,
                payload
        );
        requireFeignSuccess(result, "trigger teacher verification notification failed");
    }

    private String buildTeacherVerificationEventId(Long verificationId, String eventCode) {
        return eventCode + "_" + verificationId + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveAuditTargetName(Teachers teacher, Users applicant) {
        String realName = trimToNull(applicant == null ? null : applicant.getRealName());
        if (realName != null) {
            return realName;
        }
        String teacherNumber = trimToNull(teacher == null ? null : teacher.getTeacherNumber());
        if (teacherNumber != null) {
            return teacherNumber;
        }
        String username = trimToNull(applicant == null ? null : applicant.getUsername());
        if (username != null) {
            return username;
        }
        return "teacher-" + (teacher == null ? "unknown" : teacher.getId());
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

    private TeacherVerificationVo toVo(TeacherVerification entity) {
        TeacherVerificationVo vo = BeanUtil.copyProperties(entity, TeacherVerificationVo.class);
        vo.setApplicationId(entity.getId());
        return vo;
    }

    private String generateApplicationNo() {
        return "TV-" + LocalDateTime.now().format(NO_FORMATTER) + "-" + RandomUtil.randomStringUpper(4);
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
