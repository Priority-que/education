package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeCertificate;
import com.xixi.entity.ResumeEducation;
import com.xixi.entity.ResumeExperience;
import com.xixi.entity.ResumeProject;
import com.xixi.entity.ResumeSkill;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeCertificateMapper;
import com.xixi.mapper.ResumeEducationMapper;
import com.xixi.mapper.ResumeExperienceMapper;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mapper.ResumeProjectMapper;
import com.xixi.mapper.ResumeSkillMapper;
import com.xixi.mq.ResumePublicAccessEventProducer;
import com.xixi.openfeign.certificate.EducationCertificateResumeClient;
import com.xixi.pojo.vo.resume.CertificateInternalStudentLiteVo;
import com.xixi.pojo.query.resume.PublicResumeQuery;
import com.xixi.pojo.vo.resume.PublicResumeCertificateVo;
import com.xixi.pojo.vo.resume.PublicResumeDetailVo;
import com.xixi.pojo.vo.resume.PublicResumeOptionVo;
import com.xixi.pojo.vo.resume.PublicResumePageVo;
import com.xixi.pojo.vo.resume.PublicStudentResumePageVo;
import com.xixi.web.Result;
import com.xixi.service.PublicResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公开简历服务实现（6.1、6.2）。
 */
@Service
@RequiredArgsConstructor
public class PublicResumeServiceImpl implements PublicResumeService {
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final ResumeMapper resumeMapper;
    private final ResumeEducationMapper resumeEducationMapper;
    private final ResumeExperienceMapper resumeExperienceMapper;
    private final ResumeProjectMapper resumeProjectMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final ResumeCertificateMapper resumeCertificateMapper;
    private final EducationCertificateResumeClient educationCertificateResumeClient;
    private final ResumePublicAccessEventProducer resumePublicAccessEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("6.1：查询公开简历详情，仅允许 visibility=PUBLIC 且 status=1，并递增浏览次数")
    public PublicResumeDetailVo getPublicResumeDetail(Long resumeId, Long viewerId, Integer viewerRole) {
        Resume resume = requirePublicResume(resumeId);

        int affected = resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, resumeId)
                        .eq(Resume::getVisibility, VISIBILITY_PUBLIC)
                        .eq(Resume::getStatus, true)
                        .setSql("view_count = IFNULL(view_count,0) + 1")
                        .set(Resume::getUpdatedTime, LocalDateTime.now())
        );
        if (affected <= 0) {
            throw new BizException(409, "公开简历浏览计数更新失败");
        }
        resume.setViewCount((resume.getViewCount() == null ? 0 : resume.getViewCount()) + 1);

        PublicResumeDetailVo detailVo = new PublicResumeDetailVo();
        BeanUtil.copyProperties(resume, detailVo);
        detailVo.setEducationList(queryEducationList(resumeId));
        detailVo.setExperienceList(queryExperienceList(resumeId));
        detailVo.setProjectList(queryProjectList(resumeId));
        detailVo.setSkillList(querySkillList(resumeId));
        detailVo.setCertificateList(queryCertificateList(resumeId, resume.getStudentId()));
        detailVo.setPublicResumeOptions(queryPublicResumeOptions(resume.getStudentId()));

        resumePublicAccessEventProducer.publishDetailViewed(resumeId, viewerId, viewerRole);
        return detailVo;
    }

    @Override
    @MethodPurpose("6.2：企业分页查询公开简历，支持关键词、专业、学历过滤")
    public IPage<PublicResumePageVo> getPublicResumePage(PublicResumeQuery query, Long viewerId) {
        PublicResumeQuery safeQuery = query == null ? new PublicResumeQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();

        Set<Long> candidateIds = null;
        Set<Long> keywordIds = queryKeywordMatchedResumeIds(safeQuery.getKeyword());
        if (keywordIds != null) {
            candidateIds = keywordIds;
        }
        Set<Long> educationFilterIds = queryEducationMatchedResumeIds(safeQuery.getMajor(), safeQuery.getDegree());
        if (educationFilterIds != null) {
            candidateIds = intersect(candidateIds, educationFilterIds);
        }

        if (candidateIds != null && candidateIds.isEmpty()) {
            Page<PublicResumePageVo> emptyPage = new Page<>(pageNum, pageSize, 0);
            emptyPage.setRecords(new ArrayList<>());
            resumePublicAccessEventProducer.publishPageSearched(
                    viewerId, trimToNull(safeQuery.getKeyword()), trimToNull(safeQuery.getMajor()),
                    trimToNull(safeQuery.getDegree()), (int) pageNum, (int) pageSize
            );
            return emptyPage;
        }

        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<Resume>()
                .eq(Resume::getVisibility, VISIBILITY_PUBLIC)
                .eq(Resume::getStatus, true);
        if (candidateIds != null) {
            wrapper.in(Resume::getId, candidateIds);
        }
        wrapper.orderByDesc(Resume::getViewCount)
                .orderByDesc(Resume::getUpdatedTime)
                .orderByDesc(Resume::getCreatedTime);

        Page<Resume> entityPage = resumeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Long> resumeIds = entityPage.getRecords().stream().map(Resume::getId).toList();
        Map<Long, ResumeEducation> firstEducationMap = queryFirstEducationMap(resumeIds);
        Map<Long, String> skillSummaryMap = querySkillSummaryMap(resumeIds);

        Page<PublicResumePageVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(resume -> toPageVo(resume, firstEducationMap.get(resume.getId()), skillSummaryMap.get(resume.getId())))
                .toList());

        resumePublicAccessEventProducer.publishPageSearched(
                viewerId, trimToNull(safeQuery.getKeyword()), trimToNull(safeQuery.getMajor()),
                trimToNull(safeQuery.getDegree()), (int) pageNum, (int) pageSize
        );
        return voPage;
    }

    @Override
    @MethodPurpose("学生维度公开候选分页查询：每个学生仅返回一份主展示公开简历")
    public IPage<PublicStudentResumePageVo> getPublicStudentPage(PublicResumeQuery query, Long viewerId) {
        PublicResumeQuery safeQuery = query == null ? new PublicResumeQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();

        String keyword = trimToNull(safeQuery.getKeyword());
        String major = trimToNull(safeQuery.getMajor());
        String degree = trimToNull(safeQuery.getDegree());

        Long totalValue = resumeMapper.countPublicStudentTotal(keyword, major, degree);
        long total = totalValue == null ? 0L : totalValue;
        Page<PublicStudentResumePageVo> voPage = new Page<>(pageNum, pageSize, total);
        if (total <= 0) {
            voPage.setRecords(new ArrayList<>());
            resumePublicAccessEventProducer.publishPageSearched(
                    viewerId, keyword, major, degree, (int) pageNum, (int) pageSize
            );
            return voPage;
        }

        long offset = (pageNum - 1) * pageSize;
        List<Resume> resumeList = resumeMapper.selectPublicStudentPage(offset, pageSize, keyword, major, degree);
        List<Long> resumeIds = resumeList.stream().map(Resume::getId).toList();
        Map<Long, ResumeEducation> firstEducationMap = queryFirstEducationMap(resumeIds);
        Map<Long, String> skillSummaryMap = querySkillSummaryMap(resumeIds);

        voPage.setRecords(resumeList.stream()
                .map(resume -> toStudentPageVo(
                        resume,
                        firstEducationMap.get(resume.getId()),
                        skillSummaryMap.get(resume.getId())
                ))
                .toList());

        resumePublicAccessEventProducer.publishPageSearched(
                viewerId, keyword, major, degree, (int) pageNum, (int) pageSize
        );
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("学生维度公开简历详情：按学生ID选主展示简历并返回详情")
    public PublicResumeDetailVo getPublicStudentDetail(Long studentId, Long resumeId, Long viewerId, Integer viewerRole) {
        if (resumeId != null) {
            if (studentId == null) {
                throw new BizException(400, "studentId不能为空");
            }
            Resume selectedResume = resumeMapper.selectPublicResumeByStudentIdAndResumeId(studentId, resumeId);
            if (selectedResume == null) {
                throw new BizException(404, "公开简历不存在");
            }
            return getPublicResumeDetail(selectedResume.getId(), viewerId, viewerRole);
        }
        if (studentId == null) {
            throw new BizException(400, "studentId不能为空");
        }
        Resume primaryResume = resumeMapper.selectPrimaryPublicResumeByStudentId(studentId);
        if (primaryResume == null) {
            throw new BizException(404, "公开简历不存在");
        }
        return getPublicResumeDetail(primaryResume.getId(), viewerId, viewerRole);
    }

    @MethodPurpose("查询公开简历并做公开状态校验")
    private Resume requirePublicResume(Long resumeId) {
        if (resumeId == null) {
            throw new BizException(400, "简历ID不能为空");
        }
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BizException(404, "简历不存在");
        }
        if (!VISIBILITY_PUBLIC.equals(resume.getVisibility()) || !Boolean.TRUE.equals(resume.getStatus())) {
            throw new BizException(404, "公开简历不存在");
        }
        return resume;
    }

    @MethodPurpose("按关键词匹配简历标题或技能，返回简历ID集合")
    private Set<Long> queryKeywordMatchedResumeIds(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String likeKeyword = keyword.trim();

        Set<Long> titleIds = resumeMapper.selectList(new LambdaQueryWrapper<Resume>()
                        .select(Resume::getId)
                        .eq(Resume::getVisibility, VISIBILITY_PUBLIC)
                        .eq(Resume::getStatus, true)
                        .like(Resume::getResumeTitle, likeKeyword))
                .stream()
                .map(Resume::getId)
                .collect(Collectors.toSet());

        Set<Long> skillResumeIds = resumeSkillMapper.selectList(new LambdaQueryWrapper<ResumeSkill>()
                        .select(ResumeSkill::getResumeId)
                        .like(ResumeSkill::getSkillName, likeKeyword))
                .stream()
                .map(ResumeSkill::getResumeId)
                .collect(Collectors.toSet());

        Set<Long> result = new HashSet<>();
        result.addAll(titleIds);
        result.addAll(skillResumeIds);
        return result;
    }

    @MethodPurpose("按专业和学历过滤，返回匹配的简历ID集合")
    private Set<Long> queryEducationMatchedResumeIds(String major, String degree) {
        boolean hasMajor = StringUtils.hasText(major);
        boolean hasDegree = StringUtils.hasText(degree);
        if (!hasMajor && !hasDegree) {
            return null;
        }
        LambdaQueryWrapper<ResumeEducation> wrapper = new LambdaQueryWrapper<ResumeEducation>()
                .select(ResumeEducation::getResumeId);
        if (hasMajor) {
            wrapper.like(ResumeEducation::getMajor, major.trim());
        }
        if (hasDegree) {
            wrapper.eq(ResumeEducation::getDegree, degree.trim().toUpperCase());
        }
        return resumeEducationMapper.selectList(wrapper)
                .stream()
                .map(ResumeEducation::getResumeId)
                .collect(Collectors.toSet());
    }

    @MethodPurpose("求两个简历ID集合交集（其中一个为空时返回另一个）")
    private Set<Long> intersect(Set<Long> left, Set<Long> right) {
        if (left == null) {
            return right == null ? null : new HashSet<>(right);
        }
        if (right == null) {
            return new HashSet<>(left);
        }
        Set<Long> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection;
    }

    @MethodPurpose("查询教育经历列表（按排序字段升序）")
    private List<ResumeEducation> queryEducationList(Long resumeId) {
        return resumeEducationMapper.selectList(new LambdaQueryWrapper<ResumeEducation>()
                .eq(ResumeEducation::getResumeId, resumeId)
                .orderByAsc(ResumeEducation::getSortOrder, ResumeEducation::getCreatedTime));
    }

    @MethodPurpose("查询工作经历列表（按排序字段升序）")
    private List<ResumeExperience> queryExperienceList(Long resumeId) {
        return resumeExperienceMapper.selectList(new LambdaQueryWrapper<ResumeExperience>()
                .eq(ResumeExperience::getResumeId, resumeId)
                .orderByAsc(ResumeExperience::getSortOrder, ResumeExperience::getCreatedTime));
    }

    @MethodPurpose("查询项目经历列表（按排序字段升序）")
    private List<ResumeProject> queryProjectList(Long resumeId) {
        return resumeProjectMapper.selectList(new LambdaQueryWrapper<ResumeProject>()
                .eq(ResumeProject::getResumeId, resumeId)
                .orderByAsc(ResumeProject::getSortOrder, ResumeProject::getCreatedTime));
    }

    @MethodPurpose("查询技能列表（按排序字段升序）")
    private List<ResumeSkill> querySkillList(Long resumeId) {
        return resumeSkillMapper.selectList(new LambdaQueryWrapper<ResumeSkill>()
                .eq(ResumeSkill::getResumeId, resumeId)
                .orderByAsc(ResumeSkill::getSortOrder, ResumeSkill::getCreatedTime));
    }

    @MethodPurpose("查询简历证书关联列表（按排序字段升序）")
    private List<PublicResumeCertificateVo> queryCertificateList(Long resumeId, Long studentId) {
        List<ResumeCertificate> relationList = resumeCertificateMapper.selectList(new LambdaQueryWrapper<ResumeCertificate>()
                .eq(ResumeCertificate::getResumeId, resumeId)
                .orderByAsc(ResumeCertificate::getSortOrder, ResumeCertificate::getCreatedTime));
        if (relationList.isEmpty()) {
            return List.of();
        }

        Map<Long, CertificateInternalStudentLiteVo> certificateMap = queryStudentCertificateMap(studentId);
        List<PublicResumeCertificateVo> result = new ArrayList<>(relationList.size());
        for (ResumeCertificate relation : relationList) {
            PublicResumeCertificateVo vo = new PublicResumeCertificateVo();
            vo.setId(relation.getId());
            vo.setResumeId(relation.getResumeId());
            vo.setCertificateId(relation.getCertificateId());
            vo.setSortOrder(relation.getSortOrder());
            vo.setCreatedTime(relation.getCreatedTime());

            CertificateInternalStudentLiteVo certificate = certificateMap.get(relation.getCertificateId());
            if (certificate != null) {
                vo.setCertificateName(certificate.getCertificateName());
                vo.setCertificateNumber(certificate.getCertificateNumber());
                vo.setIssuingAuthority(certificate.getIssuingAuthority());
                vo.setIssuingDate(certificate.getIssuingDate());
                vo.setExpiryDate(certificate.getExpiryDate());
                vo.setValidityPeriodText(buildValidityPeriodText(certificate.getIssuingDate(), certificate.getExpiryDate()));
                vo.setStatus(certificate.getStatus());
                vo.setFileUrl(certificate.getFileUrl());
                vo.setThumbnailUrl(certificate.getThumbnailUrl());
            }
            result.add(vo);
        }
        return result;
    }

    private Map<Long, CertificateInternalStudentLiteVo> queryStudentCertificateMap(Long studentId) {
        if (studentId == null) {
            return Collections.emptyMap();
        }
        try {
            Result remote = educationCertificateResumeClient.listStudentCertificates(studentId, null);
            if (remote == null || remote.getCode() == null || remote.getCode() != 200 || remote.getData() == null) {
                return Collections.emptyMap();
            }
            Object data = remote.getData();
            if (!(data instanceof List<?> items)) {
                return Collections.emptyMap();
            }
            Map<Long, CertificateInternalStudentLiteVo> map = new HashMap<>();
            for (Object item : items) {
                CertificateInternalStudentLiteVo vo = JSONUtil.toBean(JSONUtil.toJsonStr(item), CertificateInternalStudentLiteVo.class);
                if (vo.getId() != null) {
                    map.put(vo.getId(), vo);
                }
            }
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String buildValidityPeriodText(LocalDate issuingDate, LocalDate expiryDate) {
        if (issuingDate == null && expiryDate == null) {
            return null;
        }
        if (issuingDate == null) {
            return "至 " + expiryDate;
        }
        if (expiryDate == null) {
            return issuingDate + " 起长期有效";
        }
        return issuingDate + " - " + expiryDate;
    }

    @MethodPurpose("批量查询每份简历首条教育信息（用于列表展示学历/专业）")
    private Map<Long, ResumeEducation> queryFirstEducationMap(List<Long> resumeIds) {
        if (resumeIds == null || resumeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ResumeEducation> educationList = resumeEducationMapper.selectList(new LambdaQueryWrapper<ResumeEducation>()
                .in(ResumeEducation::getResumeId, resumeIds)
                .orderByAsc(ResumeEducation::getSortOrder, ResumeEducation::getCreatedTime));
        Map<Long, ResumeEducation> map = new HashMap<>();
        for (ResumeEducation education : educationList) {
            map.putIfAbsent(education.getResumeId(), education);
        }
        return map;
    }

    @MethodPurpose("批量查询并拼接技能摘要（最多取前3项）")
    private Map<Long, String> querySkillSummaryMap(List<Long> resumeIds) {
        if (resumeIds == null || resumeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ResumeSkill> skillList = resumeSkillMapper.selectList(new LambdaQueryWrapper<ResumeSkill>()
                .in(ResumeSkill::getResumeId, resumeIds)
                .orderByAsc(ResumeSkill::getSortOrder, ResumeSkill::getCreatedTime));

        Map<Long, LinkedHashSet<String>> skillSetMap = new HashMap<>();
        for (ResumeSkill skill : skillList) {
            if (!StringUtils.hasText(skill.getSkillName())) {
                continue;
            }
            skillSetMap.computeIfAbsent(skill.getResumeId(), k -> new LinkedHashSet<>())
                    .add(skill.getSkillName().trim());
        }

        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, LinkedHashSet<String>> entry : skillSetMap.entrySet()) {
            String summary = entry.getValue().stream().limit(3).collect(Collectors.joining(" / "));
            result.put(entry.getKey(), summary);
        }
        return result;
    }

    @MethodPurpose("将简历实体转为公开分页项")
    private PublicResumePageVo toPageVo(Resume resume, ResumeEducation education, String skillSummary) {
        PublicResumePageVo vo = new PublicResumePageVo();
        vo.setId(resume.getId());
        vo.setResumeTitle(resume.getResumeTitle());
        vo.setAvatarUrl(resume.getAvatarUrl());
        vo.setCareerObjective(resume.getCareerObjective());
        vo.setViewCount(resume.getViewCount());
        vo.setUpdatedTime(resume.getUpdatedTime());
        vo.setSkillSummary(skillSummary);
        if (education != null) {
            vo.setMajor(education.getMajor());
            vo.setDegree(education.getDegree());
        }
        return vo;
    }

    @MethodPurpose("将简历实体转为学生维度分页项")
    private PublicStudentResumePageVo toStudentPageVo(Resume resume, ResumeEducation education, String skillSummary) {
        PublicStudentResumePageVo vo = new PublicStudentResumePageVo();
        vo.setId(resume.getStudentId());
        vo.setStudentId(resume.getStudentId());
        vo.setResumeId(resume.getId());
        vo.setResumeTitle(resume.getResumeTitle());
        vo.setAvatarUrl(resume.getAvatarUrl());
        vo.setCareerObjective(resume.getCareerObjective());
        vo.setViewCount(resume.getViewCount());
        vo.setUpdatedTime(resume.getUpdatedTime());
        vo.setSkillSummary(skillSummary);
        if (education != null) {
            vo.setMajor(education.getMajor());
            vo.setDegree(education.getDegree());
        }
        return vo;
    }

    @MethodPurpose("按学生查询全部公开简历选项")
    private List<PublicResumeOptionVo> queryPublicResumeOptions(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return resumeMapper.selectPublicResumesByStudentId(studentId).stream()
                .map(resume -> {
                    PublicResumeOptionVo vo = new PublicResumeOptionVo();
                    vo.setResumeId(resume.getId());
                    vo.setResumeTitle(resume.getResumeTitle());
                    vo.setIsDefault(Boolean.TRUE.equals(resume.getIsDefault()));
                    vo.setUpdatedTime(resume.getUpdatedTime());
                    return vo;
                })
                .toList();
    }

    @MethodPurpose("字符串去空并返回 null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
