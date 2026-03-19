package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.ExamSubmission;
import com.xixi.pojo.query.ExamSubmissionQuery;
import com.xixi.pojo.vo.StudentCourseDetailVo;
import com.xixi.pojo.vo.TeacherStudentDetailVo;
import com.xixi.pojo.vo.ExamSubmissionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测验提交Mapper接口
 */
@Mapper
public interface ExamSubmissionMapper extends BaseMapper<ExamSubmission> {
    
    /**
     * 分页查询测验提交列表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ExamSubmissionVo> selectExamSubmissionPage(Page<ExamSubmissionVo> page, @Param("q") ExamSubmissionQuery query);

    /**
     * 教师端分页查询测验提交列表。
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ExamSubmissionVo> selectTeacherExamSubmissionPage(Page<ExamSubmissionVo> page, @Param("q") ExamSubmissionQuery query);
    
    /**
     * 根据测验ID和学生ID查询提交记录
     * @param examId 测验ID
     * @param studentId 学生ID
     * @return 提交记录
     */
    ExamSubmission selectByExamAndStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);
    
    /**
     * 根据ID查询提交详情
     * @param submissionId 提交ID
     * @return 提交详情
     */
    ExamSubmissionVo selectSubmissionDetail(@Param("submissionId") Long submissionId);

    /**
     * 教师端根据提交ID查询详情（带教师权限）。
     * @param submissionId 提交ID
     * @param teacherId 教师ID
     * @return 提交详情
     */
    ExamSubmissionVo selectTeacherExamSubmissionDetail(@Param("submissionId") Long submissionId,
                                                       @Param("teacherId") Long teacherId);

    /**
     * 查询测验已完成提交记录（用于统计）。
     * @param examId 测验ID
     * @return 提交列表
     */
    List<ExamSubmission> selectCompletedByExamId(@Param("examId") Long examId);

    /**
     * 查询学生在课程下的测验完成进度（包含未参与测验）
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 测验进度列表
     */
    List<StudentCourseDetailVo.ExamProgressVo> selectExamProgressByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /**
     * 教师端查询学生在课程下的测验提交情况
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 测验提交情况列表
     */
    List<TeacherStudentDetailVo.ExamSubmissionInfo> selectTeacherStudentExamSubmissions(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId);

    /**
     * 教师端按课程统计各测验平均分。
     * @param courseId 课程ID
     * @return 测验平均分
     */
    List<Map<String, Object>> selectCourseExamAverageScores(@Param("courseId") Long courseId);

    /**
     * 教师端按课程统计测验成绩分段分布。
     * @param courseId 课程ID
     * @return 成绩分段分布
     */
    List<Map<String, Object>> selectCourseExamScoreRangeDistribution(@Param("courseId") Long courseId);

    /**
     * 教师端按课程查询已完成测验提交记录。
     * @param courseId 课程ID
     * @return 提交记录
     */
    List<ExamSubmission> selectCompletedByCourseId(@Param("courseId") Long courseId);

    /**
     * 教师端按课程查询客观题列表（含正确答案）。
     * @param courseId 课程ID
     * @return 题目列表
     */
    List<Map<String, Object>> selectCourseObjectiveQuestionList(@Param("courseId") Long courseId);
}
