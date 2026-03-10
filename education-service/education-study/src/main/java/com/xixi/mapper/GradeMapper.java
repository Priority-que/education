package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Grade;
import com.xixi.pojo.query.GradeQuery;
import com.xixi.pojo.vo.GradeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 成绩Mapper接口
 */
@Mapper
public interface GradeMapper extends BaseMapper<Grade> {
    
    /**
     * 分页查询成绩列表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<GradeVo> selectGradePage(Page<GradeVo> page, @Param("q") GradeQuery query);

    /**
     * 教师端：分页查看课程成绩列表
     * @param page 分页对象
     * @param courseId 课程ID
     * @param query 查询条件
     * @return 分页结果
     */
    Page<GradeVo> selectTeacherCourseGradePage(Page<GradeVo> page,
                                               @Param("courseId") Long courseId,
                                               @Param("q") GradeQuery query);

    /**
     * 教师端：导出课程成绩列表
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<GradeVo> selectTeacherCourseGradeList(@Param("courseId") Long courseId);
    
    /**
     * 根据课程ID和学生ID查询成绩
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 成绩详情
     */
    GradeVo selectGradeByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    /**
     * 统计总学分
     * @param studentId 学生ID
     * @return 总学分
     */
    BigDecimal selectTotalCredits(@Param("studentId") Long studentId);
    
    /**
     * 统计平均GPA
     * @param studentId 学生ID
     * @return 平均GPA
     */
    BigDecimal selectAverageGpa(@Param("studentId") Long studentId);
    
    /**
     * 统计成绩分布
     * @param studentId 学生ID
     * @return 成绩分布（等级和数量）
     */
    List<Map<String, Object>> selectGradeDistribution(@Param("studentId") Long studentId);
    
    /**
     * 查询各课程成绩趋势
     * @param studentId 学生ID
     * @return 课程成绩列表
     */
    List<Map<String, Object>> selectCourseGradeTrends(@Param("studentId") Long studentId);
    
    /**
     * 查询各课程学分明细
     * @param studentId 学生ID
     * @return 课程学分明细
     */
    List<Map<String, Object>> selectCourseCredits(@Param("studentId") Long studentId);
    
    /**
     * 查询GPA明细
     * @param studentId 学生ID
     * @return GPA明细
     */
    List<Map<String, Object>> selectGpaDetails(@Param("studentId") Long studentId);

    /**
     * 按学期统计学分
     * @param studentId 学生ID
     * @return 学期学分统计
     */
    List<Map<String, Object>> selectSemesterCredits(@Param("studentId") Long studentId);

    /**
     * 查询GPA趋势
     * @param studentId 学生ID
     * @return GPA趋势
     */
    List<Map<String, Object>> selectGpaTrends(@Param("studentId") Long studentId);

    /**
     * 教师端：按课程和学生查询成绩实体
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 成绩实体
     */
    Grade selectEntityByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    /**
     * 教师端：按课程查询成绩实体列表
     * @param courseId 课程ID
     * @return 成绩实体列表
     */
    List<Grade> selectEntityListByCourseId(@Param("courseId") Long courseId);

    /**
     * 教师端：批量发布成绩
     * @param gradeIds 成绩ID列表
     * @param publishedBy 发布教师ID
     * @param publishedTime 发布时间
     * @return 影响行数
     */
    int batchPublish(@Param("gradeIds") List<Long> gradeIds,
                     @Param("publishedBy") Long publishedBy,
                     @Param("publishedTime") LocalDateTime publishedTime);

    /**
     * 教师端：批量撤销成绩发布
     * @param gradeIds 成绩ID列表
     * @return 影响行数
     */
    int batchUnpublish(@Param("gradeIds") List<Long> gradeIds);

    /**
     * 教师端：课程成绩统计概览
     * @param courseId 课程ID
     * @return 统计结果
     */
    Map<String, Object> selectTeacherCourseStatistics(@Param("courseId") Long courseId);

    /**
     * 教师端：课程成绩等级分布
     * @param courseId 课程ID
     * @return 分布列表
     */
    List<Map<String, Object>> selectTeacherCourseGradeDistribution(@Param("courseId") Long courseId);

    /**
     * 教师端：课程成绩分数段分布
     * @param courseId 课程ID
     * @return 分布列表
     */
    List<Map<String, Object>> selectTeacherCourseScoreRangeDistribution(@Param("courseId") Long courseId);
}
















