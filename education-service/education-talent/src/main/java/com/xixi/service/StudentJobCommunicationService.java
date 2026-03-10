package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.CommunicationRecord;
import com.xixi.entity.JobApplication;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.JobApplicationMapper;
import com.xixi.pojo.dto.talent.CommunicationConfirmDto;
import com.xixi.pojo.dto.talent.StudentCommunicationPageQueryDto;
import com.xixi.pojo.vo.talent.StudentCommunicationPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 学生端沟通服务。
 */
@Service
@RequiredArgsConstructor
public class StudentJobCommunicationService {
    private static final String STATUS_INTERVIEW_INVITED = "INTERVIEW_INVITED";
    private static final String STATUS_INTERVIEW_SCHEDULED = "INTERVIEW_SCHEDULED";

    private final CommunicationRecordMapper communicationRecordMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final TalentApplicationSupportService talentApplicationSupportService;

    public IPage<StudentCommunicationPageVo> myPage(StudentCommunicationPageQueryDto query, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        StudentCommunicationPageQueryDto safeQuery = query == null ? new StudentCommunicationPageQueryDto() : query;
        IPage<Map<String, Object>> rawPage = communicationRecordMapper.selectStudentCommunicationPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                studentId,
                safeQuery.getApplicationId(),
                safeQuery.getIsRead()
        );
        Page<StudentCommunicationPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toStudentCommunicationVo).toList());
        return targetPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirm(Long recordId, CommunicationConfirmDto dto, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        CommunicationRecord record = communicationRecordMapper.selectByStudentAndId(studentId, recordId);
        if (record == null) {
            throw new BizException(404, "沟通记录不存在");
        }
        if (!Boolean.TRUE.equals(record.getNeedStudentConfirm())) {
            throw new BizException(409, "当前沟通无需确认");
        }
        if (Boolean.TRUE.equals(record.getStudentConfirmed())) {
            throw new BizException(409, "当前沟通已确认");
        }

        LocalDateTime now = LocalDateTime.now();
        int affected = communicationRecordMapper.confirmByStudentAndId(
                studentId,
                recordId,
                dto == null ? null : trimToNull(dto.getConfirmRemark()),
                now,
                now
        );
        if (affected <= 0) {
            throw new BizException(409, "确认沟通失败");
        }

        if (record.getApplicationId() != null) {
            JobApplication application = jobApplicationMapper.selectByStudentAndId(studentId, record.getApplicationId());
            if (application != null) {
                String beforeStatus = application.getStatus();
                if (STATUS_INTERVIEW_INVITED.equals(application.getStatus())) {
                    jobApplicationMapper.updateApplicationStatus(
                            application.getId(),
                            application.getEnterpriseId(),
                            STATUS_INTERVIEW_SCHEDULED,
                            application.getRemark(),
                            true,
                            now
                    );
                    application.setStatus(STATUS_INTERVIEW_SCHEDULED);
                }
                jobApplicationMapper.updateApplicationAfterCommunication(
                        application.getId(),
                        record.getId(),
                        record.getCommunicationType(),
                        record.getCreatedTime(),
                        true,
                        false,
                        now
                );
                application.setLatestCommunicationId(record.getId());
                application.setLatestCommunicationType(record.getCommunicationType());
                application.setLatestCommunicationTime(record.getCreatedTime());
                application.setReadByStudent(true);
                application.setReadByEnterprise(false);
                application.setUpdatedTime(now);
                talentApplicationSupportService.appendStatusLog(
                        application,
                        "STUDENT_CONFIRM",
                        beforeStatus,
                        application.getStatus(),
                        record.getId(),
                        userId,
                        RoleConstants.STUDENT,
                        "学生确认了企业沟通"
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
                        "学生已确认沟通",
                        "投递单【" + application.getApplicationNo() + "】有新的学生确认，请及时查看。",
                        record.getId(),
                        "JOB_COMMUNICATION",
                        userId,
                        RoleConstants.STUDENT
                );
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", recordId);
        data.put("studentConfirmed", true);
        data.put("confirmTime", now);
        return data;
    }

    private StudentCommunicationPageVo toStudentCommunicationVo(Map<String, Object> row) {
        StudentCommunicationPageVo vo = new StudentCommunicationPageVo();
        vo.setRecordId(toLong(row.get("recordId")));
        vo.setApplicationId(toLong(row.get("applicationId")));
        vo.setApplicationNo(toString(row.get("applicationNo")));
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(toString(row.get("jobTitle")));
        vo.setEnterpriseId(toLong(row.get("enterpriseId")));
        vo.setEnterpriseName(toString(row.get("enterpriseName")));
        vo.setCommunicationType(toString(row.get("communicationType")));
        vo.setCommunicationSubject(toString(row.get("communicationSubject")));
        vo.setCommunicationContent(toString(row.get("communicationContent")));
        vo.setIsRead(toBoolean(row.get("isRead")));
        vo.setNeedStudentConfirm(toBoolean(row.get("needStudentConfirm")));
        vo.setStudentConfirmed(toBoolean(row.get("studentConfirmed")));
        vo.setInterviewTime(toDateTime(row.get("interviewTime")));
        vo.setInterviewAddress(toString(row.get("interviewAddress")));
        vo.setCreatedTime(toDateTime(row.get("createdTime")));
        return vo;
    }

    private String extractStudentName(JobApplication application) {
        Map<String, Object> row = jobApplicationMapper.selectTalentApplicationStudentInfo(application.getId(), application.getEnterpriseId());
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
