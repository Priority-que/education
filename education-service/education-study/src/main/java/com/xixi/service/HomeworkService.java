package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.HomeworkCreateDto;
import com.xixi.pojo.dto.HomeworkSubmissionGradeDto;
import com.xixi.pojo.dto.HomeworkSubmissionDto;
import com.xixi.pojo.dto.HomeworkUpdateDto;
import com.xixi.pojo.query.HomeworkQuery;
import com.xixi.pojo.query.HomeworkSubmissionQuery;
import com.xixi.pojo.vo.HomeworkSubmissionAttachmentVo;
import com.xixi.pojo.vo.HomeworkSubmissionAnalysisVo;
import com.xixi.pojo.vo.HomeworkSubmissionStatisticsVo;
import com.xixi.pojo.vo.HomeworkVo;
import com.xixi.pojo.vo.HomeworkSubmissionVo;
import com.xixi.web.Result;
import java.util.List;

/**
 * 作业服务接口（学生端） */
public interface HomeworkService {
    
    /**
     * 查看课程作业列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<HomeworkVo> getCourseHomeworkList(HomeworkQuery query);
    
    /**
     * 查看作业详情
     * @param homeworkId 作业ID
     * @return 作业详情
     */
    HomeworkVo getHomeworkDetail(Long homeworkId);
    
    /**
     * 提交作业
     * @param dto 作业提交DTO
     * @return 结果
     */
    Result submitHomework(HomeworkSubmissionDto dto);
    
    /**
     * 修改作业提交
     * @param dto 作业提交DTO
     * @return 结果
     */
    Result updateHomeworkSubmission(HomeworkSubmissionDto dto);
    
    /**
     * 查看我的作业提交列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<HomeworkSubmissionVo> getMySubmissions(HomeworkSubmissionQuery query);

    /**
     * 教师端分页查询作业提交列表。
     * @param query 查询条件
     * @return 提交分页结果
     */
    IPage<HomeworkSubmissionVo> getTeacherSubmissions(HomeworkSubmissionQuery query);
    
    /**
     * 查看作业批改结果
     * @param submissionId 提交ID
     * @return 批改结果
     */
    HomeworkSubmissionVo getSubmissionResult(Long submissionId);

    /**
     * 下载指定作业提交的附件信息
     * @param submissionId 提交ID
     * @return 附件信息
     */
    HomeworkSubmissionAttachmentVo downloadSubmissionAttachment(Long submissionId);

    /**
     * 批量获取某作业下可下载附件列表
     * @param homeworkId 作业ID
     * @return 附件列表
     */
    List<HomeworkSubmissionAttachmentVo> batchDownloadAttachments(Long homeworkId);

    /**
     * 统计某作业的提交情况
     * @param homeworkId 作业ID
     * @return 统计结果
     */
    HomeworkSubmissionStatisticsVo getHomeworkSubmissionStatistics(Long homeworkId);

    /**
     * 教师端批改作业。
     * @param dto 批改参数
     * @return 批改结果
     */
    Result gradeHomeworkSubmission(HomeworkSubmissionGradeDto dto);

    /**
     * 教师端创建作业。
     * @param dto 创建作业参数
     * @return 创建结果
     */
    Result createHomework(HomeworkCreateDto dto);

    /**
     * 教师端编辑作业。
     * @param dto 编辑作业参数
     * @return 编辑结果
     */
    Result updateHomework(HomeworkUpdateDto dto);

    /**
     * 教师端发布作业。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 发布结果
     */
    Result publishHomework(Long homeworkId, Long teacherId);

    /**
     * 教师端关闭作业。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 关闭结果
     */
    Result closeHomework(Long homeworkId, Long teacherId);

    /**
     * 教师端删除作业（仅草稿可删）。
     * @param homeworkId 作业ID
     * @param teacherId 教师ID
     * @return 删除结果
     */
    Result deleteHomework(Long homeworkId, Long teacherId);

    /**
     * 教师端查看作业列表。
     * @param query 查询条件
     * @return 作业分页列表
     */
    IPage<HomeworkVo> getTeacherHomeworkList(HomeworkQuery query);

    /**
     * 教师端分析课程作业完成情况。
     * @param courseId 课程ID
     * @return 作业分析结果
     */
    HomeworkSubmissionAnalysisVo getHomeworkAnalysis(Long courseId);
}
