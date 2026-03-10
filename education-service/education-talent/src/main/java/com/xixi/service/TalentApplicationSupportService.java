package com.xixi.service;

import cn.hutool.json.JSONUtil;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.JobApplication;
import com.xixi.entity.JobApplicationStatusLog;
import com.xixi.entity.TalentContact;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.JobApplicationMapper;
import com.xixi.mapper.JobApplicationStatusLogMapper;
import com.xixi.mapper.TalentContactMapper;
import com.xixi.openfeign.message.EducationMessageInternalClient;
import com.xixi.pojo.vo.talent.EnterpriseSnapshotVo;
import com.xixi.pojo.vo.talent.JobApplicationCommunicationVo;
import com.xixi.pojo.vo.talent.JobApplicationTimelineVo;
import com.xixi.pojo.vo.talent.JobPublicResumeOptionVo;
import com.xixi.pojo.vo.talent.JobSnapshotVo;
import com.xixi.pojo.vo.talent.ResumeSnapshotVo;
import com.xixi.pojo.vo.talent.TalentApplicationCertificateVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 投递联动公共支持服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TalentApplicationSupportService {
    private static final String SOURCE_TYPE_APPLICATION = "APPLICATION";
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";
    private static final String MESSAGE_TYPE_JOB = "JOB";
    private static final String RELATED_TYPE_APPLICATION = "JOB_APPLICATION";
    private static final String RELATED_TYPE_COMMUNICATION = "JOB_COMMUNICATION";
    private static final int RESUME_COMPLETE_TOTAL_ITEMS = 9;

    private final JobApplicationMapper jobApplicationMapper;
    private final JobApplicationStatusLogMapper jobApplicationStatusLogMapper;
    private final CommunicationRecordMapper communicationRecordMapper;
    private final TalentContactMapper talentContactMapper;
    private final EducationMessageInternalClient educationMessageInternalClient;

    public Long requireStudentId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        Long studentId = jobApplicationMapper.selectStudentIdByUserId(userId);
        if (studentId == null) {
            throw new BizException(404, "当前用户未绑定学生档案");
        }
        return studentId;
    }

    public List<JobPublicResumeOptionVo> listResumeOptions(Long studentId) {
        List<Map<String, Object>> rows = jobApplicationMapper.selectStudentResumeOptionList(studentId);
        List<JobPublicResumeOptionVo> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            JobPublicResumeOptionVo vo = new JobPublicResumeOptionVo();
            vo.setResumeId(toLong(row.get("resumeId")));
            vo.setResumeTitle(toString(row.get("resumeTitle")));
            boolean isDefault = toBoolean(row.get("isDefault"));
            boolean enabled = toBoolean(row.get("status"));
            String visibility = toString(row.get("visibility"));
            vo.setIsDefault(isDefault);
            vo.setVisibility(visibility);
            vo.setCompleteScore(calculateCompleteScore(row));

            boolean canApply = enabled && ("PUBLIC".equalsIgnoreCase(visibility) || isDefault);
            vo.setCanApply(canApply);
            if (!enabled) {
                vo.setDisableReason("简历已停用");
            } else if (!canApply) {
                vo.setDisableReason("仅公开简历或默认简历可用于投递");
            }
            result.add(vo);
        }
        return result;
    }

    public ResumeSnapshotVo buildResumeSnapshot(Long studentId, Long resumeId, boolean requireCanApply) {
        Map<String, Object> source = jobApplicationMapper.selectResumeSnapshotSource(resumeId, studentId);
        if (source == null || source.isEmpty()) {
            throw new BizException(404, "简历不存在");
        }

        boolean enabled = toBoolean(source.get("status"));
        boolean isDefault = toBoolean(source.get("isDefault"));
        String visibility = toString(source.get("visibility"));
        boolean canApply = enabled && ("PUBLIC".equalsIgnoreCase(visibility) || isDefault);
        if (!enabled) {
            throw new BizException(409, "简历已停用，无法投递");
        }
        if (requireCanApply && !canApply) {
            throw new BizException(409, "仅公开简历或默认简历可用于投递");
        }

        ResumeSnapshotVo snapshot = new ResumeSnapshotVo();
        snapshot.setResumeId(toLong(source.get("resumeId")));
        snapshot.setResumeTitle(toString(source.get("resumeTitle")));
        snapshot.setVisibility(visibility);
        snapshot.setCareerObjective(toString(source.get("careerObjective")));
        snapshot.setSkillSummary(toString(source.get("skillSummary")));
        snapshot.setContactEmail(toString(source.get("contactEmail")));
        snapshot.setContactPhone(toString(source.get("contactPhone")));
        snapshot.setCertificateList(buildCertificateSnapshotList(resumeId, studentId));
        return snapshot;
    }

    public JobSnapshotVo buildJobSnapshot(Map<String, Object> row) {
        JobSnapshotVo snapshot = new JobSnapshotVo();
        snapshot.setJobId(toLong(row.get("jobId")));
        snapshot.setJobTitle(toString(row.get("jobTitle")));
        snapshot.setJobType(toString(row.get("jobType")));
        snapshot.setJobCategory(toString(row.get("jobCategory")));
        snapshot.setWorkLocation(toString(row.get("workLocation")));
        snapshot.setSalaryRange(toString(row.get("salaryRange")));
        return snapshot;
    }

    public EnterpriseSnapshotVo buildEnterpriseSnapshot(Map<String, Object> row) {
        EnterpriseSnapshotVo snapshot = new EnterpriseSnapshotVo();
        snapshot.setEnterpriseId(toLong(row.get("enterpriseId")));
        snapshot.setEnterpriseName(toString(row.get("enterpriseName")));
        snapshot.setEnterpriseLogo(toString(row.get("enterpriseLogo")));
        snapshot.setIndustry(toString(row.get("industry")));
        snapshot.setCity(toString(row.get("city")));
        snapshot.setEnterpriseVerified(toBoolean(row.get("enterpriseVerified")));
        snapshot.setEnterpriseIntroduction(toString(row.get("enterpriseIntroduction")));
        snapshot.setContactName(toString(row.get("contactName")));
        snapshot.setContactPhone(firstNonBlank(toString(row.get("contactPhone")), toString(row.get("contactUserPhone"))));
        snapshot.setContactEmail(firstNonBlank(toString(row.get("contactEmail")), toString(row.get("contactUserEmail"))));
        return snapshot;
    }

    public List<TalentApplicationCertificateVo> buildCertificateSnapshotList(Long resumeId, Long studentId) {
        List<Map<String, Object>> rows = jobApplicationMapper.selectResumeCertificateSnapshotList(resumeId, studentId);
        List<TalentApplicationCertificateVo> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            TalentApplicationCertificateVo item = new TalentApplicationCertificateVo();
            item.setCertificateId(toLong(row.get("certificateId")));
            item.setCertificateName(toString(row.get("certificateName")));
            item.setCertificateNumber(toString(row.get("certificateNumber")));
            item.setStatus(toString(row.get("status")));
            item.setVerified(toBoolean(row.get("verified")));
            item.setVerifiedTime(toDateTime(row.get("verifiedTime")));
            list.add(item);
        }
        return list;
    }

    public void appendStatusLog(
            JobApplication application,
            String actionType,
            String fromStatus,
            String toStatus,
            Long relatedId,
            Long operatorUserId,
            Integer operatorRole,
            String description
    ) {
        JobApplicationStatusLog logEntity = new JobApplicationStatusLog();
        logEntity.setApplicationId(application.getId());
        logEntity.setApplicationNo(application.getApplicationNo());
        logEntity.setJobId(application.getJobId());
        logEntity.setStudentId(application.getStudentId());
        logEntity.setEnterpriseId(application.getEnterpriseId());
        logEntity.setActionType(actionType);
        logEntity.setFromStatus(fromStatus);
        logEntity.setToStatus(toStatus);
        logEntity.setRelatedId(relatedId);
        logEntity.setOperatorUserId(operatorUserId);
        logEntity.setOperatorRole(operatorRole);
        logEntity.setDescription(description);
        logEntity.setCreatedTime(LocalDateTime.now());
        jobApplicationStatusLogMapper.insert(logEntity);
    }

    public List<JobApplicationTimelineVo> buildTimeline(Long applicationId, String currentStatus) {
        List<JobApplicationStatusLog> logs = jobApplicationStatusLogMapper.selectByApplicationId(applicationId);
        List<JobApplicationTimelineVo> result = new ArrayList<>(logs.size());
        for (int i = 0; i < logs.size(); i++) {
            JobApplicationStatusLog logEntity = logs.get(i);
            JobApplicationTimelineVo item = new JobApplicationTimelineVo();
            String nodeCode = StringUtils.hasText(logEntity.getToStatus()) ? logEntity.getToStatus() : logEntity.getActionType();
            item.setNodeCode(nodeCode);
            item.setNodeName(resolveNodeName(logEntity.getActionType(), logEntity.getToStatus()));
            boolean isLast = i == logs.size() - 1;
            item.setNodeStatus(isLast && StringUtils.hasText(currentStatus) && currentStatus.equals(logEntity.getToStatus()) ? "CURRENT" : "FINISHED");
            item.setNodeTime(logEntity.getCreatedTime());
            item.setDescription(logEntity.getDescription());
            result.add(item);
        }
        return result;
    }

    public List<JobApplicationCommunicationVo> listCommunication(Long applicationId) {
        List<Map<String, Object>> rows = communicationRecordMapper.selectByApplicationId(applicationId);
        List<JobApplicationCommunicationVo> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            JobApplicationCommunicationVo item = new JobApplicationCommunicationVo();
            item.setRecordId(toLong(row.get("recordId")));
            item.setApplicationId(toLong(row.get("applicationId")));
            item.setApplicationNo(toString(row.get("applicationNo")));
            item.setJobId(toLong(row.get("jobId")));
            item.setJobTitle(toString(row.get("jobTitle")));
            item.setEnterpriseId(toLong(row.get("enterpriseId")));
            item.setEnterpriseName(toString(row.get("enterpriseName")));
            item.setStudentId(toLong(row.get("studentId")));
            item.setStudentName(toString(row.get("studentName")));
            item.setCommunicationType(toString(row.get("communicationType")));
            item.setCommunicationSubject(toString(row.get("communicationSubject")));
            item.setCommunicationContent(toString(row.get("communicationContent")));
            item.setAttachmentUrl(toString(row.get("attachmentUrl")));
            item.setNeedStudentConfirm(toBoolean(row.get("needStudentConfirm")));
            item.setStudentConfirmed(toBoolean(row.get("studentConfirmed")));
            item.setIsRead(toBoolean(row.get("isRead")));
            item.setReadTime(toDateTime(row.get("readTime")));
            item.setConfirmTime(toDateTime(row.get("confirmTime")));
            item.setConfirmRemark(toString(row.get("confirmRemark")));
            item.setInterviewTime(toDateTime(row.get("interviewTime")));
            item.setInterviewAddress(toString(row.get("interviewAddress")));
            item.setCreatedTime(toDateTime(row.get("createdTime")));
            result.add(item);
        }
        return result;
    }

    public void syncApplicationContact(JobApplication application, JobSnapshotVo jobSnapshot, ResumeSnapshotVo resumeSnapshot, String studentName) {
        if (application == null) {
            return;
        }
        TalentContact existed = talentContactMapper.selectByEnterpriseAndApplication(application.getEnterpriseId(), application.getId());
        LocalDateTime lastTime = application.getLatestCommunicationTime() != null
                ? application.getLatestCommunicationTime()
                : application.getSubmittedTime();
        String name = StringUtils.hasText(studentName) ? studentName.trim() : "未命名学生";
        String position = jobSnapshot == null ? null : jobSnapshot.getJobTitle();
        String phone = resumeSnapshot == null ? null : resumeSnapshot.getContactPhone();
        String email = resumeSnapshot == null ? null : resumeSnapshot.getContactEmail();
        if (existed == null) {
            TalentContact contact = new TalentContact();
            contact.setEnterpriseId(application.getEnterpriseId());
            contact.setStudentId(application.getStudentId());
            contact.setSourceType(SOURCE_TYPE_APPLICATION);
            contact.setApplicationId(application.getId());
            contact.setJobId(application.getJobId());
            contact.setName(name);
            contact.setPhone(phone);
            contact.setEmail(email);
            contact.setPosition(position);
            contact.setStatus(application.getStatus());
            contact.setLatestStatus(application.getStatus());
            contact.setLastContactTime(lastTime);
            contact.setCreatedTime(LocalDateTime.now());
            contact.setUpdatedTime(LocalDateTime.now());
            talentContactMapper.insert(contact);
            return;
        }
        talentContactMapper.updateFromApplication(
                existed.getId(),
                application.getJobId(),
                name,
                phone,
                email,
                position,
                application.getStatus(),
                application.getStatus(),
                lastTime,
                LocalDateTime.now()
        );
    }

    public void sendJobMessage(
            Long targetUserId,
            String messageTitle,
            String messageContent,
            Long relatedId,
            String relatedType,
            Long operatorId,
            Integer operatorRole
    ) {
        if (targetUserId == null || operatorId == null || operatorRole == null) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", targetUserId);
            payload.put("deliverMode", "SYNC");
            payload.put("messageType", MESSAGE_TYPE_JOB);
            payload.put("messageTitle", messageTitle);
            payload.put("messageContent", messageContent);
            payload.put("relatedId", relatedId);
            payload.put("relatedType", relatedType);
            payload.put("priority", 0);
            educationMessageInternalClient.sendToUser(operatorId, operatorRole, payload);
        } catch (Exception e) {
            log.warn("发送JOB消息失败,targetUserId={},relatedId={},relatedType={}", targetUserId, relatedId, relatedType, e);
        }
    }

    public JobSnapshotVo parseJobSnapshot(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return JSONUtil.toBean(json, JobSnapshotVo.class);
    }

    public EnterpriseSnapshotVo parseEnterpriseSnapshot(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return JSONUtil.toBean(json, EnterpriseSnapshotVo.class);
    }

    public ResumeSnapshotVo parseResumeSnapshot(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return JSONUtil.toBean(json, ResumeSnapshotVo.class);
    }

    public String statusText(String status) {
        if (!StringUtils.hasText(status)) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "SUBMITTED" -> "已投递";
            case "REVIEWING" -> "筛选中";
            case "INTERVIEW_INVITED" -> "已发送面试邀约";
            case "INTERVIEW_SCHEDULED" -> "面试已确认";
            case "INTERVIEW_FINISHED" -> "面试已完成";
            case "OFFER_SENT" -> "已发送录用意向";
            case "HIRED" -> "已录用";
            case "REJECTED" -> "未通过";
            case "WITHDRAWN" -> "已撤回";
            case "CLOSED" -> "岗位已关闭";
            default -> status;
        };
    }

    public boolean canWithdraw(String status) {
        return "SUBMITTED".equals(status) || "REVIEWING".equals(status);
    }

    public boolean canEnterpriseUpdateTo(String status) {
        if (!StringUtils.hasText(status)) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return !"WITHDRAWN".equals(normalized);
    }

    public String normalizeApplicationStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "SUBMITTED",
                 "REVIEWING",
                 "INTERVIEW_INVITED",
                 "INTERVIEW_SCHEDULED",
                 "INTERVIEW_FINISHED",
                 "OFFER_SENT",
                 "HIRED",
                 "REJECTED",
                 "WITHDRAWN",
                 "CLOSED" -> normalized;
            default -> null;
        };
    }

    public Long getStudentUserId(Long studentId) {
        return jobApplicationMapper.selectStudentUserIdByStudentId(studentId);
    }

    public Long getEnterpriseUserIdByJob(Long jobId) {
        return jobApplicationMapper.selectEnterpriseUserIdByJobId(jobId);
    }

    private int calculateCompleteScore(Map<String, Object> row) {
        int filledCount = 0;
        filledCount += toBoolean(row.get("hasContactEmail")) ? 1 : 0;
        filledCount += toBoolean(row.get("hasContactPhone")) ? 1 : 0;
        filledCount += toBoolean(row.get("hasCareerObjective")) ? 1 : 0;
        filledCount += toBoolean(row.get("hasSelfIntroduction")) ? 1 : 0;
        filledCount += toInt(row.get("educationCount")) > 0 ? 1 : 0;
        filledCount += toInt(row.get("experienceCount")) > 0 ? 1 : 0;
        filledCount += toInt(row.get("projectCount")) > 0 ? 1 : 0;
        filledCount += toInt(row.get("skillCount")) > 0 ? 1 : 0;
        filledCount += toInt(row.get("certificateCount")) > 0 ? 1 : 0;
        return (int) Math.round(filledCount * 100.0d / RESUME_COMPLETE_TOTAL_ITEMS);
    }

    private String resolveNodeName(String actionType, String toStatus) {
        if (StringUtils.hasText(toStatus)) {
            return statusText(toStatus);
        }
        if (!StringUtils.hasText(actionType)) {
            return "流程节点";
        }
        return switch (actionType) {
            case "CREATE" -> "投递创建";
            case "COMMUNICATION_SEND" -> "企业发起沟通";
            case "STUDENT_CONFIRM" -> "学生确认沟通";
            case "WITHDRAW" -> "学生撤回投递";
            case "STATUS_UPDATE" -> "状态更新";
            case "SYSTEM_CLOSE" -> "系统关闭流程";
            default -> actionType;
        };
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = String.valueOf(value).trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text);
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }
}
