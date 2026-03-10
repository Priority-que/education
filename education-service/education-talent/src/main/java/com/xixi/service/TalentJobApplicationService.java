package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.JobApplication;
import com.xixi.exception.BizException;
import com.xixi.mapper.JobApplicationMapper;
import com.xixi.pojo.dto.talent.JobApplicationStatusUpdateDto;
import com.xixi.pojo.dto.talent.TalentJobApplicationPageQueryDto;
import com.xixi.pojo.vo.talent.JobApplicationCommunicationVo;
import com.xixi.pojo.vo.talent.JobApplicationTimelineVo;
import com.xixi.pojo.vo.talent.JobSnapshotVo;
import com.xixi.pojo.vo.talent.ResumeSnapshotVo;
import com.xixi.pojo.vo.talent.TalentApplicationStudentInfoVo;
import com.xixi.pojo.vo.talent.TalentJobApplicationDetailVo;
import com.xixi.pojo.vo.talent.TalentJobApplicationPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业端投递服务。
 */
@Service
@RequiredArgsConstructor
public class TalentJobApplicationService {
    private final EnterpriseIdentityService enterpriseIdentityService;
    private final JobApplicationMapper jobApplicationMapper;
    private final TalentApplicationSupportService talentApplicationSupportService;

    public IPage<TalentJobApplicationPageVo> page(TalentJobApplicationPageQueryDto query, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        TalentJobApplicationPageQueryDto safeQuery = query == null ? new TalentJobApplicationPageQueryDto() : query;
        IPage<Map<String, Object>> rawPage = jobApplicationMapper.selectTalentApplicationPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                enterpriseId,
                safeQuery.getJobId(),
                trimToNull(safeQuery.getStatus()),
                trimToNull(safeQuery.getKeyword())
        );
        Page<TalentJobApplicationPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toTalentApplicationPageVo).toList());
        return targetPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public TalentJobApplicationDetailVo detail(Long applicationId, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        JobApplication application = requireEnterpriseApplication(enterpriseId, applicationId);
        if (!Boolean.TRUE.equals(application.getReadByEnterprise())) {
            jobApplicationMapper.updateEnterpriseReadFlag(applicationId, enterpriseId, true);
            application.setReadByEnterprise(true);
        }

        TalentJobApplicationDetailVo detailVo = new TalentJobApplicationDetailVo();
        detailVo.setApplicationId(application.getId());
        detailVo.setApplicationNo(application.getApplicationNo());
        detailVo.setStatus(application.getStatus());
        detailVo.setRemark(application.getRemark());
        detailVo.setSubmittedTime(application.getSubmittedTime());
        detailVo.setUpdatedTime(application.getUpdatedTime());

        JobSnapshotVo jobSnapshot = talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson());
        ResumeSnapshotVo resumeSnapshot = talentApplicationSupportService.parseResumeSnapshot(application.getResumeSnapshotJson());
        detailVo.setJobInfo(jobSnapshot);
        detailVo.setResumeInfo(resumeSnapshot);
        detailVo.setCertificateList(resumeSnapshot == null || resumeSnapshot.getCertificateList() == null
                ? List.of()
                : resumeSnapshot.getCertificateList());

        Map<String, Object> studentRow = jobApplicationMapper.selectTalentApplicationStudentInfo(applicationId, enterpriseId);
        TalentApplicationStudentInfoVo studentInfoVo = new TalentApplicationStudentInfoVo();
        studentInfoVo.setStudentId(toLong(studentRow == null ? null : studentRow.get("studentId")));
        studentInfoVo.setStudentName(toString(studentRow == null ? null : studentRow.get("studentName")));
        studentInfoVo.setStudentNumber(toString(studentRow == null ? null : studentRow.get("studentNumber")));
        studentInfoVo.setAvatarUrl(toString(studentRow == null ? null : studentRow.get("avatarUrl")));
        studentInfoVo.setMajor(toString(studentRow == null ? null : studentRow.get("major")));
        studentInfoVo.setDegree(toString(studentRow == null ? null : studentRow.get("degree")));
        studentInfoVo.setSchoolName(toString(studentRow == null ? null : studentRow.get("schoolName")));
        detailVo.setStudentInfo(studentInfoVo);

        List<JobApplicationCommunicationVo> communicationList = talentApplicationSupportService.listCommunication(applicationId);
        List<JobApplicationTimelineVo> timeline = talentApplicationSupportService.buildTimeline(applicationId, application.getStatus());
        detailVo.setCommunicationList(communicationList);
        detailVo.setTimeline(timeline);
        return detailVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateStatus(Long applicationId, JobApplicationStatusUpdateDto dto, Long userId) {
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        JobApplication application = requireEnterpriseApplication(enterpriseId, applicationId);
        if (dto == null || !StringUtils.hasText(dto.getStatus())) {
            throw new BizException(400, "status不能为空");
        }
        String targetStatus = talentApplicationSupportService.normalizeApplicationStatus(dto.getStatus());
        if (targetStatus == null || !talentApplicationSupportService.canEnterpriseUpdateTo(targetStatus)) {
            throw new BizException(400, "status不合法");
        }
        if (targetStatus.equals(application.getStatus())) {
            throw new BizException(409, "投递状态未发生变化");
        }

        LocalDateTime now = LocalDateTime.now();
        String beforeStatus = application.getStatus();
        int affected = jobApplicationMapper.updateApplicationStatus(
                applicationId,
                enterpriseId,
                targetStatus,
                dto.getRemark(),
                false,
                now
        );
        if (affected <= 0) {
            throw new BizException(409, "更新投递状态失败");
        }

        application.setStatus(targetStatus);
        application.setRemark(trimToNull(dto.getRemark()));
        application.setReadByStudent(false);
        application.setUpdatedTime(now);
        talentApplicationSupportService.appendStatusLog(
                application,
                "STATUS_UPDATE",
                beforeStatus,
                targetStatus,
                null,
                userId,
                RoleConstants.ENTERPRISE,
                "企业更新了投递状态"
        );
        talentApplicationSupportService.syncApplicationContact(
                application,
                talentApplicationSupportService.parseJobSnapshot(application.getJobSnapshotJson()),
                talentApplicationSupportService.parseResumeSnapshot(application.getResumeSnapshotJson()),
                extractStudentName(applicationId, enterpriseId)
        );

        Long studentUserId = talentApplicationSupportService.getStudentUserId(application.getStudentId());
        talentApplicationSupportService.sendJobMessage(
                studentUserId,
                "投递状态已更新",
                "投递单【" + application.getApplicationNo() + "】状态已更新为【" + talentApplicationSupportService.statusText(targetStatus) + "】。",
                application.getId(),
                "JOB_APPLICATION",
                userId,
                RoleConstants.ENTERPRISE
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", application.getId());
        data.put("status", application.getStatus());
        data.put("updatedTime", application.getUpdatedTime());
        return data;
    }

    private JobApplication requireEnterpriseApplication(Long enterpriseId, Long applicationId) {
        if (applicationId == null) {
            throw new BizException(400, "applicationId不能为空");
        }
        JobApplication application = jobApplicationMapper.selectByEnterpriseAndId(enterpriseId, applicationId);
        if (application == null) {
            throw new BizException(404, "投递记录不存在");
        }
        return application;
    }

    private TalentJobApplicationPageVo toTalentApplicationPageVo(Map<String, Object> row) {
        TalentJobApplicationPageVo vo = new TalentJobApplicationPageVo();
        ResumeSnapshotVo resumeSnapshot = talentApplicationSupportService.parseResumeSnapshot(toString(row.get("resumeSnapshotJson")));
        JobSnapshotVo jobSnapshot = talentApplicationSupportService.parseJobSnapshot(toString(row.get("jobSnapshotJson")));
        vo.setApplicationId(toLong(row.get("applicationId")));
        vo.setApplicationNo(toString(row.get("applicationNo")));
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(jobSnapshot == null ? null : jobSnapshot.getJobTitle());
        vo.setStudentId(toLong(row.get("studentId")));
        vo.setStudentName(toString(row.get("studentName")));
        vo.setStudentNumber(toString(row.get("studentNumber")));
        vo.setResumeId(toLong(row.get("resumeId")));
        vo.setResumeTitle(resumeSnapshot == null ? null : resumeSnapshot.getResumeTitle());
        vo.setStatus(toString(row.get("status")));
        vo.setMatchScore(toInteger(row.get("matchScore")));
        vo.setCertificateVerifiedCount(resumeSnapshot == null || resumeSnapshot.getCertificateList() == null
                ? 0
                : (int) resumeSnapshot.getCertificateList().stream().filter(item -> Boolean.TRUE.equals(item.getVerified())).count());
        vo.setLatestCommunicationType(toString(row.get("latestCommunicationType")));
        vo.setLatestCommunicationTime(toDateTime(row.get("latestCommunicationTime")));
        vo.setSubmittedTime(toDateTime(row.get("submittedTime")));
        vo.setUpdatedTime(toDateTime(row.get("updatedTime")));
        return vo;
    }

    private String extractStudentName(Long applicationId, Long enterpriseId) {
        Map<String, Object> row = jobApplicationMapper.selectTalentApplicationStudentInfo(applicationId, enterpriseId);
        return row == null ? null : toString(row.get("studentName"));
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

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
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
