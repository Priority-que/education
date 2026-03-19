package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.CommunicationRecord;
import com.xixi.entity.JobPosting;
import com.xixi.pojo.dto.talent.CommunicationRecordPageQueryDto;
import com.xixi.pojo.dto.talent.CommunicationRecordSendDto;
import com.xixi.pojo.dto.talent.JobApplicationStatusUpdateDto;
import com.xixi.pojo.dto.talent.JobPostingCreateDto;
import com.xixi.pojo.dto.talent.JobPostingPageQueryDto;
import com.xixi.pojo.dto.talent.JobPostingStatusUpdateDto;
import com.xixi.pojo.dto.talent.JobPostingUpdateDto;
import com.xixi.pojo.dto.talent.TalentJobApplicationPageQueryDto;
import com.xixi.pojo.vo.talent.TalentCommunicationPageVo;
import com.xixi.pojo.vo.talent.TalentJobApplicationDetailVo;
import com.xixi.pojo.vo.talent.TalentJobApplicationPageVo;
import com.xixi.pojo.vo.talent.TalentJobDetailVo;
import com.xixi.service.TalentJobApplicationService;
import com.xixi.service.TalentJobCommunicationService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 企业端招聘管理与沟通控制器。
 */
@RestController
@RequestMapping("/talent")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ENTERPRISE})
public class TalentJobCommunicationController {
    private final TalentJobCommunicationService talentJobCommunicationService;
    private final TalentJobApplicationService talentJobApplicationService;

    @MethodPurpose("岗位分页：查询企业岗位列表")
    @GetMapping("/job/page")
    public Result pageJobs(
            JobPostingPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<JobPosting> page = talentJobCommunicationService.pageJobs(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("岗位详情：查询企业岗位信息与投递统计")
    @GetMapping("/job/detail/{jobId}")
    public Result getJobDetail(
            @PathVariable Long jobId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        TalentJobDetailVo detail = talentJobCommunicationService.getJobDetail(jobId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detail);
    }

    @MethodPurpose("岗位创建：新增企业招聘岗位")
    @PostMapping("/job/create")
    public Result createJob(
            @RequestBody JobPostingCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long jobId = talentJobCommunicationService.createJob(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("创建成功", jobId);
    }

    @MethodPurpose("岗位更新：修改岗位基础信息")
    @PutMapping("/job/update/{jobId}")
    public Result updateJob(
            @PathVariable Long jobId,
            @RequestBody JobPostingUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentJobCommunicationService.updateJob(jobId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("更新成功");
    }

    @MethodPurpose("岗位状态更新")
    @PutMapping("/job/status/{jobId}")
    public Result updateJobStatus(
            @PathVariable Long jobId,
            @RequestBody JobPostingStatusUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentJobCommunicationService.updateJobStatus(jobId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("状态更新成功");
    }

    @MethodPurpose("删除岗位")
    @DeleteMapping("/job/delete/{jobId}")
    public Result deleteJob(
            @PathVariable Long jobId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentJobCommunicationService.deleteJob(jobId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("删除成功");
    }

    @MethodPurpose("岗位投递分页")
    @GetMapping("/job/application/page")
    public Result pageApplications(
            TalentJobApplicationPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<TalentJobApplicationPageVo> page = talentJobApplicationService.page(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("岗位投递详情")
    @GetMapping("/job/application/detail/{applicationId}")
    public Result applicationDetail(
            @PathVariable Long applicationId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        TalentJobApplicationDetailVo detailVo = talentJobApplicationService.detail(applicationId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detailVo);
    }

    @MethodPurpose("更新岗位投递状态")
    @PutMapping("/job/application/status/{applicationId}")
    public Result updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody JobApplicationStatusUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Map<String, Object> data = talentJobApplicationService.updateStatus(applicationId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(data);
    }

    @MethodPurpose("发送沟通记录")
    @PostMapping("/communication/send")
    public Result sendCommunication(
            @RequestBody CommunicationRecordSendDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Map<String, Object> data = talentJobCommunicationService.sendCommunication(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(data);
    }

    @MethodPurpose("企业沟通分页")
    @GetMapping("/communication/page")
    public Result pageCommunication(
            CommunicationRecordPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<TalentCommunicationPageVo> page = talentJobCommunicationService.pageCommunication(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("沟通详情")
    @GetMapping("/communication/detail/{recordId}")
    public Result getCommunicationDetail(
            @PathVariable Long recordId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        CommunicationRecord detail = talentJobCommunicationService.getCommunicationDetail(recordId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detail);
    }

    @MethodPurpose("沟通已读标记")
    @PutMapping("/communication/read/{recordId}")
    public Result markCommunicationRead(
            @PathVariable Long recordId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentJobCommunicationService.markCommunicationRead(recordId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("标记成功");
    }
}
