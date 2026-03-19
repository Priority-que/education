package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.query.resume.PublicResumeQuery;
import com.xixi.pojo.vo.resume.PublicResumeDetailVo;
import com.xixi.pojo.vo.resume.PublicResumePageVo;
import com.xixi.service.PublicResumeService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开简历接口（6.1、6.2）。
 */
@RestController
@RequestMapping("/resume/public")
@RequiredArgsConstructor
public class PublicResumeController {
    private final PublicResumeService publicResumeService;

    @MethodPurpose("6.1：公开简历详情查询（企业/访客可访问）")
    @GetMapping("/{resumeId}")
    public Result getPublicResumeDetail(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        PublicResumeDetailVo detail = publicResumeService.getPublicResumeDetail(
                resumeId,
                parseUserId(userIdHeader),
                parseUserRole(userRoleHeader)
        );
        return Result.success(detail);
    }

    @MethodPurpose("6.2：企业分页检索公开简历")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @GetMapping("/page")
    public Result getPublicResumePage(
            PublicResumeQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<PublicResumePageVo> page = publicResumeService.getPublicResumePage(query, parseUserId(userIdHeader));
        return Result.success(page);
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
