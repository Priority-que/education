package com.xixi.controller;

import com.xixi.pojo.query.LearningStatisticsQuery;
import com.xixi.pojo.vo.LearningStatisticsOverviewVo;
import com.xixi.pojo.vo.LearningStatisticsProgressVo;
import com.xixi.pojo.vo.LearningStatisticsSummaryVo;
import com.xixi.pojo.vo.LearningStatisticsTimeVo;
import com.xixi.pojo.vo.TeacherCourseActivityVo;
import com.xixi.pojo.vo.TeacherCourseProgressAnalysisVo;
import com.xixi.pojo.vo.TeacherCourseTimeAnalysisVo;
import com.xixi.service.LearningStatisticsService;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习统计控制器
 */
@RestController
@RequestMapping("/study/learningStatistics")
@RequiredArgsConstructor
public class LearningStatisticsController {
    
    private final LearningStatisticsService learningStatisticsService;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 查看学习统计概览
     * @param studentId 学生ID
     * @return 学习统计概览
     */
    @GetMapping("/overview")
    public Result getOverview() {
        LearningStatisticsOverviewVo vo = learningStatisticsService.getOverview(
                currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
    
    /**
     * 查看学习时长统计
     * @param query 查询条件
     * @return 学习时长统计
     */
    @GetMapping("/studyTime")
    public Result getStudyTime(LearningStatisticsQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        LearningStatisticsTimeVo vo = learningStatisticsService.getStudyTime(query);
        return Result.success(vo);
    }
    
    /**
     * 查看学习进度统计
     * @param studentId 学生ID
     * @return 学习进度统计
     */
    @GetMapping("/progress")
    public Result getProgress() {
        LearningStatisticsProgressVo vo = learningStatisticsService.getProgress(
                currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }
    
    /**
     * 查看学习活跃度统计
     * @param studentId 学生ID
     * @return 学习活跃度统计
     */
    @GetMapping("/activity")
    public Result getActivity() {
        List<Map<String, Object>> activityList = learningStatisticsService.getActivity(
                currentStudentResolver.requireCurrentStudentId());
        return Result.success(activityList);
    }

    /**
     * 汇总学生学习数据
     * @param studentId 学生ID
     * @return 学习数据汇总
     */
    @GetMapping("/summary")
    public Result getSummary() {
        LearningStatisticsSummaryVo vo = learningStatisticsService.getSummary(
                currentStudentResolver.requireCurrentStudentId());
        return Result.success(vo);
    }

    /**
     * 教师端查看课程学生活跃度统计。
     * @param courseId 课程ID
     * @return 学生活跃度统计
     */
    @GetMapping("/teacher/activity/{courseId}")
    public Result getTeacherCourseActivity(@PathVariable Long courseId) {
        TeacherCourseActivityVo vo = learningStatisticsService.getTeacherCourseActivity(courseId);
        return Result.success(vo);
    }

    /**
     * 教师端查看课程学习进度统计分析。
     * @param courseId 课程ID
     * @return 学习进度分析结果
     */
    @GetMapping("/teacher/progress/{courseId}")
    public Result getTeacherCourseProgressAnalysis(@PathVariable Long courseId) {
        TeacherCourseProgressAnalysisVo vo = learningStatisticsService.getTeacherCourseProgressAnalysis(courseId);
        return Result.success(vo);
    }

    /**
     * 教师端查看学生活跃时间段分析。
     * @param courseId 课程ID
     * @return 活跃时间段分析结果
     */
    @GetMapping("/teacher/timeAnalysis/{courseId}")
    public Result getTeacherCourseTimeAnalysis(@PathVariable Long courseId) {
        TeacherCourseTimeAnalysisVo vo = learningStatisticsService.getTeacherCourseTimeAnalysis(courseId);
        return Result.success(vo);
    }
}
