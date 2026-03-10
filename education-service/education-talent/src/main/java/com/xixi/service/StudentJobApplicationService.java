package com.xixi.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.JobApplication;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.JobApplicationMapper;
import com.xixi.mapper.JobPostingMapper;
import com.xixi.pojo.dto.talent.JobApplicationCreateDto;
import com.xixi.pojo.dto.talent.StudentJobApplicationPageQueryDto;
import com.xixi.pojo.vo.talent.EnterpriseSnapshotVo;
import com.xixi.pojo.vo.talent.JobApplicationCommunicationVo;
import com.xixi.pojo.vo.talent.JobApplicationCreateVo;
import com.xixi.pojo.vo.talent.JobApplicationTimelineVo;
import com.xixi.pojo.vo.talent.JobSnapshotVo;
import com.xixi.pojo.vo.talent.ResumeSnapshotVo;
import com.xixi.pojo.vo.talent.StudentJobApplicationDetailVo;
import com.xixi.pojo.vo.talent.StudentJobApplicationPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端投递服务。
 */
@Service
@RequiredArgsConstructor
public class StudentJobApplicationService {
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private final JobApplicationMapper jobApplicationMapper;
    private final JobPostingMapper jobPostingMapper;
    private final CommunicationRecordMapper communicationRecordMapper;
    private final TalentApplicationSupportService talentApplicationSupportService;

    @Transactional(rollbackFor = Exception.class)
    public JobApplicationCreateVo create(JobApplicationCreateDto dto, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        if (dto == null || dto.getJobId() == null || dto.getResumeId() == null) {
            throw new BizException(400, "jobId和resumeId不能为空");
        }

        int activeCount = jobApplicationMapper.countActiveByJobAndStudent(dto.getJobId(), studentId);
        if (activeCount > 0) {
            throw new BizException(409, "当前岗位已存在进行中的投递记录");
        }

        Map<String, Object> jobRow = jobPostingMapper.selectPublicJobDetail(dto.getJobId(), studentId);
        if (jobRow == null || jobRow.isEmpty()) {
            throw new BizException(404, "岗位不存在");
        }
        if (!"PUBLISHED".equals(toString(jobRow.get("status")))) {
            throw new BizException(409, "当前岗位不可投递");
        }
        LocalDateTime deadline = toDateTime(jobRow.get("applicationDeadline"));
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new BizException(409, "当前岗位已截止投递");
        }

        ResumeSnapshotVo resumeSnapshot = talentApplicationSupportService.buildResumeSnapshot(studentId, dto.getResumeId(), true);
        JobSnapshotVo jobSnapshot = talentApplicationSupportService.buildJobSnapshot(jobRow);
        EnterpriseSnapshotVo enterpriseSnapshot = talentApplicationSupportService.buildEnterpriseSnapshot(jobRow);

        LocalDateTime now = LocalDateTime.now();
        JobApplication application = new JobApplication();
        application.setApplicationNo(generateApplicationNo());
        application.setJobId(dto.getJobId());
        application.setStudentId(studentId);
        application.setResumeId(dto.getResumeId());
        application.setEnterpriseId(toLong(jobRow.get("enterpriseId")));
        application.setStatus(STATUS_SUBMITTED);
        application.setSourceType("STUDENT_APPLY");
        application.setReadByStudent(true);
        application.setReadByEnterprise(false);
        application.setJobSnapshotJson(JSONUtil.toJsonStr(jobSnapshot));
        application.setEnterpriseSnapshotJson(JSONUtil.toJsonStr(enterpriseSnapshot));
        application.setResumeSnapshotJson(JSONUtil.toJsonStr(resumeSnapshot));
        application.setSubmittedTime(now);
        application.setUpdatedTime(now);
        jobApplicationMapper.insert(application);

        jobPostingMapper.incrementApplyCount(dto.getJobId(), 1);
        talentApplicationSupportService.appendStatusLog(
                application,
                "CREATE",
                null,
                STATUS_SUBMITTED,
                null,
                userId,
                RoleConstants.STUDENT,
                "学生发起岗位投递"
        );
        talentApplicationSupportService.syncApplicationContact(application, jobSnapshot, resumeSnapshot, extractStudentName(application));

        Long enterpriseUserId = talentApplicationSupportService.getEnterpriseUserIdByJob(dto.getJobId());
        talentApplicationSupportService.sendJobMessage(
                enterpriseUserId,
                "收到新的岗位投递",
                "岗位【" + jobSnapshot.getJobTitle() + "】收到一条新的学生投递，请尽快查看。",
                application.getId(),
                "JOB_APPLICATION",
                userId,
                RoleConstants.STUDENT
        );

        JobApplicationCreateVo vo = new JobApplicationCreateVo();
        vo.setApplicationId(application.getId());
        vo.setApplicationNo(application.getApplicationNo());
        vo.setStatus(application.getStatus());
        vo.setSubmittedTime(application.getSubmittedTime());
        return vo;
    }

    public IPage<StudentJobApplicationPageVo> myPage(StudentJobApplicationPageQueryDto query, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        StudentJobApplicationPageQueryDto safeQuery = query == null ? new StudentJobApplicationPageQueryDto() : query;
        IPage<Map<String, Object>> rawPage = jobApplicationMapper.selectStudentApplicationPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                studentId,
                trimToNull(safeQuery.getStatus()),
                trimToNull(safeQuery.getKeyword())
        );
        Page<StudentJobApplicationPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toStudentPageVo).toList());
        return targetPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentJobApplicationDetailVo myDetail(Long applicationId, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        JobApplication application = requireStudentApplication(studentId, applicationId);
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(application.getReadByStudent())) {
            jobApplicationMapper.updateStudentReadFlag(applicationId, studentId, true);
            communicationRecordMapper.markReadByStudentAndApplication(studentId, applicationId, now);
            application.setReadByStudent(true);
        }

        StudentJobApplicationDetailVo detailVo = new StudentJobApplicationDetailVo();
        detailVo.setApplicationId(application.getId());
        detailVo.setApplicationNo(application.getApplicationNo());
        detailVo.setStatus(application.getStatus());
        detailVo.setSubmittedTime(application.getSubmittedTime());
        detailVo.setUpdatedTime(application.getUpdatedTime());
        detailVo.setJobSnapshot(talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()));
        detailVo.setEnterpriseSnapshot(talentApplicationSupportService.parseEnterpriseSnapshot(application.getEnterpriseSnapshotJson()));
        detailVo.setResumeSnapshot(talentApplicationSupportService.parseResumeSnapshot(application.getResumeSnapshotJson()));

        List<JobApplicationTimelineVo> timeline = talentApplicationSupportService.buildTimeline(applicationId, application.getStatus());
        List<JobApplicationCommunicationVo> communicationList = talentApplicationSupportService.listCommunication(applicationId);
        detailVo.setTimeline(timeline);
        detailVo.setCommunicationList(communicationList);

        StudentJobApplicationDetailVo.Actions actions = new StudentJobApplicationDetailVo.Actions();
        actions.setCanWithdraw(talentApplicationSupportService.canWithdraw(application.getStatus()));
        actions.setCanConfirmInterview(communicationList.stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getNeedStudentConfirm()) && !Boolean.TRUE.equals(item.getStudentConfirmed())));
        actions.setCanUploadAttachment(false);
        detailVo.setActions(actions);
        return detailVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> withdraw(Long applicationId, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        JobApplication application = requireStudentApplication(studentId, applicationId);
        if (!talentApplicationSupportService.canWithdraw(application.getStatus())) {
            throw new BizException(409, "当前投递状态不允许撤回");
        }
        LocalDateTime now = LocalDateTime.now();
        int affected = jobApplicationMapper.updateApplicationWithdraw(applicationId, studentId, STATUS_WITHDRAWN, false, now);
        if (affected <= 0) {
            throw new BizException(409, "撤回投递失败");
        }
        String beforeStatus = application.getStatus();
        application.setStatus(STATUS_WITHDRAWN);
        application.setUpdatedTime(now);
        talentApplicationSupportService.appendStatusLog(
                application,
                "WITHDRAW",
                beforeStatus,
                STATUS_WITHDRAWN,
                null,
                userId,
                RoleConstants.STUDENT,
                "学生撤回了投递"
        );
        talentApplicationSupportService.syncApplicationContact(
                application,
                talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()),
                talentApplicationSupportService.parseResumeSnapshot(application.getResumeSnapshotJson()),
                extractStudentName(application)
        );

        Long enterpriseUserId = talentApplicationSupportService.getEnterpriseUserIdByJob(application.getJobId());
        talentApplicationSupportService.sendJobMessage(
                enterpriseUserId,
                "学生撤回了投递",
                "投递单【" + application.getApplicationNo() + "】已被学生撤回。",
                application.getId(),
                "JOB_APPLICATION",
                userId,
                RoleConstants.STUDENT
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", application.getId());
        data.put("status", application.getStatus());
        data.put("updatedTime", application.getUpdatedTime());
        return data;
    }

    private JobApplication requireStudentApplication(Long studentId, Long applicationId) {
        if (applicationId == null) {
            throw new BizException(400, "applicationId不能为空");
        }
        JobApplication application = jobApplicationMapper.selectByStudentAndId(studentId, applicationId);
        if (application == null) {
            throw new BizException(404, "投递记录不存在");
        }
        return application;
    }

    private StudentJobApplicationPageVo toStudentPageVo(Map<String, Object> row) {
        StudentJobApplicationPageVo vo = new StudentJobApplicationPageVo();
        ResumeSnapshotVo resumeSnapshot = talentApplicationSupportService.parseResumeSnapshot(toString(row.get("resumeSnapshotJson")));
        JobSnapshotVo jobSnapshot = talentApplicationSupportService.parseJobSnapshot(toString(row.get("jobSnapshotJson")));
        EnterpriseSnapshotVo enterpriseSnapshot = talentApplicationSupportService.parseEnterpriseSnapshot(toString(row.get("enterpriseSnapshotJson")));
        vo.setApplicationId(toLong(row.get("applicationId")));
        vo.setApplicationNo(toString(row.get("applicationNo")));
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(jobSnapshot == null ? null : jobSnapshot.getJobTitle());
        vo.setEnterpriseId(toLong(row.get("enterpriseId")));
        vo.setEnterpriseName(enterpriseSnapshot == null ? null : enterpriseSnapshot.getEnterpriseName());
        vo.setResumeId(toLong(row.get("resumeId")));
        vo.setResumeTitle(resumeSnapshot == null ? null : resumeSnapshot.getResumeTitle());
        String status = toString(row.get("status"));
        vo.setStatus(status);
        vo.setStatusText(talentApplicationSupportService.statusText(status));
        vo.setLatestCommunicationType(toString(row.get("latestCommunicationType")));
        vo.setLatestCommunicationTime(toDateTime(row.get("latestCommunicationTime")));
        vo.setHasUnreadUpdate(!toBoolean(row.get("readByStudent")));
        vo.setSubmittedTime(toDateTime(row.get("submittedTime")));
        vo.setUpdatedTime(toDateTime(row.get("updatedTime")));
        return vo;
    }

    private String extractStudentName(JobApplication application) {
        Map<String, Object> row = jobApplicationMapper.selectTalentApplicationStudentInfo(application.getId(), application.getEnterpriseId());
        return row == null ? null : toString(row.get("studentName"));
    }

    private String generateApplicationNo() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String snowflake = IdUtil.getSnowflakeNextIdStr();
        String suffix = snowflake.length() > 6 ? snowflake.substring(snowflake.length() - 6) : snowflake;
        return "AP" + prefix + suffix;
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
