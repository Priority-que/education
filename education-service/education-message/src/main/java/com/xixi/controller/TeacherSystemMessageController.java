package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.TeacherTargetPreviewDto;
import com.xixi.pojo.query.message.TeacherSystemMessageHistoryQuery;
import com.xixi.service.TeacherSystemMessageService;
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
@RequestMapping("/message/teacher/system")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.TEACHER})
public class TeacherSystemMessageController {
    private final TeacherSystemMessageService teacherSystemMessageService;

    @MethodPurpose("教师公告创建草稿")
    @PostMapping("/create")
    public Result createTeacherSystemMessage(
            @RequestBody SystemMessageCreateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return teacherSystemMessageService.createTeacherSystemMessage(
                request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("教师公告发布")
    @PostMapping("/publish/{messageId}")
    public Result publishTeacherSystemMessage(
            @PathVariable("messageId") Long messageId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return teacherSystemMessageService.publishTeacherSystemMessage(
                messageId, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("教师公告目标预览")
    @PostMapping("/target/preview")
    public Result previewTeacherTarget(
            @RequestBody TeacherTargetPreviewDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return teacherSystemMessageService.previewTeacherTarget(
                request, parseUserId(userIdHeader), parseRole(roleHeader));
    }

    @MethodPurpose("教师公告历史分页")
    @GetMapping("/history/page")
    public Result getTeacherSystemMessageHistoryPage(
            TeacherSystemMessageHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return teacherSystemMessageService.getTeacherSystemMessageHistoryPage(
                query, parseUserId(userIdHeader), parseRole(roleHeader));
    }

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

    private Integer parseRole(String roleHeader) {
        if (!StringUtils.hasText(roleHeader)) {
            return null;
        }
        try {
            return Integer.parseInt(roleHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
