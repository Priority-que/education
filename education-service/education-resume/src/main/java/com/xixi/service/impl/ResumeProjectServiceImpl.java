package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeProject;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mapper.ResumeProjectMapper;
import com.xixi.mq.ResumeChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeProjectCreateDto;
import com.xixi.pojo.dto.resume.ResumeProjectUpdateDto;
import com.xixi.service.ResumeProjectService;
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
 * 项目经历服务实现（7.x）。
 */
@Service
@RequiredArgsConstructor
public class ResumeProjectServiceImpl implements ResumeProjectService {
    private final ResumeMapper resumeMapper;
    private final ResumeProjectMapper resumeProjectMapper;
    private final ResumeChangedEventProducer resumeChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-项目经历：新增项目经历并校验简历归属、时间范围和排序")
    public Result create(ResumeProjectCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);
        Resume resume = requireOwnedResume(dto.getResumeId(), validStudentId);
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        ResumeProject entity = new ResumeProject();
        entity.setResumeId(dto.getResumeId());
        entity.setProjectName(requireText(dto.getProjectName(), "projectName不能为空"));
        entity.setProjectRole(trimToNull(dto.getProjectRole()));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setProjectDescription(trimToNull(dto.getProjectDescription()));
        entity.setTechnologiesUsed(trimToNull(dto.getTechnologiesUsed()));
        entity.setProjectLink(trimToNull(dto.getProjectLink()));
        entity.setSortOrder(resolveSortOrder(dto.getResumeId(), dto.getSortOrder()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeProjectMapper.insert(entity);

        publishResumeChanged("PROJECT_CREATE", resume);
        return Result.success("新增项目经历成功", Map.of("id", entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-项目经历：修改项目经历并校验归属与时间范围")
    public Result update(ResumeProjectUpdateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "项目经历ID不能为空");
        }

        ResumeProject entity = requireProject(dto.getId());
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);

        if (dto.getProjectName() != null) {
            entity.setProjectName(requireText(dto.getProjectName(), "projectName不能为空"));
        }
        if (dto.getProjectRole() != null) {
            entity.setProjectRole(trimToNull(dto.getProjectRole()));
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getProjectDescription() != null) {
            entity.setProjectDescription(trimToNull(dto.getProjectDescription()));
        }
        if (dto.getTechnologiesUsed() != null) {
            entity.setTechnologiesUsed(trimToNull(dto.getTechnologiesUsed()));
        }
        if (dto.getProjectLink() != null) {
            entity.setProjectLink(trimToNull(dto.getProjectLink()));
        }
        if (dto.getSortOrder() != null) {
            if (dto.getSortOrder() < 0) {
                throw new BizException(400, "sortOrder不能小于0");
            }
            entity.setSortOrder(dto.getSortOrder());
        }
        validateDateRange(entity.getStartDate(), entity.getEndDate());
        entity.setUpdatedTime(LocalDateTime.now());
        resumeProjectMapper.updateById(entity);

        publishResumeChanged("PROJECT_UPDATE", resume);
        return Result.success("修改项目经历成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("7-项目经历：删除项目经历并校验归属")
    public Result delete(Long id, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        ResumeProject entity = requireProject(id);
        Resume resume = requireOwnedResume(entity.getResumeId(), validStudentId);
        resumeProjectMapper.deleteById(id);

        publishResumeChanged("PROJECT_DELETE", resume);
        return Result.success("删除项目经历成功");
    }

    @Override
    @MethodPurpose("7-项目经历：按简历查询项目经历列表并校验归属")
    public List<ResumeProject> listByResumeId(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);
        return resumeProjectMapper.selectList(new LambdaQueryWrapper<ResumeProject>()
                .eq(ResumeProject::getResumeId, resumeId)
                .orderByAsc(ResumeProject::getSortOrder, ResumeProject::getCreatedTime));
    }

    @MethodPurpose("校验新增项目经历参数")
    private void validateCreateDto(ResumeProjectCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "resumeId不能为空");
        }
        requireText(dto.getProjectName(), "projectName不能为空");
        if (dto.getStartDate() == null) {
            throw new BizException(400, "startDate不能为空");
        }
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BizException(400, "sortOrder不能小于0");
        }
    }

    @MethodPurpose("校验项目经历时间范围")
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
        ResumeProject last = resumeProjectMapper.selectOne(new LambdaQueryWrapper<ResumeProject>()
                .select(ResumeProject::getSortOrder)
                .eq(ResumeProject::getResumeId, resumeId)
                .orderByDesc(ResumeProject::getSortOrder)
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

    @MethodPurpose("按项目经历ID查询")
    private ResumeProject requireProject(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(400, "项目经历ID不能为空");
        }
        ResumeProject entity = resumeProjectMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "项目经历不存在");
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
