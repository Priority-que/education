package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeCertificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeCertificateMapper;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mq.ResumeCertificateChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeCertificateBindDto;
import com.xixi.service.ResumeCertificateService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 简历证书关联服务实现（8.1~8.3）。
 */
@Service
@RequiredArgsConstructor
public class ResumeCertificateServiceImpl implements ResumeCertificateService {
    private final ResumeMapper resumeMapper;
    private final ResumeCertificateMapper resumeCertificateMapper;
    private final ResumeCertificateChangedEventProducer resumeCertificateChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("8.1：绑定证书到简历，校验归属、去重和排序后写入关联记录")
    public Result bindCertificate(ResumeCertificateBindDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateBindDto(dto);
        Resume resume = requireOwnedResume(dto.getResumeId(), validStudentId);

        Long duplicateCount = resumeCertificateMapper.selectCount(new LambdaQueryWrapper<ResumeCertificate>()
                .eq(ResumeCertificate::getResumeId, dto.getResumeId())
                .eq(ResumeCertificate::getCertificateId, dto.getCertificateId()));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException(409, "该证书已绑定到当前简历");
        }

        Integer sortOrder = resolveSortOrder(dto.getResumeId(), dto.getSortOrder());
        ResumeCertificate resumeCertificate = new ResumeCertificate();
        resumeCertificate.setResumeId(dto.getResumeId());
        resumeCertificate.setCertificateId(dto.getCertificateId());
        resumeCertificate.setSortOrder(sortOrder);
        resumeCertificate.setCreatedTime(LocalDateTime.now());
        resumeCertificateMapper.insert(resumeCertificate);

        resumeCertificateChangedEventProducer.publish(
                ResumeCertificateChangedEventProducer.EVENT_BIND,
                resumeCertificate.getId(),
                resumeCertificate.getResumeId(),
                resumeCertificate.getCertificateId(),
                resume.getStudentId(),
                resumeCertificate.getSortOrder()
        );
        return Result.success("绑定证书成功", Map.of("id", resumeCertificate.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("8.2：解绑证书关联，校验关联记录和简历归属后删除")
    public Result unbindCertificate(Long id, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (id == null || id <= 0) {
            throw new BizException(400, "关联ID不能为空");
        }

        ResumeCertificate resumeCertificate = resumeCertificateMapper.selectById(id);
        if (resumeCertificate == null) {
            throw new BizException(404, "证书关联记录不存在");
        }
        Resume resume = requireOwnedResume(resumeCertificate.getResumeId(), validStudentId);
        resumeCertificateMapper.deleteById(id);

        resumeCertificateChangedEventProducer.publish(
                ResumeCertificateChangedEventProducer.EVENT_UNBIND,
                resumeCertificate.getId(),
                resumeCertificate.getResumeId(),
                resumeCertificate.getCertificateId(),
                resume.getStudentId(),
                resumeCertificate.getSortOrder()
        );
        return Result.success("解绑证书成功");
    }

    @Override
    @MethodPurpose("8.3：查询当前学生指定简历下的证书关联列表")
    public List<ResumeCertificate> listCertificates(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);
        return resumeCertificateMapper.selectList(new LambdaQueryWrapper<ResumeCertificate>()
                .eq(ResumeCertificate::getResumeId, resumeId)
                .orderByAsc(ResumeCertificate::getSortOrder, ResumeCertificate::getCreatedTime));
    }

    @MethodPurpose("校验绑定参数")
    private void validateBindDto(ResumeCertificateBindDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "简历ID不能为空");
        }
        if (dto.getCertificateId() == null || dto.getCertificateId() <= 0) {
            throw new BizException(400, "证书ID不能为空");
        }
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BizException(400, "sortOrder不能小于0");
        }
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

    @MethodPurpose("计算证书关联排序值，不传时自动补位")
    private Integer resolveSortOrder(Long resumeId, Integer requestSortOrder) {
        if (requestSortOrder != null) {
            return requestSortOrder;
        }
        ResumeCertificate lastRecord = resumeCertificateMapper.selectOne(new LambdaQueryWrapper<ResumeCertificate>()
                .select(ResumeCertificate::getSortOrder)
                .eq(ResumeCertificate::getResumeId, resumeId)
                .orderByDesc(ResumeCertificate::getSortOrder)
                .last("limit 1"));
        if (lastRecord == null || lastRecord.getSortOrder() == null) {
            return 1;
        }
        return lastRecord.getSortOrder() + 1;
    }
}
