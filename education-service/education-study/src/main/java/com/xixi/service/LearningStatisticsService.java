package com.xixi.service;

import com.xixi.pojo.query.LearningStatisticsQuery;
import com.xixi.pojo.vo.LearningStatisticsOverviewVo;
import com.xixi.pojo.vo.LearningStatisticsProgressVo;
import com.xixi.pojo.vo.LearningStatisticsSummaryVo;
import com.xixi.pojo.vo.LearningStatisticsTimeVo;
import com.xixi.pojo.vo.TeacherCourseActivityVo;
import com.xixi.pojo.vo.TeacherCourseProgressAnalysisVo;
import com.xixi.pojo.vo.TeacherCourseTimeAnalysisVo;

import java.util.List;
import java.util.Map;

/**
 * 学习统计服务接口
 */
public interface LearningStatisticsService {
    
    /**
     * 查看学习统计概览
     * @param studentId 学生ID
     * @return 学习统计概览
     */
    LearningStatisticsOverviewVo getOverview(Long studentId);
    
    /**
     * 查看学习时长统计
     * @param query 查询条件
     * @return 学习时长统计
     */
    LearningStatisticsTimeVo getStudyTime(LearningStatisticsQuery query);
    
    /**
     * 查看学习进度统计
     * @param studentId 学生ID
     * @return 学习进度统计
     */
    LearningStatisticsProgressVo getProgress(Long studentId);
    
    /**
     * 查看学习活跃度统计
     * @param studentId 学生ID
     * @return 学习活跃度统计（各时间段的学习时长分布）
     */
    List<Map<String, Object>> getActivity(Long studentId);

    /**
     * 汇总学生学习数据
     * @param studentId 学生ID
     * @return 学习数据汇总
     */
    LearningStatisticsSummaryVo getSummary(Long studentId);

    /**
     * 教师端查看课程学生活跃度统计。
     * @param courseId 课程ID
     * @return 活跃度统计结果
     */
    TeacherCourseActivityVo getTeacherCourseActivity(Long courseId);

    /**
     * 教师端查看课程学习进度分析。
     * @param courseId 课程ID
     * @return 进度分析结果
     */
    TeacherCourseProgressAnalysisVo getTeacherCourseProgressAnalysis(Long courseId);

    /**
     * 教师端查看课程活跃时间段分析。
     * @param courseId 课程ID
     * @return 时间段分析结果
     */
    TeacherCourseTimeAnalysisVo getTeacherCourseTimeAnalysis(Long courseId);
}
