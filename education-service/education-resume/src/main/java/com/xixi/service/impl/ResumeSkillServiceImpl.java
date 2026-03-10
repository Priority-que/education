package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeSkill;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mapper.ResumeSkillMapper;
import com.xixi.mq.ResumeChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeSkillCreateDto;
import com.xixi.pojo.dto.resume.ResumeSkillUpdateDto;
import com.xixi.service.ResumeSkillService;
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
 * 技能服务实现（7.x）。
 */
@Service
@RequiredArgsConstructor
public class ResumeSkillServiceImpl implements ResumeSkillService {
    private final ResumeMapper resumeMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final ResumeChangedEventProducer resumeChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-技能：新增技能并校验简历归属和排序")
    public Result create(ResumeSkillCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);
        Resume resume = requireOwnedResume(dto.getResumeId(), validStudentId);

        ResumeSkill entity = new ResumeSkill();
        entity.setResumeId(dto.getResumeId());
        entity.setSkillName(requireText(dto.getSkillName(), "skillName不能为空"));
        entity.setSkillCategory(normalizeSkillCategory(dto.getSkillCategory()));
        entity.setProficiencyLevel(normalizeProficiency(dto.getProficiencyLevel()));
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setSortOrder(resolveSortOrder(dto.getResumeId(), dto.getSortOrder()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeSkillMapper.insert(entity);

        publishResumeChanged("SKILL_CREATE", resume);
        return Result.success("新增技能成功", Map.of("id", entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-技能：修改技能并校验归属")
    public Result update(ResumeSkillUpdateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "技能ID不能为空");
        }

        ResumeSkill entity = requireSkill(dto.getId());
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);

        if (dto.getSkillName() != null) {
            entity.setSkillName(requireText(dto.getSkillName(), "skillName不能为空"));
        }
        if (dto.getSkillCategory() != null) {
            entity.setSkillCategory(normalizeSkillCategory(dto.getSkillCategory()));
        }
        if (dto.getProficiencyLevel() != null) {
            entity.setProficiencyLevel(normalizeProficiency(dto.getProficiencyLevel()));
        }
        if (dto.getDescription() != null) {
            entity.setDescription(trimToNull(dto.getDescription()));
        }
        if (dto.getSortOrder() != null) {
            if (dto.getSortOrder() < 0) {
                throw new BizException(400, "sortOrder不能小于0");
            }
            entity.setSortOrder(dto.getSortOrder());
        }
        entity.setUpdatedTime(LocalDateTime.now());
        resumeSkillMapper.updateById(entity);

        publishResumeChanged("SKILL_UPDATE", resume);
        return Result.success("修改技能成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-技能：删除技能并校验归属")
    public Result delete(Long id, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        ResumeSkill entity = requireSkill(id);
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);
        resumeSkillMapper.deleteById(id);

        publishResumeChanged("SKILL_DELETE", resume);
        return Result.success("删除技能成功");
    }

    @Override
    @MethodPurpose("7-技能：按简历查询技能列表并校验归属")
    public List<ResumeSkill> listByResumeId(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);
        return resumeSkillMapper.selectList(new LambdaQueryWrapper<ResumeSkill>()
                .eq(ResumeSkill::getResumeId, resumeId)
                .orderByAsc(ResumeSkill::getSortOrder, ResumeSkill::getCreatedTime));
    }

    @MethodPurpose("校验新增技能参数")
    private void validateCreateDto(ResumeSkillCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "resumeId不能为空");
        }
        requireText(dto.getSkillName(), "skillName不能为空");
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BizException(400, "sortOrder不能小于0");
        }
    }

    @MethodPurpose("标准化技能分类")
    private String normalizeSkillCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        String normalized = category.trim().toUpperCase();
        if (!"LANGUAGE".equals(normalized)
                && !"FRAMEWORK".equals(normalized)
                && !"DATABASE".equals(normalized)
                && !"TOOL".equals(normalized)
                && !"OTHER".equals(normalized)) {
            throw new BizException(400, "skillCategory仅支持LANGUAGE/FRAMEWORK/DATABASE/TOOL/OTHER");
        }
        return normalized;
    }

    @MethodPurpose("标准化熟练等级")
    private String normalizeProficiency(String proficiency) {
        if (!StringUtils.hasText(proficiency)) {
            return "INTERMEDIATE";
        }
        String normalized = proficiency.trim().toUpperCase();
        if (!"BEGINNER".equals(normalized)
                && !"INTERMEDIATE".equals(normalized)
                && !"ADVANCED".equals(normalized)
                && !"EXPERT".equals(normalized)) {
            throw new BizException(400, "proficiencyLevel仅支持BEGINNER/INTERMEDIATE/ADVANCED/EXPERT");
        }
        return normalized;
    }

    @MethodPurpose("计算排序值，不传时自动补位")
    private Integer resolveSortOrder(Long resumeId, Integer requestSortOrder) {
        if (requestSortOrder != null) {
            return requestSortOrder;
        }
        ResumeSkill last = resumeSkillMapper.selectOne(new LambdaQueryWrapper<ResumeSkill>()
                .select(ResumeSkill::getSortOrder)
                .eq(ResumeSkill::getResumeId, resumeId)
                .orderByDesc(ResumeSkill::getSortOrder)
                .last("limit 1"));
        if (last == null || last.getSortOrder() == null) {
            return 1;
        }
        return last.getSortOrder() + 1;
    }

    @MethodPurpose("校验并返回当前学生ID")
    private Long requireStudentId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return studentId;
    }

    @MethodPurpose("按简历ID查询并校验归属")
    private Resume requireOwnedResume(Long resumeId, Long studentId) {
        if (resumeId == null || resumeId <= 0) {
            throw new BizException(400, "resumeId不能为空");
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

    @MethodPurpose("按技能ID查询")
    private ResumeSkill requireSkill(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(400, "技能ID不能为空");
        }
        ResumeSkill entity = resumeSkillMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "技能不存在");
        }
        return entity;
    }

    @MethodPurpose("发送简历变更事件")
    private void publishResumeChanged(String eventType, Resume resume) {
        resumeChangedEventProducer.publish(
                eventType,
                resume.getId(),
                resume.getStudentId(),
                resume.getVisibility(),
                resume.getIsDefault()
        );
    }

    @MethodPurpose("文本去空并转null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @MethodPurpose("读取并校验非空文本")
    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(400, message);
        }
        return value.trim();
    }
}
