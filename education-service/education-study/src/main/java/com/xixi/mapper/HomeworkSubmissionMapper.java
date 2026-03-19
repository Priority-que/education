package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.HomeworkSubmission;
import com.xixi.pojo.query.HomeworkSubmissionQuery;
import com.xixi.pojo.vo.StudentCourseDetailVo;
import com.xixi.pojo.vo.TeacherStudentDetailVo;
import com.xixi.pojo.vo.HomeworkSubmissionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 作业提交Mapper接口
 */
@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmission> {
    
    /**
     * 分页查询作业提交列表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<HomeworkSubmissionVo> selectHomeworkSubmissionPage(Page<HomeworkSubmissionVo> page, @Param("q") HomeworkSubmissionQuery query);

    /**
     * 教师端分页查询作业提交列表。
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<HomeworkSubmissionVo> selectTeacherHomeworkSubmissionPage(Page<HomeworkSubmissionVo> page, @Param("q") HomeworkSubmissionQuery query);
    
    /**
     * 根据作业ID和学生ID查询提交记录
     * @param homeworkId 作业ID
     * @param studentId 学生ID
     * @return 提交记录
     */
    HomeworkSubmission selectByHomeworkAndStudent(@Param("homeworkId") Long homeworkId, @Param("studentId") Long studentId);
    
    /**
     * 根据ID查询提交详情
     * @param submissionId 提交ID
     * @return 提交详情
     */
    HomeworkSubmissionVo selectSubmissionDetail(@Param("submissionId") Long submissionId);

    /**
     * 根据提交ID查询附件信息
     * @param submissionId 提交ID
     * @return 附件信息
     */
    HomeworkSubmissionVo selectSubmissionAttachment(@Param("submissionId") Long submissionId);

    /**
     * 根据作业ID查询可下载附件列表
     * @param homeworkId 作业ID
     * @return 附件列表
     */
    List<HomeworkSubmissionVo> selectHomeworkAttachments(@Param("homeworkId") Long homeworkId);

    /**
     * 教师端根据提交ID查询提交详情（校验教师归属）。
     * @param submissionId 提交ID
     * @param teacherId 教师ID
     * @return 提交详情
     */
    HomeworkSubmissionVo selectTeacherSubmissionDetail(@Param("submissionId") Long submissionId,
                                                       @Param("teacherId") Long teacherId);

    /**
     * 查询学生在课程下的作业完成进度（包含未提交作业）
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 作业进度列表
     */
    List<StudentCourseDetailVo.HomeworkProgressVo> selectHomeworkProgressByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /**
     * 教师端查询学生在课程下的作业提交情况
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 作业提交情况列表
     */
    List<TeacherStudentDetailVo.HomeworkSubmissionInfo> selectTeacherStudentHomeworkSubmissions(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId);

    /**
     * 关闭作业时标记迟交记录。
     * @param homeworkId 作业ID
     * @return 影响行数
     */
    int markLateSubmissionsByHomeworkId(@Param("homeworkId") Long homeworkId);

    /**
     * 教师端按课程统计作业完成情况。
     * @param courseId 课程ID
     * @return 作业完成情况统计
     */
    List<Map<String, Object>> selectCourseHomeworkCompletionAnalysis(@Param("courseId") Long courseId);

    /**
     * 教师端按课程统计作业提交时间分布（按小时）。
     * @param courseId 课程ID
     * @return 提交时间分布
     */
    List<Map<String, Object>> selectCourseHomeworkSubmissionTimeDistribution(@Param("courseId") Long courseId);

    /**
     * 统计作业提交概览
     * @param homeworkId 作业ID
     * @return 统计结果
     */
    Map<String, Object> selectHomeworkSubmissionStatistics(@Param("homeworkId") Long homeworkId);

    /**
     * 统计作业提交分数分布
     * @param homeworkId 作业ID
     * @return 分数分布
     */
    List<Map<String, Object>> selectHomeworkScoreDistribution(@Param("homeworkId") Long homeworkId);
}
