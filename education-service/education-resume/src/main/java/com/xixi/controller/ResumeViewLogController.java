package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.resume.ResumeViewLogRecordDto;
import com.xixi.pojo.vo.resume.ResumeViewLogStatVo;
import com.xixi.pojo.vo.resume.ResumeViewLogVo;
import com.xixi.service.ResumeViewLogService;
import com.xixi.support.StudentIdentityResolver;
import com.xixi.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简历浏览日志接口（9.1~9.3）。
 */
@RestController
@RequestMapping("/resume/view-log")
@RequiredArgsConstructor
public class ResumeViewLogController {
    private final ResumeViewLogService resumeViewLogService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("9.1：记录浏览行为（内部服务调用）")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @PostMapping("/record")
    public Result record(
            @RequestBody ResumeViewLogRecordDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader,
            HttpServletRequest request
    ) {
        return resumeViewLogService.recordViewLog(
                dto,
                parseUserId(userIdHeader),
                parseUserRole(userRoleHeader),
                getClientIp(request),
                getUserAgent(request)
        );
    }

    @MethodPurpose("9.2：分页查询当前学生指定简历的浏览日志")
    @RoleRequired({RoleConstants.STUDENT})
    @GetMapping("/my/{resumeId}")
    public Result getMyViewLogPage(
            @PathVariable Long resumeId,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<ResumeViewLogVo> page = resumeViewLogService.getMyViewLogPage(
                resumeId,
                resolveStudentId(userIdHeader),
                pageNum,
                pageSize
        );
        return Result.success(page);
    }

    @MethodPurpose("9.3：查询当前学生指定简历的浏览统计")
    @RoleRequired({RoleConstants.STUDENT})
    @GetMapping("/stat/{resumeId}")
    public Result getMyViewStat(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        ResumeViewLogStatVo statVo = resumeViewLogService.getMyViewStat(resumeId, resolveStudentId(userIdHeader));
        return Result.success(statVo);
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

    @MethodPurpose("获取客户端IP地址")
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            int index = xForwardedFor.indexOf(',');
            return index > 0 ? xForwardedFor.substring(0, index).trim() : xForwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    @MethodPurpose("获取客户端User-Agent")
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? "" : userAgent;
    }
}
