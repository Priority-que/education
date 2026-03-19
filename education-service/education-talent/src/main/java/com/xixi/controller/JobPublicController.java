package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.talent.JobPublicPageQueryDto;
import com.xixi.pojo.vo.talent.JobPublicDetailVo;
import com.xixi.pojo.vo.talent.JobPublicPageVo;
import com.xixi.service.StudentJobService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端岗位公开查询控制器。
 */
@RestController
@RequestMapping("/job/public")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class JobPublicController {
    private final StudentJobService studentJobService;

    @MethodPurpose("学生端岗位广场分页")
    @GetMapping("/page")
    public Result publicPage(
            JobPublicPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<JobPublicPageVo> page = studentJobService.publicPage(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("学生端岗位详情")
    @GetMapping("/detail/{jobId}")
    public Result publicDetail(
            @PathVariable Long jobId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        JobPublicDetailVo detailVo = studentJobService.publicDetail(jobId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detailVo);
    }
}
