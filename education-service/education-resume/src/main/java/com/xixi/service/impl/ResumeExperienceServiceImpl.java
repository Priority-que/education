package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeExperience;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeExperienceMapper;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mq.ResumeChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeExperienceCreateDto;
import com.xixi.pojo.dto.resume.ResumeExperienceUpdateDto;
import com.xixi.service.ResumeExperienceService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作经历服务实现（7.x）。
 */
@Service
@RequiredArgsConstructor
public class ResumeExperienceServiceImpl implements ResumeExperienceService {
    private final ResumeMapper resumeMapper;
    private final ResumeExperienceMapper resumeExperienceMapper;
    private final ResumeChangedEventProducer resumeChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-工作经历：新增工作经历并校验简历归属、时间范围和排序")
    public Result create(ResumeExperienceCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);
        Resume resume = requireOwnedResume(dto.getResumeId(), validStudentId);
        validateDateRange(dto.getStartDate(), dto.getEndDate(), dto.getIsCurrent());

        ResumeExperience entity = new ResumeExperience();
        entity.setResumeId(dto.getResumeId());
        entity.setCompanyName(requireText(dto.getCompanyName(), "companyName不能为空"));
        entity.setPosition(requireText(dto.getPosition(), "position不能为空"));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setIsCurrent(Boolean.TRUE.equals(dto.getIsCurrent()));
        entity.setLocation(trimToNull(dto.getLocation()));
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setAchievements(trimToNull(dto.getAchievements()));
        entity.setSortOrder(resolveSortOrder(dto.getResumeId(), dto.getSortOrder()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeExperienceMapper.insert(entity);

        publishResumeChanged("EXPERIENCE_CREATE", resume);
        return Result.success("新增工作经历成功", Map.of("id", entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-工作经历：修改工作经历并校验归属与时间范围")
    public Result update(ResumeExperienceUpdateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "工作经历ID不能为空");
        }

        ResumeExperience entity = requireExperience(dto.getId());
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);

        if (dto.getCompanyName() != null) {
            entity.setCompanyName(requireText(dto.getCompanyName(), "companyName不能为空"));
        }
        if (dto.getPosition() != null) {
            entity.setPosition(requireText(dto.getPosition(), "position不能为空"));
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getIsCurrent() != null) {
            entity.setIsCurrent(dto.getIsCurrent());
        }
        if (dto.getLocation() != null) {
            entity.setLocation(trimToNull(dto.getLocation()));
        }
        if (dto.getDescription() != null) {
            entity.setDescription(trimToNull(dto.getDescription()));
        }
        if (dto.getAchievements() != null) {
            entity.setAchievements(trimToNull(dto.getAchievements()));
        }
        if (dto.getSortOrder() != null) {
            if (dto.getSortOrder() < 0) {
                throw new BizException(400, "sortOrder不能小于0");
            }
            entity.setSortOrder(dto.getSortOrder());
        }
        validateDateRange(entity.getStartDate(), entity.getEndDate(), entity.getIsCurrent());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeExperienceMapper.updateById(entity);

        publishResumeChanged("EXPERIENCE_UPDATE", resume);
        return Result.success("修改工作经历成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-工作经历：删除工作经历并校验归属")
    public Result delete(Long id, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        ResumeExperience entity = requireExperience(id);
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);
        resumeExperienceMapper.deleteById(id);

        publishResumeChanged("EXPERIENCE_DELETE", resume);
        return Result.success("删除工作经历成功");
    }

    @Override
    @MethodPurpose("7-工作经历：按简历查询工作经历列表并校验归属")
    public List<ResumeExperience> listByResumeId(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);
        return resumeExperienceMapper.selectList(new LambdaQueryWrapper<ResumeExperience>()
                .eq(ResumeExperience::getResumeId, resumeId)
                .orderByAsc(ResumeExperience::getSortOrder, ResumeExperience::getCreatedTime));
    }

    @MethodPurpose("校验新增工作经历参数")
    private void validateCreateDto(ResumeExperienceCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "resumeId不能为空");
        }
        requireText(dto.getCompanyName(), "companyName不能为空");
        requireText(dto.getPosition(), "position不能为空");
        if (dto.getStartDate() == null) {
            throw new BizException(400, "startDate不能为空");
        }
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BizException(400, "sortOrder不能小于0");
        }
    }

    @MethodPurpose("校验工作经历时间范围")
    private void validateDateRange(LocalDate startDate, LocalDate endDate, Boolean isCurrent) {
        if (startDate == null) {
            throw new BizException(400, "startDate不能为空");
        }
        if (!Boolean.TRUE.equals(isCurrent) && endDate != null && endDate.isBefore(startDate)) {
            throw new BizException(400, "endDate不能早于startDate");
        }
    }

    @MethodPurpose("计算排序值，不传时自动补位")
    private Integer resolveSortOrder(Long resumeId, Integer requestSortOrder) {
        if (requestSortOrder != null) {
            return requestSortOrder;
        }
        ResumeExperience last = resumeExperienceMapper.selectOne(new LambdaQueryWrapper<ResumeExperience>()
                .select(ResumeExperience::getSortOrder)
                .eq(ResumeExperience::getResumeId, resumeId)
                .orderByDesc(ResumeExperience::getSortOrder)
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

    @MethodPurpose("按工作经历ID查询")
    private ResumeExperience requireExperience(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(400, "工作经历ID不能为空");
        }
        ResumeExperience entity = resumeExperienceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "工作经历不存在");
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
