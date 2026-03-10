package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.HomeworkSubmissionGradeDto;
import com.xixi.pojo.dto.HomeworkSubmissionDto;
import com.xixi.pojo.query.HomeworkSubmissionQuery;
import com.xixi.pojo.vo.HomeworkSubmissionAttachmentVo;
import com.xixi.pojo.vo.HomeworkSubmissionAnalysisVo;
import com.xixi.pojo.vo.HomeworkSubmissionStatisticsVo;
import com.xixi.pojo.vo.HomeworkSubmissionVo;
import com.xixi.service.HomeworkService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业提交控制器（学生端 + 教师端）。
 */
@RestController
@RequestMapping("/study/homeworkSubmission")
@RequiredArgsConstructor
public class HomeworkSubmissionController {
    
    private final HomeworkService homeworkService;
    private final CurrentTeacherResolver currentTeacherResolver;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 提交作业
     * @param dto 作业提交DTO
     * @return 结果
     */
    @PostMapping("/submit")
    public Result submitHomework(@RequestBody HomeworkSubmissionDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return homeworkService.submitHomework(dto);
    }
    
    /**
     * 修改作业提交
     * @param dto 作业提交DTO
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateHomeworkSubmission(@RequestBody HomeworkSubmissionDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return homeworkService.updateHomeworkSubmission(dto);
    }
    
    /**
     * 查看我的作业提交列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/mySubmissions")
    public Result getMySubmissions(HomeworkSubmissionQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<HomeworkSubmissionVo> page = homeworkService.getMySubmissions(query);
        return Result.success(page);
    }
    
    /**
     * 查看作业批改结果
     * @param submissionId 提交ID
     * @return 批改结果
     */
    @GetMapping("/result/{submissionId}")
    public Result getSubmissionResult(@PathVariable Long submissionId) {
        HomeworkSubmissionVo vo = homeworkService.getSubmissionResult(submissionId);
        return Result.success(vo);
    }

    /**
     * 下载指定作业提交的附件信息
     * @param submissionId 提交ID
     * @return 附件信息
     */
    @GetMapping("/download/{submissionId}")
    public Result downloadSubmissionAttachment(@PathVariable Long submissionId) {
        HomeworkSubmissionAttachmentVo vo = homeworkService.downloadSubmissionAttachment(submissionId);
        return Result.success(vo);
    }

    /**
     * 批量获取某作业下所有可下载附件
     * @param homeworkId 作业ID
     * @return 附件列表
     */
    @GetMapping("/batchDownload/{homeworkId}")
    public Result batchDownloadAttachments(@PathVariable Long homeworkId) {
        List<HomeworkSubmissionAttachmentVo> list = homeworkService.batchDownloadAttachments(homeworkId);
        return Result.success(list);
    }

    /**
     * 查看作业提交统计结果
     * @param homeworkId 作业ID
     * @return 统计结果
     */
    @GetMapping("/statistics/{homeworkId}")
    public Result getHomeworkSubmissionStatistics(@PathVariable Long homeworkId) {
        HomeworkSubmissionStatisticsVo vo = homeworkService.getHomeworkSubmissionStatistics(homeworkId);
        return Result.success(vo);
    }

    /**
     * 教师端查看作业提交列表。
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/teacher/list")
    public Result getTeacherSubmissionList(HomeworkSubmissionQuery query) {
        query.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        IPage<HomeworkSubmissionVo> page = homeworkService.getTeacherSubmissions(query);
        return Result.success(page);
    }

    /**
     * 教师端批改作业。
     * @param dto 批改参数
     * @return 批改结果
     */
    @PostMapping("/grade")
    public Result gradeHomeworkSubmission(@RequestBody HomeworkSubmissionGradeDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return homeworkService.gradeHomeworkSubmission(dto);
    }

    /**
     * 教师端分析课程作业完成情况。
     * @param courseId 课程ID
     * @return 作业分析结果
     */
    @GetMapping("/analysis/{courseId}")
    public Result getHomeworkAnalysis(@PathVariable Long courseId) {
        HomeworkSubmissionAnalysisVo vo = homeworkService.getHomeworkAnalysis(courseId);
        return Result.success(vo);
    }
}

