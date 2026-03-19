package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.vo.CourseExamAnalysisVo;
import com.xixi.pojo.dto.ExamSubmissionGradeDto;
import com.xixi.pojo.query.ExamSubmissionQuery;
import com.xixi.pojo.vo.ExamSubmissionStatisticsVo;
import com.xixi.pojo.vo.ExamSubmissionVo;
import com.xixi.service.ExamService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 测验提交控制器（教师端）。
 */
@RestController
@RequestMapping("/study/examSubmission")
@RequiredArgsConstructor
public class ExamSubmissionController {

    private final ExamService examService;
    private final CurrentTeacherResolver currentTeacherResolver;

    /**
     * 查看测验提交列表。
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/teacher/list")
    public Result getTeacherExamSubmissions(ExamSubmissionQuery query) {
        query.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        IPage<ExamSubmissionVo> page = examService.getTeacherExamSubmissions(query);
        return Result.success(page);
    }

    /**
     * 批改主观题。
     * @param dto 批改参数
     * @return 结果
     */
    @PostMapping("/grade")
    public Result gradeExamSubmission(@RequestBody ExamSubmissionGradeDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return examService.gradeExamSubmission(dto);
    }

    /**
     * 查看测验统计。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 统计结果
     */
    @GetMapping("/statistics/{examId}")
    public Result getExamSubmissionStatistics(@PathVariable Long examId) {
        ExamSubmissionStatisticsVo vo = examService.getExamSubmissionStatistics(examId, currentTeacherResolver.requireCurrentTeacherId());
        return Result.success(vo);
    }

    /**
     * 教师端按课程分析测验成绩分布。
     * @param courseId 课程ID
     * @return 测验分析结果
     */
    @GetMapping("/analysis/{courseId}")
    public Result getCourseExamAnalysis(@PathVariable Long courseId) {
        CourseExamAnalysisVo vo = examService.getCourseExamAnalysis(courseId);
        return Result.success(vo);
    }
}
