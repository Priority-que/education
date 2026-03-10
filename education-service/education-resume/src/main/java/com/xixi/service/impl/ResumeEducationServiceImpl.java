package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeEducation;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeEducationMapper;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mq.ResumeChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeEducationCreateDto;
import com.xixi.pojo.dto.resume.ResumeEducationUpdateDto;
import com.xixi.service.ResumeEducationService;
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
 * 教育经历服务实现（7.x）。
 */
@Service
@RequiredArgsConstructor
public class ResumeEducationServiceImpl implements ResumeEducationService {
    private final ResumeMapper resumeMapper;
    private final ResumeEducationMapper resumeEducationMapper;
    private final ResumeChangedEventProducer resumeChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-教育经历：新增教育经历并校验简历归属、时间范围和排序")
    public Result create(ResumeEducationCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);
        Resume resume = requireOwnedResume(dto.getResumeId(), validStudentId);
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        ResumeEducation entity = new ResumeEducation();
        entity.setResumeId(dto.getResumeId());
        entity.setSchoolName(requireText(dto.getSchoolName(), "schoolName不能为空"));
        entity.setDegree(requireText(dto.getDegree(), "degree不能为空").toUpperCase());
        entity.setMajor(requireText(dto.getMajor(), "major不能为空"));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setGpa(dto.getGpa());
        entity.setRanking(trimToNull(dto.getRanking()));
        entity.setHonors(trimToNull(dto.getHonors()));
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setSortOrder(resolveSortOrder(dto.getResumeId(), dto.getSortOrder()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeEducationMapper.insert(entity);

        publishResumeChanged("EDUCATION_CREATE", resume);
        return Result.success("新增教育经历成功", Map.of("id", entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-教育经历：修改教育经历并校验归属与时间范围")
    public Result update(ResumeEducationUpdateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "教育经历ID不能为空");
        }

        ResumeEducation entity = requireEducation(dto.getId());
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);

        if (dto.getSchoolName() != null) {
            entity.setSchoolName(requireText(dto.getSchoolName(), "schoolName不能为空"));
        }
        if (dto.getDegree() != null) {
            entity.setDegree(requireText(dto.getDegree(), "degree不能为空").toUpperCase());
        }
        if (dto.getMajor() != null) {
            entity.setMajor(requireText(dto.getMajor(), "major不能为空"));
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getGpa() != null) {
            entity.setGpa(dto.getGpa());
        }
        if (dto.getRanking() != null) {
            entity.setRanking(trimToNull(dto.getRanking()));
        }
        if (dto.getHonors() != null) {
            entity.setHonors(trimToNull(dto.getHonors()));
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
        validateDateRange(entity.getStartDate(), entity.getEndDate());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeEducationMapper.updateById(entity);

        publishResumeChanged("EDUCATION_UPDATE", resume);
        return Result.success("修改教育经历成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-教育经历：删除教育经历并校验归属")
    public Result delete(Long id, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        ResumeEducation entity = requireEducation(id);
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);
        resumeEducationMapper.deleteById(id);

        publishResumeChanged("EDUCATION_DELETE", resume);
        return Result.success("删除教育经历成功");
    }

    @Override
    @MethodPurpose("7-教育经历：按简历查询教育经历列表并校验归属")
    public List<ResumeEducation> listByResumeId(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);
        return resumeEducationMapper.selectList(new LambdaQueryWrapper<ResumeEducation>()
                .eq(ResumeEducation::getResumeId, resumeId)
                .orderByAsc(ResumeEducation::getSortOrder, ResumeEducation::getCreatedTime));
    }

    @MethodPurpose("校验新增教育经历参数")
    private void validateCreateDto(ResumeEducationCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "resumeId不能为空");
        }
        requireText(dto.getSchoolName(), "schoolName不能为空");
        requireText(dto.getDegree(), "degree不能为空");
        requireText(dto.getMajor(), "major不能为空");
        if (dto.getStartDate() == null) {
            throw new BizException(400, "startDate不能为空");
        }
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BizException(400, "sortOrder不能小于0");
        }
    }

    @MethodPurpose("校验教育经历时间范围")
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BizException(400, "startDate不能为空");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BizException(400, "endDate不能早于startDate");
        }
    }

    @MethodPurpose("计算排序值，不传时自动补位")
    private Integer resolveSortOrder(Long resumeId, Integer requestSortOrder) {
        if (requestSortOrder != null) {
            return requestSortOrder;
        }
        ResumeEducation last = resumeEducationMapper.selectOne(new LambdaQueryWrapper<ResumeEducation>()
                .select(ResumeEducation::getSortOrder)
                .eq(ResumeEducation::getResumeId, resumeId)
                .orderByDesc(ResumeEducation::getSortOrder)
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

    @MethodPurpose("按教育经历ID查询")
    private ResumeEducation requireEducation(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(400, "教育经历ID不能为空");
        }
        ResumeEducation entity = resumeEducationMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "教育经历不存在");
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
