package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.ExamCreateDto;
import com.xixi.pojo.dto.ExamQuestionBatchImportDto;
import com.xixi.pojo.dto.ExamQuestionCreateDto;
import com.xixi.pojo.dto.ExamQuestionUpdateDto;
import com.xixi.pojo.dto.ExamSubmissionGradeDto;
import com.xixi.pojo.dto.ExamSubmissionDto;
import com.xixi.pojo.dto.ExamUpdateDto;
import com.xixi.pojo.query.ExamQuery;
import com.xixi.pojo.query.ExamSubmissionQuery;
import com.xixi.pojo.vo.CourseExamAnalysisVo;
import com.xixi.pojo.vo.ExamResultVo;
import com.xixi.pojo.vo.ExamQuestionVo;
import com.xixi.pojo.vo.ExamSubmissionStatisticsVo;
import com.xixi.pojo.vo.ExamVo;
import com.xixi.pojo.vo.ExamSubmissionVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 测验服务接口（学生端）
 */
public interface ExamService {

    /**
     * 教师端创建测验。
     * @param dto 创建参数
     * @return 创建结果
     */
    Result createExam(ExamCreateDto dto);

    /**
     * 教师端编辑测验。
     * @param dto 编辑参数
     * @return 编辑结果
     */
    Result updateExam(ExamUpdateDto dto);

    /**
     * 教师端发布测验。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 发布结果
     */
    Result publishExam(Long examId, Long teacherId);

    /**
     * 教师端关闭测验。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 关闭结果
     */
    Result closeExam(Long examId, Long teacherId);

    /**
     * 教师端删除测验（仅草稿）。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 删除结果
     */
    Result deleteExam(Long examId, Long teacherId);

    /**
     * 教师端查询测验列表。
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExamVo> getTeacherExamList(ExamQuery query);

    /**
     * 教师端新增题目。
     * @param dto 题目参数
     * @return 结果
     */
    Result addExamQuestion(ExamQuestionCreateDto dto);

    /**
     * 教师端编辑题目。
     * @param dto 题目参数
     * @return 结果
     */
    Result updateExamQuestion(ExamQuestionUpdateDto dto);

    /**
     * 教师端删除题目。
     * @param questionId 题目ID
     * @param teacherId 教师ID
     * @return 结果
     */
    Result deleteExamQuestion(Long questionId, Long teacherId);

    /**
     * 教师端批量导入题目。
     * @param dto 导入参数
     * @return 结果
     */
    Result batchImportExamQuestion(ExamQuestionBatchImportDto dto);

    /**
     * 查看题目列表。
     * 教师返回包含正确答案的数据，学生返回不包含正确答案的数据。
     * @param examId 测验ID
     * @param userId 当前用户ID
     * @return 题目列表
     */
    List<ExamQuestionVo> getExamQuestionList(Long examId, Long userId);

    /**
     * 教师端查看测验提交列表。
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExamSubmissionVo> getTeacherExamSubmissions(ExamSubmissionQuery query);

    /**
     * 教师端批改主观题。
     * @param dto 批改参数
     * @return 批改结果
     */
    Result gradeExamSubmission(ExamSubmissionGradeDto dto);

    /**
     * 教师端查看测验统计。
     * @param examId 测验ID
     * @param teacherId 教师ID
     * @return 统计结果
     */
    ExamSubmissionStatisticsVo getExamSubmissionStatistics(Long examId, Long teacherId);

    /**
     * 教师端按课程分析测验成绩分布。
     * @param courseId 课程ID
     * @return 测验分析结果
     */
    CourseExamAnalysisVo getCourseExamAnalysis(Long courseId);
    
    /**
     * 查看课程测验列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExamVo> getCourseExamList(ExamQuery query);
    
    /**
     * 查看测验详情
     * @param examId 测验ID
     * @return 测验详情
     */
    ExamVo getExamDetail(Long examId);
    
    /**
     * 开始测验
     * @param examId 测验ID
     * @param studentId 学生ID
     * @return 题目列表（不包含正确答案）
     */
    Result startExam(Long examId, Long studentId);
    
    /**
     * 提交答案（保存答题进度）
     * @param dto 测验提交DTO
     * @return 结果
     */
    Result submitAnswer(ExamSubmissionDto dto);
    
    /**
     * 提交测验
     * @param examId 测验ID
     * @param studentId 学生ID
     * @return 结果
     */
    Result submitExam(Long examId, Long studentId);
    
    /**
     * 查看测验结果
     * @param submissionId 提交ID
     * @return 测验结果
     */
    ExamResultVo getExamResult(Long submissionId);
    
    /**
     * 查看我的测验记录
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExamSubmissionVo> getMySubmissions(ExamSubmissionQuery query);
}
