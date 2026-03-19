package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.service.TeacherSystemMessageService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message/teacher/receivers")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.TEACHER})
public class TeacherReceiverController {
    private final TeacherSystemMessageService teacherSystemMessageService;

    @MethodPurpose("教师可选接收人查询")
    @GetMapping("/search")
    public Result searchReceivers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        return teacherSystemMessageService.searchTeacherReceivers(
                parseUserId(userIdHeader),
                parseRole(roleHeader),
                keyword,
                courseId,
                classId,
                pageNum,
                pageSize
        );
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
