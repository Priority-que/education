package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测验题目Mapper接口
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
    
    /**
     * 查询测验的所有题目（学生端，不包含正确答案）
     * @param examId 测验ID
     * @return 题目列表
     */
    List<ExamQuestion> selectQuestionsByExamId(@Param("examId") Long examId);
    
    /**
     * 查询测验的所有题目（包含正确答案，用于查看结果）
     * @param examId 测验ID
     * @return 题目列表
     */
    List<ExamQuestion> selectQuestionsWithAnswerByExamId(@Param("examId") Long examId);

    /**
     * 教师端根据题目ID查询题目（带教师权限）。
     * @param questionId 题目ID
     * @param teacherId 教师ID
     * @return 题目
     */
    ExamQuestion selectTeacherQuestionById(@Param("questionId") Long questionId, @Param("teacherId") Long teacherId);

    /**
     * 根据测验ID统计题目数。
     * @param examId 测验ID
     * @return 题目数
     */
    int countByExamId(@Param("examId") Long examId);

    /**
     * 根据测验ID删除题目。
     * @param examId 测验ID
     * @return 影响行数
     */
    int deleteByExamId(@Param("examId") Long examId);
}
