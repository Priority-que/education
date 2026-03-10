package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.HomeworkCreateDto;
import com.xixi.pojo.dto.HomeworkSubmissionDto;
import com.xixi.pojo.dto.HomeworkUpdateDto;
import com.xixi.pojo.query.HomeworkQuery;
import com.xixi.pojo.query.HomeworkSubmissionQuery;
import com.xixi.pojo.vo.HomeworkVo;
import com.xixi.pojo.vo.HomeworkSubmissionVo;
import com.xixi.service.HomeworkService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 作业控制器（学生端）
 */
@RestController
@RequestMapping("/study/homework")
@RequiredArgsConstructor
public class HomeworkController {
    
    private final HomeworkService homeworkService;
    private final CurrentTeacherResolver currentTeacherResolver;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 查看课程作业列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/course/{courseId}")
    public Result getCourseHomeworkList(@PathVariable Long courseId, HomeworkQuery query) {
        query.setCourseId(courseId);
        IPage<HomeworkVo> page = homeworkService.getCourseHomeworkList(query);
        return Result.success(page);
    }
    
    /**
     * 查看作业详情
     * @param homeworkId 作业ID
     * @return 作业详情
     */
    @GetMapping("/detail/{homeworkId}")
    public Result getHomeworkDetail(@PathVariable Long homeworkId) {
        HomeworkVo vo = homeworkService.getHomeworkDetail(homeworkId);
        return Result.success(vo);
    }
    
    /**
     * 提交作业
     * @param dto 作业提交DTO
     * @return 结果
     */
    @PostMapping("/submission/submit")
    public Result submitHomework(@RequestBody HomeworkSubmissionDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return homeworkService.submitHomework(dto);
    }
    
    /**
     * 修改作业提交
     * @param dto 作业提交DTO
     * @return 结果
     */
    @PutMapping("/submission/update")
    public Result updateHomeworkSubmission(@RequestBody HomeworkSubmissionDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return homeworkService.updateHomeworkSubmission(dto);
    }
    
    /**
     * 查看我的作业提交列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/submission/mySubmissions")
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
    @GetMapping("/submission/result/{submissionId}")
    public Result getSubmissionResult(@PathVariable Long submissionId) {
        HomeworkSubmissionVo vo = homeworkService.getSubmissionResult(submissionId);
        return Result.success(vo);
    }

    /**
     * 教师端创建作业。
     * @param dto 创建参数
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result createHomework(@RequestBody HomeworkCreateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return homeworkService.createHomework(dto);
    }

    /**
     * 教师端编辑作业。
     * @param dto 编辑参数
     * @return 编辑结果
     */
    @PutMapping("/update")
    public Result updateHomework(@RequestBody HomeworkUpdateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return homeworkService.updateHomework(dto);
    }

    /**
     * 教师端发布作业。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 发布结果
     */
    @PostMapping("/publish/{homeworkId}")
    public Result publishHomework(@PathVariable Long homeworkId) {
        return homeworkService.publishHomework(homeworkId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端关闭作业。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 关闭结果
     */
    @PostMapping("/close/{homeworkId}")
    public Result closeHomework(@PathVariable Long homeworkId) {
        return homeworkService.closeHomework(homeworkId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端删除作业（仅草稿可删）。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{homeworkId}")
    public Result deleteHomework(@PathVariable Long homeworkId) {
        return homeworkService.deleteHomework(homeworkId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端查看作业列表。
     * @param query 查询条件
     * @return 作业分页
     */
    @GetMapping("/teacher/list")
    public Result getTeacherHomeworkList(HomeworkQuery query) {
        query.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        IPage<HomeworkVo> page = homeworkService.getTeacherHomeworkList(query);
        return Result.success(page);
    }
}
