package com.xixi.service;

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
import com.xixi.web.Result;

import java.util.List;

/**
 * 成绩服务接口（学生端 + 教师端）
 */
public interface GradeService {

    /**
     * 教师端：录入成绩
     * @param dto 录入参数
     * @return 结果
     */
    Result createGrade(GradeCreateDto dto);

    /**
     * 教师端：批量录入成绩
     * @param dto 批量录入参数
     * @return 结果
     */
    Result batchCreateGrade(GradeBatchCreateDto dto);

    /**
     * 教师端：修改成绩
     * @param dto 修改参数
     * @return 结果
     */
    Result updateGrade(GradeUpdateDto dto);

    /**
     * 教师端：设置课程成绩权重
     * @param courseId 课程ID
     * @param dto 权重参数
     * @return 结果
     */
    Result setGradeWeight(Long courseId, GradeWeightDto dto);

    /**
     * 教师端：发布成绩
     * @param dto 发布参数
     * @return 结果
     */
    Result publishGrade(GradePublishDto dto);

    /**
     * 教师端：撤销成绩发布
     * @param dto 撤销参数
     * @return 结果
     */
    Result unpublishGrade(GradeUnpublishDto dto);

    /**
     * 教师端：查看课程成绩列表
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<GradeVo> getTeacherCourseGrades(Long courseId, Long teacherId, GradeQuery query);

    /**
     * 教师端：导出课程成绩单
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    List<GradeVo> exportCourseGrades(Long courseId, Long teacherId);

    /**
     * 教师端：课程成绩统计分析
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 统计结果
     */
    TeacherCourseGradeStatisticsVo getTeacherCourseStatistics(Long courseId, Long teacherId);
    
    /**
     * 查看我的成绩列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<GradeVo> getMyGrades(GradeQuery query);
    
    /**
     * 查看课程成绩详情
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 成绩详情
     */
    GradeVo getCourseGrade(Long courseId, Long studentId);
    
    /**
     * 成绩统计分析
     * @param studentId 学生ID
     * @return 成绩统计
     */
    GradeStatisticsVo getStatistics(Long studentId);
    
    /**
     * 学分累计统计
     * @param studentId 学生ID
     * @return 学分统计
     */
    CreditSummaryVo getCreditSummary(Long studentId);
    
    /**
     * GPA计算与展示
     * @param studentId 学生ID
     * @return GPA统计
     */
    GpaVo getGpa(Long studentId);
}
















