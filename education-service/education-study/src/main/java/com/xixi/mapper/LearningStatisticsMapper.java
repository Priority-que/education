package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.LearningStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学习统计Mapper接口
 */
@Mapper
public interface LearningStatisticsMapper extends BaseMapper<LearningStatistics> {
    
    /**
     * 统计总学习时长（秒）
     * @param studentId 学生ID
     * @return 总学习时长
     */
    Integer selectTotalStudyTime(@Param("studentId") Long studentId);

    /**
     * 按时间范围统计学习时长（秒）
     * @param studentId 学生ID
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 学习时长（秒）
     */
    Integer selectStudyTimeInRange(@Param("studentId") Long studentId,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);
    
    /**
     * 按日期统计学习时长
     * @param studentId 学生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期和学习时长的映射列表
     */
    List<Map<String, Object>> selectStudyTimeByDate(@Param("studentId") Long studentId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);
    
    /**
     * 统计各课程的学习进度
     * @param studentId 学生ID
     * @return 课程ID和进度百分比的映射列表
     */
    List<Map<String, Object>> selectCourseProgress(@Param("studentId") Long studentId);
    
    /**
     * 统计学习活跃时间段
     * @param studentId 学生ID
     * @return 时间段和学习时长的映射列表
     */
    List<Map<String, Object>> selectStudyTimeByHour(@Param("studentId") Long studentId);

    /**
     * 统计学生作业提交数
     * @param studentId 学生ID
     * @return 作业提交数
     */
    Integer selectHomeworkSubmissionCount(@Param("studentId") Long studentId);

    /**
     * 统计学生当前可见的已发布作业总数（按选课课程范围）。
     * @param studentId 学生ID
     * @return 作业总数
     */
    Integer selectStudentPublishedHomeworkCount(@Param("studentId") Long studentId);

    /**
     * 统计学生在当前可见已发布作业中的已提交数量（排除草稿）。
     * @param studentId 学生ID
     * @return 已提交作业数
     */
    Integer selectStudentSubmittedHomeworkCount(@Param("studentId") Long studentId);

    /**
     * 统计学生测验提交数
     * @param studentId 学生ID
     * @return 测验提交数
     */
    Integer selectExamSubmissionCount(@Param("studentId") Long studentId);

    /**
     * 汇总学生学习数据
     * @param studentId 学生ID
     * @return 汇总结果
     */
    Map<String, Object> selectLearningSummary(@Param("studentId") Long studentId);

    /**
     * 查询学生所有学习日期
     * @param studentId 学生ID
     * @return 学习日期列表
     */
    List<String> selectStudyDateList(@Param("studentId") Long studentId);

    /**
     * 教师端统计课程活跃学生数。
     * @param courseId 课程ID
     * @return 活跃学生数
     */
    Integer selectTeacherCourseActiveStudentCount(@Param("courseId") Long courseId);

    /**
     * 教师端统计课程学习时长排名。
     * @param courseId 课程ID
     * @return 学习时长排名
     */
    List<Map<String, Object>> selectTeacherCourseStudyTimeRanking(@Param("courseId") Long courseId);

    /**
     * 教师端统计课程学习进度排名。
     * @param courseId 课程ID
     * @return 学习进度排名
     */
    List<Map<String, Object>> selectTeacherCourseProgressRanking(@Param("courseId") Long courseId);

    /**
     * 教师端统计课程活跃时间段分布。
     * @param courseId 课程ID
     * @return 活跃时间段分布
     */
    List<Map<String, Object>> selectTeacherCourseActiveTimeDistribution(@Param("courseId") Long courseId);

    /**
     * 教师端查询课程学习进度汇总。
     * @param courseId 课程ID
     * @return 进度汇总
     */
    Map<String, Object> selectTeacherCourseProgressSummary(@Param("courseId") Long courseId);

    /**
     * 教师端查询课程学习进度分段分布。
     * @param courseId 课程ID
     * @return 分段分布
     */
    List<Map<String, Object>> selectTeacherCourseProgressDistribution(@Param("courseId") Long courseId);

    /**
     * 教师端查询课程学习进度趋势。
     * @param courseId 课程ID
     * @return 进度趋势
     */
    List<Map<String, Object>> selectTeacherCourseProgressTrend(@Param("courseId") Long courseId);

    /**
     * 教师端查询课程活跃时间段分析数据。
     * @param courseId 课程ID
     * @return 时间段分析数据
     */
    List<Map<String, Object>> selectTeacherCourseTimeAnalysis(@Param("courseId") Long courseId);
}
