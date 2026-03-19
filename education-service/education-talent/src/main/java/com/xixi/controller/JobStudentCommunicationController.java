package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.talent.CommunicationConfirmDto;
import com.xixi.pojo.dto.talent.StudentCommunicationPageQueryDto;
import com.xixi.pojo.vo.talent.StudentCommunicationPageVo;
import com.xixi.service.StudentJobCommunicationService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学生端沟通控制器。
 */
@RestController
@RequestMapping("/job/communication")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class JobStudentCommunicationController {
    private final StudentJobCommunicationService studentJobCommunicationService;

    @MethodPurpose("学生端沟通记录分页")
    @GetMapping("/my/page")
    public Result myPage(
            StudentCommunicationPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<StudentCommunicationPageVo> page = studentJobCommunicationService.myPage(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("学生端确认沟通")
    @PutMapping("/confirm/{recordId}")
    public Result confirm(
            @PathVariable Long recordId,
            @RequestBody(required = false) CommunicationConfirmDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Map<String, Object> data = studentJobCommunicationService.confirm(recordId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(data);
    }
}
