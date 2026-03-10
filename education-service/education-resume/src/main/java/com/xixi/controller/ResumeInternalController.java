package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.query.resume.PublicResumeQuery;
import com.xixi.pojo.vo.resume.PublicResumeDetailVo;
import com.xixi.pojo.vo.resume.PublicResumePageVo;
import com.xixi.pojo.vo.resume.PublicStudentResumePageVo;
import com.xixi.service.PublicResumeService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简历内部接口（10.1、10.2）。
 */
@RestController
@RequestMapping("/resume/internal/public")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
public class ResumeInternalController {
    private final PublicResumeService publicResumeService;

    @MethodPurpose("10.1：内部公开简历分页查询（供人才服务调用）")
    @GetMapping("/page")
    public Result getInternalPublicResumePage(
            PublicResumeQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<PublicResumePageVo> page = publicResumeService.getPublicResumePage(query, parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("10.3：内部学生维度公开简历分页查询（供人才服务调用）")
    @GetMapping("/student/page")
    public Result getInternalPublicStudentPage(
            PublicResumeQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<PublicStudentResumePageVo> page = publicResumeService.getPublicStudentPage(query, parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("10.2：内部公开简历详情查询（供人才服务调用）")
    @GetMapping("/{resumeId}")
    public Result getInternalPublicResumeDetail(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        PublicResumeDetailVo detailVo = publicResumeService.getPublicResumeDetail(
                resumeId,
                parseUserId(userIdHeader),
                parseUserRole(userRoleHeader)
        );
        return Result.success(detailVo);
    }

    @MethodPurpose("10.4：内部学生维度公开简历详情查询（供人才服务调用）")
    @GetMapping("/student/{studentId}")
    public Result getInternalPublicStudentDetail(
            @PathVariable Long studentId,
            @RequestParam(value = "resumeId", required = false) Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        PublicResumeDetailVo detailVo = publicResumeService.getPublicStudentDetail(
                studentId,
                resumeId,
                parseUserId(userIdHeader),
                parseUserRole(userRoleHeader)
        );
        return Result.success(detailVo);
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

    @MethodPurpose("解析请求头中的角色码")
    private Integer parseUserRole(String userRoleHeader) {
        if (!StringUtils.hasText(userRoleHeader)) {
            return null;
        }
        try {
            return Integer.parseInt(userRoleHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
