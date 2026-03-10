package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.ExamCreateDto;
import com.xixi.pojo.dto.ExamSubmissionDto;
import com.xixi.pojo.dto.ExamUpdateDto;
import com.xixi.pojo.query.ExamQuery;
import com.xixi.pojo.query.ExamSubmissionQuery;
import com.xixi.pojo.vo.ExamResultVo;
import com.xixi.pojo.vo.ExamVo;
import com.xixi.pojo.vo.ExamSubmissionVo;
import com.xixi.service.ExamService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 测验控制器（学生端 + 教师端）
 */
@RestController
@RequestMapping("/study/exam")
@RequiredArgsConstructor
public class ExamController {
    
    private final ExamService examService;
    private final CurrentTeacherResolver currentTeacherResolver;
    private final CurrentStudentResolver currentStudentResolver;

    /**
     * 教师端创建测验
     * @param dto 创建参数
     * @return 结果
     */
    @PostMapping("/create")
    public Result createExam(@RequestBody ExamCreateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.createExam(dto);
    }

    /**
     * 教师端编辑测验
     * @param dto 编辑参数
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateExam(@RequestBody ExamUpdateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.updateExam(dto);
    }

    /**
     * 教师端发布测验
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 结果
     */
    @PostMapping("/publish/{examId}")
    public Result publishExam(@PathVariable Long examId) {
        return examService.publishExam(examId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端关闭测验
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 结果
     */
    @PostMapping("/close/{examId}")
    public Result closeExam(@PathVariable Long examId) {
        return examService.closeExam(examId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端删除测验
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 结果
     */
    @DeleteMapping("/delete/{examId}")
    public Result deleteExam(@PathVariable Long examId) {
        return examService.deleteExam(examId, currentTeacherResolver.requireCurrentTeacherId());
    }

    /**
     * 教师端查看测验列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/teacher/list")
    public Result getTeacherExamList(ExamQuery query) {
        query.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        IPage<ExamVo> page = examService.getTeacherExamList(query);
        return Result.success(page);
    }
    
    /**
     * 查看课程测验列表
     * @param courseId 课程ID
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/course/{courseId}")
    public Result getCourseExamList(@PathVariable Long courseId, ExamQuery query) {
        query.setCourseId(courseId);
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<ExamVo> page = examService.getCourseExamList(query);
        return Result.success(page);
    }
    
    /**
     * 查看测验详情
     * @param examId 测验ID
     * @return 测验详情
     */
    @GetMapping("/detail/{examId}")
    public Result getExamDetail(@PathVariable Long examId) {
        ExamVo vo = examService.getExamDetail(examId);
        return Result.success(vo);
    }
    
    /**
     * 开始测验
     * @param examId 测验ID
     * @param studentId 学生ID
     * @return 题目列表
     */
    @PostMapping("/submission/start")
    public Result startExam(@RequestParam Long examId) {
        return examService.startExam(examId, currentStudentResolver.requireCurrentStudentId());
    }
    
    /**
     * 提交答案（保存答题进度）
     * @param dto 测验提交DTO
     * @return 结果
     */
    @PutMapping("/submission/submitAnswer")
    public Result submitAnswer(@RequestBody ExamSubmissionDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return examService.submitAnswer(dto);
    }
    
    /**
     * 提交测验
     * @param examId 测验ID
     * @param studentId 学生ID
     * @return 结果
     */
    @PostMapping("/submission/submit/{examId}")
    public Result submitExam(@PathVariable Long examId) {
        return examService.submitExam(examId, currentStudentResolver.requireCurrentStudentId());
    }
    
    /**
     * 查看测验结果
     * @param submissionId 提交ID
     * @return 测验结果
     */
    @GetMapping("/submission/result/{submissionId}")
    public Result getExamResult(@PathVariable Long submissionId) {
        ExamResultVo vo = examService.getExamResult(submissionId);
        return Result.success(vo);
    }
    
    /**
     * 查看我的测验记录
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/submission/mySubmissions")
    public Result getMySubmissions(ExamSubmissionQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<ExamSubmissionVo> page = examService.getMySubmissions(query);
        return Result.success(page);
    }
}
