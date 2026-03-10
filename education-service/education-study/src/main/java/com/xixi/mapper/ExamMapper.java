package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Exam;
import com.xixi.pojo.query.ExamQuery;
import com.xixi.pojo.vo.ExamVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 测验Mapper接口
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
    
    /**
     * 分页查询课程测验列表（学生端）
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ExamVo> selectExamPage(Page<ExamVo> page, @Param("q") ExamQuery query);

    /**
     * 教师端分页查询测验列表。
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ExamVo> selectTeacherExamPage(Page<ExamVo> page, @Param("q") ExamQuery query);
    
    /**
     * 根据ID查询测验详情
     * @param examId 测验ID
     * @return 测验详情
     */
    ExamVo selectExamDetail(@Param("examId") Long examId);

    /**
     * 教师端根据测验ID查询测验实体。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 测验实体
     */
    Exam selectTeacherExamById(@Param("examId") Long examId, @Param("teacherId") Long teacherId);

    /**
     * 教师端删除草稿测验。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 影响行数
     */
    int deleteDraftExam(@Param("examId") Long examId, @Param("teacherId") Long teacherId);
}
