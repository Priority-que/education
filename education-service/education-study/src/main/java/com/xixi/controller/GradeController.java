package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.GradeBatchCreateDto;
import com.xixi.pojo.dto.GradeCreateDto;
import com.xixi.pojo.dto.GradePublishDto;
import com.xixi.pojo.dto.GradeUnpublishDto;
import com.xixi.pojo.dto.GradeUpdateDto;
import com.xixi.pojo.dto.GradeWeightDto;
import com.xixi.pojo.query.GradeQuery;
import com.xixi.pojo.vo.CreditSummaryVo;
import com.xixi.pojo.vo.GpaVo;
import com.xixi.pojo.vo.GradeStatisticsVo;
import com.xixi.pojo.vo.GradeVo;
import com.xixi.pojo.vo.TeacherCourseGradeStatisticsVo;
import com.xixi.service.GradeService;
import com.xixi.support.CurrentTeacherResolver;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩控制器（学生端 + 教师端）
 */
@RestController
@RequestMapping("/study/grade")
@RequiredArgsConstructor
public class GradeController {
    
    private final GradeService gradeService;
    private final CurrentTeacherResolver currentTeacherResolver;
    private final CurrentStudentResolver currentStudentResolver;

    /**
     * 教师端：录入成绩
     * @param dto 录入参数
     * @return 结果
     */
    @PostMapping("/create")
    public Result createGrade(@RequestBody GradeCreateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return gradeService.createGrade(dto);
    }

    /**
     * 教师端：批量录入成绩
     * @param dto 批量录入参数
     * @return 结果
     */
    @PostMapping("/batchCreate")
    public Result batchCreateGrade(@RequestBody GradeBatchCreateDto dto) {
        if (dto.getGradeList() != null) {
            Long teacherId = currentTeacherResolver.requireCurrentTeacherId();
            dto.getGradeList().forEach(item -> item.setTeacherId(teacherId));
        }
        return gradeService.batchCreateGrade(dto);
    }

    /**
     * 教师端：修改成绩
     * @param dto 修改参数
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateGrade(@RequestBody GradeUpdateDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return gradeService.updateGrade(dto);
    }

    /**
     * 教师端：设置成绩权重
     * @param courseId 课程ID
     * @param dto 权重参数
     * @return 结果
     */
    @PutMapping("/weight/{courseId}")
    public Result setGradeWeight(@PathVariable Long courseId, @RequestBody GradeWeightDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return gradeService.setGradeWeight(courseId, dto);
    }

    /**
     * 教师端：发布成绩
     * @param dto 发布参数
     * @return 结果
     */
    @PostMapping("/publish")
    public Result publishGrade(@RequestBody GradePublishDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return gradeService.publishGrade(dto);
    }

    /**
     * 教师端：撤销成绩发布
     * @param dto 撤销参数
     * @return 结果
     */
    @PostMapping("/unpublish")
    public Result unpublishGrade(@RequestBody GradeUnpublishDto dto) {
        dto.setTeacherId(currentTeacherResolver.requireCurrentTeacherId());
        return gradeService.unpublishGrade(dto);
    }

    /**
     * 教师端：查看课程成绩列表
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/teacher/course/{courseId}")
    public Result getTeacherCourseGrades(@PathVariable Long courseId, GradeQuery query) {
        IPage<GradeVo> page = gradeService.getTeacherCourseGrades(courseId, currentTeacherResolver.requireCurrentTeacherId(), query);
        return Result.success(page);
    }

    /**
     * 教师端：导出课程成绩单
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 导出数据
     */
    @GetMapping("/export/{courseId}")
    public Result exportCourseGrades(@PathVariable Long courseId) {
        List<GradeVo> exportData = gradeService.exportCourseGrades(courseId, currentTeacherResolver.requireCurrentTeacherId());
        return Result.success(exportData);
    }

    /**
     * 教师端：成绩统计分析
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 统计结果
     */
    @GetMapping("/teacher/statistics/{courseId}")
    public Result getTeacherCourseStatistics(@PathVariable Long courseId) {
        TeacherCourseGradeStatisticsVo vo = gradeService.getTeacherCourseStatistics(courseId, currentTeacherResolver.requireCurrentTeacherId());
        return Result.success(vo);
    }
    
    /**
     * 查看我的成绩列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/myGrades")
    public Result getMyGrades(GradeQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<GradeVo> page = gradeService.getMyGrades(query);
        return Result.success(page);
    }
    
    /**
     * 查看课程成绩详情
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 成绩详情
     */
    @GetMapping("/course/{courseId}")
    public Result getCourseGrade(@PathVariable Long courseId) {
        GradeVo vo = gradeService.getCourseGrade(courseId, currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
    
    /**
     * 成绩统计分析
     * @param studentId 学生ID
     * @return 成绩统计
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        GradeStatisticsVo vo = gradeService.getStatistics(currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
    
    /**
     * 学分累计统计
     * @param studentId 学生ID
     * @return 学分统计
     */
    @GetMapping("/creditSummary")
    public Result getCreditSummary() {
        CreditSummaryVo vo = gradeService.getCreditSummary(currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
    
    /**
     * GPA计算与展示
     * @param studentId 学生ID
     * @return GPA统计
     */
    @GetMapping("/gpa")
    public Result getGpa() {
        GpaVo vo = gradeService.getGpa(currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
}
















