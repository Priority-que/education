package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.pojo.dto.TeacherVerificationApplyDto;
import com.xixi.pojo.dto.TeacherVerificationAuditDto;
import com.xixi.pojo.query.TeacherVerificationHistoryQuery;
import com.xixi.pojo.vo.TeacherVerificationVo;
import com.xixi.service.TeacherVerificationService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/verification")
@RequiredArgsConstructor
public class TeacherVerificationController {
    private final TeacherVerificationService teacherVerificationService;

    @PostMapping("/apply")
    public Result apply(
            @RequestBody(required = false) TeacherVerificationApplyDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return teacherVerificationService.apply(dto, parseLong(userIdHeader));
    }

    @GetMapping("/current")
    public Result current(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        TeacherVerificationVo vo = teacherVerificationService.current(parseLong(userIdHeader));
        return Result.success(vo);
    }

    @GetMapping("/history/page")
    public Result historyPage(
            TeacherVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        IPage<TeacherVerificationVo> page = teacherVerificationService.historyPage(
                query,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
        return Result.success(page);
    }

    @GetMapping("/history/my/page")
    public Result historyMyPage(
            TeacherVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<TeacherVerificationVo> page = teacherVerificationService.historyMyPage(
                query,
                parseLong(userIdHeader)
        );
        return Result.success(page);
    }

    @GetMapping("/history/admin/page")
    public Result historyAdminPage(
            TeacherVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        IPage<TeacherVerificationVo> page = teacherVerificationService.historyAdminPage(
                query,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
        return Result.success(page);
    }

    @PostMapping("/audit/{applicationId}")
    public Result audit(
            @PathVariable Long applicationId,
            @RequestBody TeacherVerificationAuditDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        return teacherVerificationService.audit(
                applicationId,
                dto,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
