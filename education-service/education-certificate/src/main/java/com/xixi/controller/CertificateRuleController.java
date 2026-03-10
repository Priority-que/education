package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.certificate.CertificateIssueRuleSaveDto;
import com.xixi.pojo.vo.certificate.CertificateIssueRuleVo;
import com.xixi.service.CertificateRuleService;
import com.xixi.support.TeacherIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 证书自动颁发规则接口。
 */
@RestController
@RequestMapping("/certificate/rule")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.TEACHER})
public class CertificateRuleController {
    private final CertificateRuleService certificateRuleService;
    private final TeacherIdentityResolver teacherIdentityResolver;

    @MethodPurpose("查询课程证书规则")
    @GetMapping("/course/{courseId}")
    public Result getByCourseId(
            @PathVariable Long courseId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        CertificateIssueRuleVo vo = certificateRuleService.getByCourseId(courseId, teacherId);
        return Result.success(vo);
    }

    @MethodPurpose("创建证书规则")
    @PostMapping("/create")
    public Result createRule(
            @RequestBody CertificateIssueRuleSaveDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        return certificateRuleService.createRule(dto, teacherId);
    }

    @MethodPurpose("更新证书规则")
    @PutMapping("/update/{ruleId}")
    public Result updateRule(
            @PathVariable Long ruleId,
            @RequestBody CertificateIssueRuleSaveDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        return certificateRuleService.updateRule(ruleId, dto, teacherId);
    }

    @MethodPurpose("删除证书规则")
    @DeleteMapping("/delete/{ruleId}")
    public Result deleteRule(
            @PathVariable Long ruleId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        return certificateRuleService.deleteRule(ruleId, teacherId);
    }

    @MethodPurpose("解析请求头中的用户ID")
    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
