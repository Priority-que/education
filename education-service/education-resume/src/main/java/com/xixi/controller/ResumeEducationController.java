package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.resume.ResumeEducationCreateDto;
import com.xixi.pojo.dto.resume.ResumeEducationUpdateDto;
import com.xixi.service.ResumeEducationService;
import com.xixi.support.StudentIdentityResolver;
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
 * 教育经历接口（7.x）。
 */
@RestController
@RequestMapping("/resume/education")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class ResumeEducationController {
    private final ResumeEducationService resumeEducationService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("7.1：新增教育经历")
    @PostMapping("/create")
    public Result create(
            @RequestBody ResumeEducationCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeEducationService.create(dto, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("7.2：修改教育经历")
    @PutMapping("/update")
    public Result update(
            @RequestBody ResumeEducationUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeEducationService.update(dto, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("7.3：删除教育经历")
    @DeleteMapping("/delete/{id}")
    public Result delete(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeEducationService.delete(id, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("7.4：查询教育经历列表")
    @GetMapping("/list/{resumeId}")
    public Result list(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return Result.success(resumeEducationService.listByResumeId(resumeId, resolveStudentId(userIdHeader)));
    }

    @MethodPurpose("将用户ID解析为学生ID")
    private Long resolveStudentId(String userIdHeader) {
        return studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
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
