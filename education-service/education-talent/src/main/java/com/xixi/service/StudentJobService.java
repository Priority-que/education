package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.exception.BizException;
import com.xixi.mapper.JobPostingMapper;
import com.xixi.pojo.dto.talent.JobPublicPageQueryDto;
import com.xixi.pojo.vo.talent.EnterpriseSnapshotVo;
import com.xixi.pojo.vo.talent.JobPublicDetailVo;
import com.xixi.pojo.vo.talent.JobPublicPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生端岗位公开查询服务。
 */
@Service
@RequiredArgsConstructor
public class StudentJobService {
    private static final String JOB_STATUS_PUBLISHED = "PUBLISHED";

    private final JobPostingMapper jobPostingMapper;
    private final TalentApplicationSupportService talentApplicationSupportService;

    public IPage<JobPublicPageVo> publicPage(JobPublicPageQueryDto query, Long userId) {
        JobPublicPageQueryDto safeQuery = query == null ? new JobPublicPageQueryDto() : query;
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        IPage<Map<String, Object>> rawPage = jobPostingMapper.selectPublicJobPage(
                new Page<>(normalizePageNum(safeQuery.getPageNum()), normalizePageSize(safeQuery.getPageSize())),
                trimToNull(safeQuery.getKeyword()),
                trimToNull(safeQuery.getJobType()),
                trimToNull(safeQuery.getCity()),
                trimToNull(safeQuery.getEducationRequirement()),
                safeQuery.getOnlyOpen(),
                studentId
        );
        Page<JobPublicPageVo> targetPage = new Page<>(rawPage.getCurrent(), rawPage.getSize(), rawPage.getTotal());
        targetPage.setRecords(rawPage.getRecords().stream().map(this::toPublicPageVo).toList());
        return targetPage;
    }

    public JobPublicDetailVo publicDetail(Long jobId, Long userId) {
        Long studentId = talentApplicationSupportService.requireStudentId(userId);
        Map<String, Object> row = jobPostingMapper.selectPublicJobDetail(jobId, studentId);
        if (row == null || row.isEmpty()) {
            throw new BizException(404, "岗位不存在");
        }
        String status = toString(row.get("status"));
        if (!JOB_STATUS_PUBLISHED.equals(status)) {
            throw new BizException(404, "岗位不存在或未发布");
        }
        LocalDateTime deadline = toDateTime(row.get("applicationDeadline"));
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new BizException(404, "岗位已截止投递");
        }

        jobPostingMapper.incrementViewCount(jobId);

        JobPublicDetailVo detailVo = new JobPublicDetailVo();
        detailVo.setJobId(toLong(row.get("jobId")));
        detailVo.setJobTitle(toString(row.get("jobTitle")));
        detailVo.setJobType(toString(row.get("jobType")));
        detailVo.setJobCategory(toString(row.get("jobCategory")));
        detailVo.setWorkLocation(toString(row.get("workLocation")));
        detailVo.setSalaryRange(toString(row.get("salaryRange")));
        detailVo.setEducationRequirement(toString(row.get("educationRequirement")));
        detailVo.setExperienceRequirement(toString(row.get("experienceRequirement")));
        detailVo.setRecruitmentNumber(toInt(row.get("recruitmentNumber")));
        detailVo.setApplicationDeadline(deadline);
        detailVo.setContactEmail(toString(row.get("contactEmail")));
        detailVo.setContactPhone(toString(row.get("contactPhone")));
        detailVo.setJobDescription(toString(row.get("jobDescription")));
        detailVo.setRequirements(toString(row.get("requirements")));
        detailVo.setBenefits(toString(row.get("benefits")));
        detailVo.setStatus(status);
        detailVo.setApplyCount(toInt(row.get("applyCount")));
        detailVo.setViewCount(toInt(row.get("viewCount")) + 1);
        EnterpriseSnapshotVo enterpriseInfo = talentApplicationSupportService.buildEnterpriseSnapshot(row);
        detailVo.setEnterpriseInfo(enterpriseInfo);
        detailVo.setStudentApplied(toBoolean(row.get("studentApplied")));
        detailVo.setStudentApplicationId(toLong(row.get("studentApplicationId")));
        detailVo.setRecommendedResumeList(talentApplicationSupportService.listResumeOptions(studentId));

        JobPublicDetailVo.MatchInfo matchInfo = new JobPublicDetailVo.MatchInfo();
        matchInfo.setMatchScore(toInteger(row.get("matchScore")));
        matchInfo.setMatchReasons(List.of());
        matchInfo.setMissingRequirements(List.of());
        detailVo.setMatchInfo(matchInfo);
        return detailVo;
    }

    private JobPublicPageVo toPublicPageVo(Map<String, Object> row) {
        JobPublicPageVo vo = new JobPublicPageVo();
        vo.setJobId(toLong(row.get("jobId")));
        vo.setJobTitle(toString(row.get("jobTitle")));
        vo.setJobType(toString(row.get("jobType")));
        vo.setJobCategory(toString(row.get("jobCategory")));
        vo.setWorkLocation(toString(row.get("workLocation")));
        vo.setSalaryRange(toString(row.get("salaryRange")));
        vo.setEducationRequirement(toString(row.get("educationRequirement")));
        vo.setExperienceRequirement(toString(row.get("experienceRequirement")));
        vo.setRecruitmentNumber(toInt(row.get("recruitmentNumber")));
        vo.setApplicationDeadline(toDateTime(row.get("applicationDeadline")));
        vo.setStatus(toString(row.get("status")));
        vo.setApplyCount(toInt(row.get("applyCount")));
        vo.setViewCount(toInt(row.get("viewCount")));
        vo.setEnterpriseId(toLong(row.get("enterpriseId")));
        vo.setEnterpriseName(toString(row.get("enterpriseName")));
        vo.setEnterpriseLogo(toString(row.get("enterpriseLogo")));
        vo.setEnterpriseVerified(toBoolean(row.get("enterpriseVerified")));
        vo.setStudentApplied(toBoolean(row.get("studentApplied")));
        vo.setStudentApplicationId(toLong(row.get("studentApplicationId")));
        vo.setMatchScore(toInteger(row.get("matchScore")));
        vo.setPublishTime(toDateTime(row.get("publishTime")));
        return vo;
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

    private int toInt(Object value) {
        Integer number = toInteger(value);
        return number == null ? 0 : number;
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
        String text = String.valueOf(value);
        return "1".equals(text) || "true".equalsIgnoreCase(text);
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
