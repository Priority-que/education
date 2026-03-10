package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.CertificateIssueRule;
import com.xixi.exception.BizException;
import com.xixi.mapper.CertificateIssueRuleMapper;
import com.xixi.pojo.dto.certificate.CertificateIssueRuleSaveDto;
import com.xixi.pojo.vo.certificate.CertificateIssueRuleVo;
import com.xixi.service.CertificateRuleService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 证书规则服务实现。
 */
@Service
@RequiredArgsConstructor
public class CertificateRuleServiceImpl implements CertificateRuleService {
    private final CertificateIssueRuleMapper certificateIssueRuleMapper;

    @Override
    @MethodPurpose("按课程查询证书规则")
    public CertificateIssueRuleVo getByCourseId(Long courseId, Long teacherId) {
        requireTeacherId(teacherId);
        if (courseId == null) {
            throw new BizException(400, "courseId不能为空");
        }
        CertificateIssueRule rule = certificateIssueRuleMapper.selectByCourseId(courseId);
        if (rule == null) {
            return null;
        }
        if (!teacherId.equals(rule.getTeacherId())) {
            throw new BizException(403, "无权限查看该课程规则");
        }
        return BeanUtil.copyProperties(rule, CertificateIssueRuleVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("创建证书规则")
    public Result createRule(CertificateIssueRuleSaveDto dto, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        validateSaveDto(dto, true);

        CertificateIssueRule rule = new CertificateIssueRule();
        BeanUtil.copyProperties(dto, rule);
        rule.setTeacherId(validTeacherId);
        rule.setEnabled(Boolean.TRUE.equals(dto.getEnabled()));
        rule.setCreatedTime(LocalDateTime.now());
        rule.setUpdatedTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(rule.getEnabled())) {
            certificateIssueRuleMapper.disableOtherEnabledRules(rule.getCourseId(), null, LocalDateTime.now());
        }
        certificateIssueRuleMapper.insert(rule);
        return Result.success("规则创建成功", rule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("更新证书规则")
    public Result updateRule(Long ruleId, CertificateIssueRuleSaveDto dto, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        if (ruleId == null) {
            throw new BizException(400, "ruleId不能为空");
        }
        validateSaveDto(dto, false);

        CertificateIssueRule existed = certificateIssueRuleMapper.selectByIdAndTeacherId(ruleId, validTeacherId);
        if (existed == null) {
            throw new BizException(404, "规则不存在或无权限");
        }

        Long targetCourseId = dto.getCourseId() == null ? existed.getCourseId() : dto.getCourseId();
        boolean targetEnabled = dto.getEnabled() == null ? Boolean.TRUE.equals(existed.getEnabled()) : Boolean.TRUE.equals(dto.getEnabled());
        if (targetEnabled) {
            certificateIssueRuleMapper.disableOtherEnabledRules(targetCourseId, ruleId, LocalDateTime.now());
        }

        if (dto.getCourseId() != null) {
            existed.setCourseId(dto.getCourseId());
        }
        if (dto.getEnabled() != null) {
            existed.setEnabled(dto.getEnabled());
        }
        if (StringUtils.hasText(dto.getRuleName())) {
            existed.setRuleName(dto.getRuleName().trim());
        }
        if (dto.getMinFinalScore() != null) {
            existed.setMinFinalScore(dto.getMinFinalScore());
        }
        if (dto.getMinCompletionRate() != null) {
            existed.setMinCompletionRate(dto.getMinCompletionRate());
        }
        if (dto.getRequireExamPass() != null) {
            existed.setRequireExamPass(dto.getRequireExamPass());
        }
        if (dto.getDescription() != null) {
            existed.setDescription(trimToNull(dto.getDescription()));
        }
        existed.setUpdatedTime(LocalDateTime.now());
        certificateIssueRuleMapper.updateById(existed);
        return Result.success("规则更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("删除证书规则")
    public Result deleteRule(Long ruleId, Long teacherId) {
        Long validTeacherId = requireTeacherId(teacherId);
        if (ruleId == null) {
            throw new BizException(400, "ruleId不能为空");
        }
        int affected = certificateIssueRuleMapper.deleteByIdAndTeacherId(ruleId, validTeacherId);
        if (affected <= 0) {
            throw new BizException(404, "规则不存在或无权限");
        }
        return Result.success("规则删除成功");
    }

    @MethodPurpose("校验教师ID")
    private Long requireTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new BizException(401, "未登录或教师ID缺失");
        }
        return teacherId;
    }

    @MethodPurpose("校验创建/更新参数")
    private void validateSaveDto(CertificateIssueRuleSaveDto dto, boolean createMode) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (createMode && dto.getCourseId() == null) {
            throw new BizException(400, "courseId不能为空");
        }
        if (createMode && !StringUtils.hasText(dto.getRuleName())) {
            throw new BizException(400, "ruleName不能为空");
        }
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
