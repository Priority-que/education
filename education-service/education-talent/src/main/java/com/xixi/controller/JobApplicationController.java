package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.talent.JobApplicationCreateDto;
import com.xixi.pojo.dto.talent.StudentJobApplicationPageQueryDto;
import com.xixi.pojo.vo.talent.JobApplicationCreateVo;
import com.xixi.pojo.vo.talent.StudentJobApplicationDetailVo;
import com.xixi.pojo.vo.talent.StudentJobApplicationPageVo;
import com.xixi.service.StudentJobApplicationService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学生端投递控制器。
 */
@RestController
@RequestMapping("/job/application")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class JobApplicationController {
    private final StudentJobApplicationService studentJobApplicationService;

    @MethodPurpose("学生端创建投递")
    @PostMapping("/create")
    public Result create(
            @RequestBody(required = false) JobApplicationCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        JobApplicationCreateVo vo = studentJobApplicationService.create(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(vo);
    }

    @MethodPurpose("学生端我的投递分页")
    @GetMapping("/my/page")
    public Result myPage(
            StudentJobApplicationPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<StudentJobApplicationPageVo> page = studentJobApplicationService.myPage(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("学生端投递详情")
    @GetMapping("/my/detail/{applicationId}")
    public Result myDetail(
            @PathVariable Long applicationId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        StudentJobApplicationDetailVo detailVo = studentJobApplicationService.myDetail(applicationId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detailVo);
    }

    @MethodPurpose("学生端撤回投递")
    @PutMapping("/withdraw/{applicationId}")
    public Result withdraw(
            @PathVariable Long applicationId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Map<String, Object> data = studentJobApplicationService.withdraw(applicationId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(data);
    }
}
