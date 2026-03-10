package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.xixi.entity.ResumeViewLog;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeCertificateMapper;
import com.xixi.mapper.ResumeEducationMapper;
import com.xixi.mapper.ResumeExperienceMapper;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mapper.ResumeProjectMapper;
import com.xixi.mapper.ResumeSkillMapper;
import com.xixi.mapper.ResumeViewLogMapper;
import com.xixi.mq.ResumeChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeCreateDto;
import com.xixi.pojo.dto.resume.ResumeUpdateDto;
import com.xixi.pojo.query.resume.ResumeQuery;
import com.xixi.pojo.vo.resume.ResumeVo;
import com.xixi.service.ResumeService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 简历主档服务实现（5.1~5.6）。
 */
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {
    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";

    private final ResumeMapper resumeMapper;
    private final ResumeEducationMapper resumeEducationMapper;
    private final ResumeExperienceMapper resumeExperienceMapper;
    private final ResumeProjectMapper resumeProjectMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final ResumeCertificateMapper resumeCertificateMapper;
    private final ResumeViewLogMapper resumeViewLogMapper;
    private final ResumeChangedEventProducer resumeChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("5.1：学生创建简历主档，首次简历自动设为默认简历")
    public Result createResume(ResumeCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);

        long myResumeCount = resumeMapper.selectCount(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, validStudentId));

        Resume resume = new Resume();
        resume.setStudentId(validStudentId);
        resume.setResumeTitle(dto.getResumeTitle().trim());
        resume.setResumeTemplate(StringUtils.hasText(dto.getResumeTemplate()) ? dto.getResumeTemplate().trim() : "DEFAULT");
        resume.setAvatarUrl(trimToNull(dto.getAvatarUrl()));
        resume.setContactEmail(trimToNull(dto.getContactEmail()));
        resume.setContactPhone(trimToNull(dto.getContactPhone()));
        resume.setCareerObjective(trimToNull(dto.getCareerObjective()));
        resume.setSelfIntroduction(trimToNull(dto.getSelfIntroduction()));
        resume.setVisibility(normalizeVisibility(dto.getVisibility(), VISIBILITY_PRIVATE));
        resume.setViewCount(0);
        resume.setDownloadCount(0);
        resume.setIsDefault(myResumeCount == 0);
        resume.setStatus(true);
        resume.setCreatedTime(LocalDateTime.now());
        resume.setUpdatedTime(LocalDateTime.now());
        resumeMapper.insert(resume);

        resumeChangedEventProducer.publish(
                "CREATE",
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
        return Result.success("创建简历成功", Map.of("id", resume.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("5.2：学生修改自己的简历主档字段")
    public Result updateResume(ResumeUpdateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "简历ID不能为空");
        }

        Resume resume = requireOwnedResume(dto.getId(), validStudentId);
        if (StringUtils.hasText(dto.getResumeTitle())) {
            resume.setResumeTitle(dto.getResumeTitle().trim());
        }
        if (dto.getResumeTemplate() != null) {
            resume.setResumeTemplate(trimToNull(dto.getResumeTemplate()));
        }
        if (dto.getAvatarUrl() != null) {
            resume.setAvatarUrl(trimToNull(dto.getAvatarUrl()));
        }
        if (dto.getContactEmail() != null) {
            resume.setContactEmail(trimToNull(dto.getContactEmail()));
        }
        if (dto.getContactPhone() != null) {
            resume.setContactPhone(trimToNull(dto.getContactPhone()));
        }
        if (dto.getWechat() != null) {
            resume.setWechat(trimToNull(dto.getWechat()));
        }
        if (dto.getLinkedin() != null) {
            resume.setLinkedin(trimToNull(dto.getLinkedin()));
        }
        if (dto.getGithub() != null) {
            resume.setGithub(trimToNull(dto.getGithub()));
        }
        if (dto.getSelfIntroduction() != null) {
            resume.setSelfIntroduction(trimToNull(dto.getSelfIntroduction()));
        }
        if (dto.getCareerObjective() != null) {
            resume.setCareerObjective(trimToNull(dto.getCareerObjective()));
        }
        resume.setUpdatedTime(LocalDateTime.now());
        resumeMapper.updateById(resume);

        resumeChangedEventProducer.publish(
                "UPDATE",
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
        return Result.success("修改简历成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("5.3：学生删除自己的简历，并级联删除子表数据")
    public Result deleteResume(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        Resume resume = requireOwnedResume(resumeId, validStudentId);

        deleteResumeChildren(resumeId);
        resumeMapper.deleteById(resumeId);

        if (Boolean.TRUE.equals(resume.getIsDefault())) {
            setAnotherResumeAsDefault(validStudentId);
        }

        resumeChangedEventProducer.publish(
                "DELETE",
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
        return Result.success("删除简历成功");
    }

    @Override
    @MethodPurpose("5.4-扩展：查询当前学生指定简历详情")
    public ResumeVo getResumeDetail(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        Resume resume = requireOwnedResume(resumeId, validStudentId);
        return toVo(resume);
    }

    @Override
    @MethodPurpose("5.4：分页查询当前学生的简历列表")
    public IPage<ResumeVo> getMyResumePage(ResumeQuery query, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        ResumeQuery safeQuery = query == null ? new ResumeQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();

        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, validStudentId);
        if (StringUtils.hasText(safeQuery.getVisibility())) {
            wrapper.eq(Resume::getVisibility, safeQuery.getVisibility().trim().toUpperCase());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(Resume::getResumeTitle, safeQuery.getKeyword().trim());
        }
        wrapper.orderByDesc(Resume::getIsDefault)
                .orderByDesc(Resume::getUpdatedTime)
                .orderByDesc(Resume::getCreatedTime);

        Page<Resume> entityPage = resumeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<ResumeVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("5.5：设置默认简历，保证同一学生只有一份默认简历")
    public Result setDefaultResume(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        Resume resume = requireOwnedResume(resumeId, validStudentId);
        if (Boolean.TRUE.equals(resume.getIsDefault())) {
            return Result.success("当前简历已是默认简历");
        }

        resumeMapper.update(
                null,
                new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getStudentId, validStudentId)
                        .set(Resume::getIsDefault, false)
                        .set(Resume::getUpdatedTime, LocalDateTime.now())
        );

        resume.setIsDefault(true);
        resume.setUpdatedTime(LocalDateTime.now());
        resumeMapper.updateById(resume);

        resumeChangedEventProducer.publish(
                "SET_DEFAULT",
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
        return Result.success("设置默认简历成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("5.6：设置简历公开状态（PUBLIC/PRIVATE）")
    public Result setVisibility(Long resumeId, String visibility, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        Resume resume = requireOwnedResume(resumeId, validStudentId);
        String normalizedVisibility = normalizeVisibility(visibility, null);

        if (VISIBILITY_PUBLIC.equals(normalizedVisibility) && !Boolean.TRUE.equals(resume.getStatus())) {
            throw new BizException(409, "禁用简历不允许设置为公开");
        }
        if (Objects.equals(normalizedVisibility, resume.getVisibility())) {
            return Result.success("公开状态未变化");
        }

        resume.setVisibility(normalizedVisibility);
        resume.setUpdatedTime(LocalDateTime.now());
        resumeMapper.updateById(resume);

        resumeChangedEventProducer.publish(
                "SET_VISIBILITY",
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
        return Result.success("设置公开状态成功");
    }

    @MethodPurpose("校验创建简历参数")
    private void validateCreateDto(ResumeCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "创建参数不能为空");
        }
        if (!StringUtils.hasText(dto.getResumeTitle())) {
            throw new BizException(400, "简历标题不能为空");
        }
        if (StringUtils.hasText(dto.getVisibility())) {
            normalizeVisibility(dto.getVisibility(), null);
        }
    }

    @MethodPurpose("校验并返回当前学生ID")
    private Long requireStudentId(Long studentId) {
        if (studentId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return studentId;
    }

    @MethodPurpose("按简历ID查询并校验归属")
    private Resume requireOwnedResume(Long resumeId, Long studentId) {
        if (resumeId == null) {
            throw new BizException(400, "简历ID不能为空");
        }
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BizException(404, "简历不存在");
        }
        if (!Objects.equals(resume.getStudentId(), studentId)) {
            throw new BizException(403, "无权限操作他人简历");
        }
        return resume;
    }

    @MethodPurpose("删除简历主档关联子表数据")
    private void deleteResumeChildren(Long resumeId) {
        resumeEducationMapper.delete(new LambdaQueryWrapper<ResumeEducation>().eq(ResumeEducation::getResumeId, resumeId));
        resumeExperienceMapper.delete(new LambdaQueryWrapper<ResumeExperience>().eq(ResumeExperience::getResumeId, resumeId));
        resumeProjectMapper.delete(new LambdaQueryWrapper<ResumeProject>().eq(ResumeProject::getResumeId, resumeId));
        resumeSkillMapper.delete(new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId));
        resumeCertificateMapper.delete(new LambdaQueryWrapper<ResumeCertificate>().eq(ResumeCertificate::getResumeId, resumeId));
        resumeViewLogMapper.delete(new LambdaQueryWrapper<ResumeViewLog>().eq(ResumeViewLog::getResumeId, resumeId));
    }

    @MethodPurpose("删除默认简历后，为该学生回填一份新的默认简历")
    private void setAnotherResumeAsDefault(Long studentId) {
        List<Resume> list = resumeMapper.selectList(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, studentId)
                .orderByDesc(Resume::getUpdatedTime)
                .orderByDesc(Resume::getCreatedTime)
                .last("limit 1"));
        if (list == null || list.isEmpty()) {
            return;
        }
        Resume nextDefault = list.get(0);
        nextDefault.setIsDefault(true);
        nextDefault.setUpdatedTime(LocalDateTime.now());
        resumeMapper.updateById(nextDefault);
        resumeChangedEventProducer.publish(
                "SET_DEFAULT",
                nextDefault.getId(),
                nextDefault.getStudentId(),
                nextDefault.getVisibility(),
                nextDefault.getIsDefault()
        );
    }

    @MethodPurpose("标准化可见性枚举值")
    private String normalizeVisibility(String visibility, String defaultValue) {
        if (!StringUtils.hasText(visibility)) {
            if (defaultValue == null) {
                throw new BizException(400, "visibility不能为空");
            }
            return defaultValue;
        }
        String normalized = visibility.trim().toUpperCase();
        if (!VISIBILITY_PUBLIC.equals(normalized) && !VISIBILITY_PRIVATE.equals(normalized)) {
            throw new BizException(400, "visibility仅支持PUBLIC或PRIVATE");
        }
        return normalized;
    }

    @MethodPurpose("将简历实体转换为视图对象")
    private ResumeVo toVo(Resume resume) {
        return BeanUtil.copyProperties(resume, ResumeVo.class);
    }

    @MethodPurpose("去除空白并转空")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
