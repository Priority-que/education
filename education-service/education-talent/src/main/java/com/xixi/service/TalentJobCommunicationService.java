package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.CommunicationRecord;
import com.xixi.entity.JobApplication;
import com.xixi.entity.JobPosting;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.JobApplicationMapper;
import com.xixi.mapper.JobPostingMapper;
import com.xixi.mq.TalentDomainEventProducer;
import com.xixi.pojo.dto.talent.CommunicationRecordPageQueryDto;
import com.xixi.pojo.dto.talent.CommunicationRecordSendDto;
import com.xixi.pojo.dto.talent.JobPostingCreateDto;
import com.xixi.pojo.dto.talent.JobPostingPageQueryDto;
import com.xixi.pojo.dto.talent.JobPostingStatusUpdateDto;
import com.xixi.pojo.dto.talent.JobPostingUpdateDto;
import com.xixi.pojo.vo.talent.TalentCommunicationPageVo;
import com.xixi.pojo.vo.talent.TalentJobDetailVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 岗位管理与企业沟通服务。
 */
@Service
@RequiredArgsConstructor
public class TalentJobCommunicationService {
    private static final String JOB_STATUS_DRAFT = "DRAFT";
    private static final String JOB_STATUS_PUBLISHED = "PUBLISHED";
    private static final String JOB_STATUS_CLOSED = "CLOSED";
    private static final String JOB_STATUS_EXPIRED = "EXPIRED";

    private static final String JOB_TYPE_FULL_TIME = "FULL_TIME";
    private static final String JOB_TYPE_PART_TIME = "PART_TIME";
    private static final String JOB_TYPE_INTERNSHIP = "INTERNSHIP";

    private static final String COMMUNICATION_TYPE_EMAIL = "EMAIL";
    private static final String COMMUNICATION_TYPE_PHONE = "PHONE";
    private static final String COMMUNICATION_TYPE_INTERVIEW = "INTERVIEW";
    private static final String COMMUNICATION_TYPE_MESSAGE = "MESSAGE";
    private static final String STATUS_INTERVIEW_INVITED = "INTERVIEW_INVITED";
    private static final String SENDER_ENTERPRISE = "ENTERPRISE";
    private static final String RECEIVER_STUDENT = "STUDENT";

    private final EnterpriseIdentityService enterpriseIdentityService;
    private final JobPostingMapper jobPostingMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final CommunicationRecordMapper communicationRecordMapper;
    private final TalentApplicationSupportService talentApplicationSupportService;
    private final TalentDomainEventProducer talentDomainEventProducer;

    @MethodPurpose("分页查询企业岗位列表")
    public IPage<JobPosting> pageJobs(JobPostingPageQueryDto query, Long userId) {
        JobPostingPageQueryDto safeQuery = query == null ? new JobPostingPageQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        return jobPostingMapper.selectJobPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                enterpriseId,
                normalizeJobStatus(safeQuery.getStatus()),
                normalizeJobType(safeQuery.getJobType()),
                trimToNull(safeQuery.getKeyword())
        );
    }

    @MethodPurpose("查询企业岗位详情及投递统计")
    public TalentJobDetailVo getJobDetail(Long jobId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        Map<String, Object> row = jobPostingMapper.selectTalentJobDetail(jobId, enterpriseId);
        if (row == null || row.isEmpty()) {
            throw new BizException(404, "岗位不存在");
        }
        TalentJobDetailVo vo = new TalentJobDetailVo();
        vo.setId(toLong(row.get("id")));
        vo.setEnterpriseId(toLong(row.get("enterpriseId")));
        vo.setJobTitle(toString(row.get("jobTitle")));
        vo.setJobType(toString(row.get("jobType")));
        vo.setJobCategory(toString(row.get("jobCategory")));
        vo.setWorkLocation(toString(row.get("workLocation")));
        vo.setSalaryRange(toString(row.get("salaryRange")));
        vo.setExperienceRequirement(toString(row.get("experienceRequirement")));
        vo.setEducationRequirement(toString(row.get("educationRequirement")));
        vo.setJobDescription(toString(row.get("jobDescription")));
        vo.setRequirements(toString(row.get("requirements")));
        vo.setBenefits(toString(row.get("benefits")));
        vo.setRecruitmentNumber(toInt(row.get("recruitmentNumber")));
        vo.setApplicationDeadline(toDate(row.get("applicationDeadline")));
        vo.setContactEmail(toString(row.get("contactEmail")));
        vo.setContactPhone(toString(row.get("contactPhone")));
        vo.setStatus(toString(row.get("status")));
        vo.setPublishTime(toDateTime(row.get("publishTime")));
        vo.setViewCount(toInt(row.get("viewCount")));
        vo.setApplyCount(toInt(row.get("applyCount")));
        vo.setPendingCount(toInt(row.get("pendingCount")));
        vo.setReviewingCount(toInt(row.get("reviewingCount")));
        vo.setInterviewCount(toInt(row.get("interviewCount")));
        vo.setOfferCount(toInt(row.get("offerCount")));
        vo.setHiredCount(toInt(row.get("hiredCount")));
        vo.setRejectedCount(toInt(row.get("rejectedCount")));
        vo.setCreatedTime(toDateTime(row.get("createdTime")));
        vo.setUpdatedTime(toDateTime(row.get("updatedTime")));
        return vo;
    }

    @MethodPurpose("创建企业岗位")
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobPostingCreateDto dto, Long userId) {
        if (dto == null || !StringUtils.hasText(dto.getJobTitle())) {
            throw new BizException(400, "jobTitle不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        String status = normalizeJobStatus(dto.getStatus());
        if (status == null) {
            status = JOB_STATUS_DRAFT;
        }
        validatePublishRequiredFields(status, dto.getWorkLocation(), dto.getApplicationDeadline(), dto.getContactEmail(), dto.getContactPhone());

        LocalDateTime now = LocalDateTime.now();
        JobPosting posting = new JobPosting();
        posting.setEnterpriseId(enterpriseId);
        posting.setJobTitle(dto.getJobTitle().trim());
        posting.setJobType(normalizeJobType(dto.getJobType()));
        posting.setJobCategory(trimToNull(dto.getJobCategory()));
        posting.setWorkLocation(trimToNull(dto.getWorkLocation()));
        posting.setSalaryRange(trimToNull(dto.getSalaryRange()));
        posting.setExperienceRequirement(trimToNull(dto.getExperienceRequirement()));
        posting.setEducationRequirement(trimToNull(dto.getEducationRequirement()));
        posting.setJobDescription(trimToNull(dto.getJobDescription()));
        posting.setRequirements(trimToNull(dto.getRequirements()));
        posting.setBenefits(trimToNull(dto.getBenefits()));
        posting.setRecruitmentNumber(dto.getRecruitmentNumber() == null ? 1 : dto.getRecruitmentNumber());
        posting.setApplicationDeadline(dto.getApplicationDeadline());
        posting.setContactEmail(trimToNull(dto.getContactEmail()));
        posting.setContactPhone(trimToNull(dto.getContactPhone()));
        posting.setStatus(status);
        posting.setPublishTime(JOB_STATUS_PUBLISHED.equals(status) ? now : null);
        posting.setViewCount(0);
        posting.setApplyCount(0);
        posting.setCreatedTime(now);
        posting.setUpdatedTime(now);
        jobPostingMapper.insert(posting);

        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_JOB_CHANGED,
                enterpriseId,
                posting.getId(),
                Map.of("action", "CREATE", "status", status)
        );
        return posting.getId();
    }

    @MethodPurpose("更新企业岗位内容")
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(Long jobId, JobPostingUpdateDto dto, Long userId) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        JobPosting existed = requireJob(enterpriseId, jobId);
        int affected = jobPostingMapper.updateJobByEnterprise(
                jobId,
                enterpriseId,
                defaultValue(trimToNull(dto.getJobTitle()), existed.getJobTitle()),
                defaultValue(normalizeJobType(dto.getJobType()), existed.getJobType()),
                defaultValue(trimToNull(dto.getJobCategory()), existed.getJobCategory()),
                defaultValue(trimToNull(dto.getWorkLocation()), existed.getWorkLocation()),
                defaultValue(trimToNull(dto.getSalaryRange()), existed.getSalaryRange()),
                defaultValue(trimToNull(dto.getExperienceRequirement()), existed.getExperienceRequirement()),
                defaultValue(trimToNull(dto.getEducationRequirement()), existed.getEducationRequirement()),
                defaultValue(trimToNull(dto.getJobDescription()), existed.getJobDescription()),
                defaultValue(trimToNull(dto.getRequirements()), existed.getRequirements()),
                defaultValue(trimToNull(dto.getBenefits()), existed.getBenefits()),
                dto.getRecruitmentNumber() == null ? existed.getRecruitmentNumber() : dto.getRecruitmentNumber(),
                dto.getApplicationDeadline() == null ? existed.getApplicationDeadline() : dto.getApplicationDeadline(),
                defaultValue(trimToNull(dto.getContactEmail()), existed.getContactEmail()),
                defaultValue(trimToNull(dto.getContactPhone()), existed.getContactPhone()),
                LocalDateTime.now()
        );
        if (affected <= 0) {
            throw new BizException(409, "岗位更新失败");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_JOB_CHANGED,
                enterpriseId,
                jobId,
                Map.of("action", "UPDATE")
        );
    }

    @MethodPurpose("更新企业岗位状态")
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long jobId, JobPostingStatusUpdateDto dto, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        if (dto == null || !StringUtils.hasText(dto.getStatus())) {
            throw new BizException(400, "status不能为空");
        }
        JobPosting posting = requireJob(enterpriseId, jobId);
        String targetStatus = normalizeJobStatus(dto.getStatus());
        if (targetStatus == null) {
            throw new BizException(400, "status不合法");
        }
        if (targetStatus.equals(posting.getStatus())) {
            throw new BizException(409, "岗位状态未发生变化");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishTime = JOB_STATUS_PUBLISHED.equals(targetStatus) && posting.getPublishTime() == null ? now : null;
        int affected = jobPostingMapper.updateStatusByEnterprise(jobId, enterpriseId, targetStatus, publishTime, now);
        if (affected <= 0) {
            throw new BizException(409, "岗位状态更新失败");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_JOB_CHANGED,
                enterpriseId,
                jobId,
                Map.of("action", "STATUS", "status", targetStatus)
        );
    }

    @MethodPurpose("删除企业岗位")
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long jobId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        requireJob(enterpriseId, jobId);
        int affected = jobPostingMapper.deleteByEnterpriseAndId(enterpriseId, jobId);
        if (affected <= 0) {
            throw new BizException(409, "岗位删除失败");
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_JOB_CHANGED,
                enterpriseId,
                jobId,
                Map.of("action", "DELETE")
        );
    }

    @MethodPurpose("企业发送沟通记录")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendCommunication(CommunicationRecordSendDto dto, Long userId) {
        if (dto == null || dto.getApplicationId() == null || !StringUtils.hasText(dto.getCommunicationType())) {
            throw new BizException(400, "applicationId和communicationType不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        JobApplication application = jobApplicationMapper.selectByEnterpriseAndId(enterpriseId, dto.getApplicationId());
        if (application == null) {
            throw new BizException(404, "投递记录不存在");
        }
        String communicationType = normalizeCommunicationType(dto.getCommunicationType());
        if (communicationType == null) {
            throw new BizException(400, "communicationType不合法");
        }
        if (dto.getStudentId() != null && !dto.getStudentId().equals(application.getStudentId())) {
            throw new BizException(400, "studentId与投递记录不一致");
        }
        if (dto.getJobId() != null && !dto.getJobId().equals(application.getJobId())) {
            throw new BizException(400, "jobId与投递记录不一致");
        }

        LocalDateTime now = LocalDateTime.now();
        CommunicationRecord record = new CommunicationRecord();
        record.setEnterpriseId(enterpriseId);
        record.setStudentId(application.getStudentId());
        record.setApplicationId(application.getId());
        record.setJobId(application.getJobId());
        record.setCommunicationType(communicationType);
        record.setCommunicationSubject(trimToNull(dto.getCommunicationSubject()));
        record.setCommunicationContent(trimToNull(dto.getCommunicationContent()));
        record.setSenderId(enterpriseId);
        record.setSenderType(SENDER_ENTERPRISE);
        record.setReceiverId(application.getStudentId());
        record.setReceiverType(RECEIVER_STUDENT);
        record.setAttachmentUrl(trimToNull(dto.getAttachmentUrl()));
        record.setNeedStudentConfirm(Boolean.TRUE.equals(dto.getNeedStudentConfirm()));
        record.setStudentConfirmed(false);
        record.setInterviewTime(dto.getInterviewTime());
        record.setInterviewAddress(trimToNull(dto.getInterviewAddress()));
        record.setIsRead(false);
        record.setCreatedTime(now);
        record.setUpdatedTime(now);
        communicationRecordMapper.insert(record);

        String beforeStatus = application.getStatus();
        if (COMMUNICATION_TYPE_INTERVIEW.equals(communicationType)
                && !STATUS_INTERVIEW_INVITED.equals(application.getStatus())
                && !"INTERVIEW_SCHEDULED".equals(application.getStatus())
                && !"INTERVIEW_FINISHED".equals(application.getStatus())) {
            jobApplicationMapper.updateApplicationStatus(
                    application.getId(),
                    enterpriseId,
                    STATUS_INTERVIEW_INVITED,
                    application.getRemark(),
                    false,
                    now
            );
            application.setStatus(STATUS_INTERVIEW_INVITED);
            talentApplicationSupportService.appendStatusLog(
                    application,
                    "STATUS_UPDATE",
                    beforeStatus,
                    STATUS_INTERVIEW_INVITED,
                    record.getId(),
                    userId,
                    RoleConstants.ENTERPRISE,
                    "企业发送面试邀约"
            );
        }

        jobApplicationMapper.updateApplicationAfterCommunication(
                application.getId(),
                record.getId(),
                communicationType,
                now,
                false,
                true,
                now
        );
        application.setLatestCommunicationId(record.getId());
        application.setLatestCommunicationType(communicationType);
        application.setLatestCommunicationTime(now);
        application.setReadByStudent(false);
        application.setReadByEnterprise(true);
        application.setUpdatedTime(now);
        talentApplicationSupportService.appendStatusLog(
                application,
                "COMMUNICATION_SEND",
                application.getStatus(),
                application.getStatus(),
                record.getId(),
                userId,
                RoleConstants.ENTERPRISE,
                "企业发起了一条沟通记录"
        );
        talentApplicationSupportService.syncApplicationContact(
                application,
                talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()),
                talentApplicationSupportService.parseResumeSnapshot(application.getResumeSnapshotJson()),
                extractStudentName(application)
        );

        Long studentUserId = talentApplicationSupportService.getStudentUserId(application.getStudentId());
        String messageTitle = COMMUNICATION_TYPE_INTERVIEW.equals(communicationType) ? "收到面试邀约" : "收到企业新沟通";
        String jobTitle = talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()) == null
                ? "岗位"
                : talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()).getJobTitle();
        talentApplicationSupportService.sendJobMessage(
                studentUserId,
                messageTitle,
                "岗位【" + jobTitle + "】有新的企业沟通，请及时查看。",
                record.getId(),
                "JOB_COMMUNICATION",
                userId,
                RoleConstants.ENTERPRISE
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getId());
        data.put("applicationId", application.getId());
        data.put("communicationType", communicationType);
        data.put("createdTime", record.getCreatedTime());
        return data;
    }

    @MethodPurpose("企业沟通分页")
    public IPage<TalentCommunicationPageVo> pageCommunication(CommunicationRecordPageQueryDto query, Long userId) {
        CommunicationRecordPageQueryDto safeQuery = query == null ? new CommunicationRecordPageQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        IPage<Map<String, Object>> rawPage = communicationRecordMapper.selectTalentCommunicationPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                enterpriseId,
                safeQuery.getApplicationId(),
                safeQuery.getJobId(),
                safeQuery.getStudentId(),
                normalizeCommunicationType(safeQuery.getCommunicationType()),
                safeQuery.getIsRead()
        );
        Page<TalentCommunicationPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toTalentCommunicationVo).toList());
        return targetPage;
    }

    @MethodPurpose("查询企业沟通详情")
    public CommunicationRecord getCommunicationDetail(Long recordId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        CommunicationRecord record = communicationRecordMapper.selectByEnterpriseAndId(enterpriseId, recordId);
        if (record == null) {
            throw new BizException(404, "沟通记录不存在");
        }
        return record;
    }

    @MethodPurpose("标记企业沟通已读")
    @Transactional(rollbackFor = Exception.class)
    public void markCommunicationRead(Long recordId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        CommunicationRecord record = communicationRecordMapper.selectByEnterpriseAndId(enterpriseId, recordId);
        if (record == null) {
            throw new BizException(404, "沟通记录不存在");
        }
        if (Boolean.TRUE.equals(record.getIsRead())) {
            return;
        }
        int affected = communicationRecordMapper.markReadByEnterpriseAndId(enterpriseId, recordId, LocalDateTime.now());
        if (affected <= 0) {
            throw new BizException(409, "标记已读失败");
        }
    }

    private JobPosting requireJob(Long enterpriseId, Long jobId) {
        if (jobId == null) {
            throw new BizException(400, "jobId不能为空");
        }
        JobPosting posting = jobPostingMapper.selectByEnterpriseAndId(enterpriseId, jobId);
        if (posting == null) {
            throw new BizException(404, "岗位不存在");
        }
        return posting;
    }

    private String extractStudentName(JobApplication application) {
        Map<String, Object> row = jobApplicationMapper.selectTalentApplicationStudentInfo(application.getId(), application.getEnterpriseId());
        return row == null ? null : toString(row.get("studentName"));
    }

    private TalentCommunicationPageVo toTalentCommunicationVo(Map<String, Object> row) {
        TalentCommunicationPageVo vo = new TalentCommunicationPageVo();
        vo.setRecordId(toLong(row.get("recordId")));
        vo.setApplicationId(toLong(row.get("applicationId")));
        vo.setApplicationNo(toString(row.get("applicationNo")));
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(toString(row.get("jobTitle")));
        vo.setStudentId(toLong(row.get("studentId")));
        vo.setStudentName(toString(row.get("studentName")));
        vo.setCommunicationType(toString(row.get("communicationType")));
        vo.setCommunicationSubject(toString(row.get("communicationSubject")));
        vo.setCommunicationContent(toString(row.get("communicationContent")));
        vo.setNeedStudentConfirm(toBoolean(row.get("needStudentConfirm")));
        vo.setStudentConfirmed(toBoolean(row.get("studentConfirmed")));
        vo.setIsRead(toBoolean(row.get("isRead")));
        vo.setReadTime(toDateTime(row.get("readTime")));
        vo.setCreatedTime(toDateTime(row.get("createdTime")));
        return vo;
    }

    private void validatePublishRequiredFields(
            String status,
            String workLocation,
            LocalDate applicationDeadline,
            String contactEmail,
            String contactPhone
    ) {
        if (!JOB_STATUS_PUBLISHED.equals(status)) {
            return;
        }
        if (!StringUtils.hasText(workLocation) || applicationDeadline == null
                || (!StringUtils.hasText(contactEmail) && !StringUtils.hasText(contactPhone))) {
            throw new BizException(400, "岗位发布前需完善地点、截止日期和联系方式");
        }
    }

    private String normalizeJobStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (JOB_STATUS_DRAFT.equals(normalized)
                || JOB_STATUS_PUBLISHED.equals(normalized)
                || JOB_STATUS_CLOSED.equals(normalized)
                || JOB_STATUS_EXPIRED.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String normalizeJobType(String jobType) {
        if (!StringUtils.hasText(jobType)) {
            return null;
        }
        String normalized = jobType.trim().toUpperCase();
        if (JOB_TYPE_FULL_TIME.equals(normalized)
                || JOB_TYPE_PART_TIME.equals(normalized)
                || JOB_TYPE_INTERNSHIP.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String normalizeCommunicationType(String communicationType) {
        if (!StringUtils.hasText(communicationType)) {
            return null;
        }
        String normalized = communicationType.trim().toUpperCase();
        if (COMMUNICATION_TYPE_EMAIL.equals(normalized)
                || COMMUNICATION_TYPE_PHONE.equals(normalized)
                || COMMUNICATION_TYPE_INTERVIEW.equals(normalized)
                || COMMUNICATION_TYPE_MESSAGE.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String defaultValue(String first, String second) {
        return first == null ? second : first;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
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

    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = String.valueOf(value);
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    private int toInt(Object value) {
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

    private LocalDate toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
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
