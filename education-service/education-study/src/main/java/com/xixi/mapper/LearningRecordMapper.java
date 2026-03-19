package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.LearningRecord;
import com.xixi.pojo.query.LearningRecordQuery;
import com.xixi.pojo.vo.LearningRecordVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习记录Mapper接口
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
    
    /**
     * 分页查询学习记录
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<LearningRecordVo> selectLearningRecordPage(Page<LearningRecordVo> page, @Param("q") LearningRecordQuery query);
    
    /**
     * 根据学生ID、课程ID、视频ID查询最新的学习记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param videoId 视频ID
     * @return 学习记录
     */
    LearningRecord selectLatestByStudentAndVideo(@Param("studentId") Long studentId, 
                                                  @Param("courseId") Long courseId, 
                                                  @Param("videoId") Long videoId);

    /**
     * 查询学生在某课程的全部学习记录（按时间倒序）
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习记录列表
     */
    List<LearningRecord> selectByStudentAndCourse(@Param("studentId") Long studentId,
                                                  @Param("courseId") Long courseId);

    /**
     * 查询学生在某课程下每个视频的最新学习记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 最新学习记录列表
     */
    List<LearningRecord> selectLatestByStudentAndCourse(@Param("studentId") Long studentId,
                                                         @Param("courseId") Long courseId);

    /**
     * 教师端查询课程下学生各视频最新学习记录（用于监控计算）。
     * @param courseId 课程ID
     * @param studentId 学生ID（可选）
     * @param chapterId 章节ID（可选）
     * @return 最新学习记录列表
     */
    List<LearningRecord> selectLatestMonitorRecords(@Param("courseId") Long courseId,
                                                    @Param("studentId") Long studentId,
                                                    @Param("chapterId") Long chapterId);

    /**
     * 教师端统计课程下学生学习时长。
     * @param courseId 课程ID
     * @param studentId 学生ID（可选）
     * @param chapterId 章节ID（可选）
     * @return 学生学习时长统计
     */
    List<java.util.Map<String, Object>> selectStudentStudyTimeStats(@Param("courseId") Long courseId,
                                                                     @Param("studentId") Long studentId,
                                                                     @Param("chapterId") Long chapterId);

    /**
     * 教师端统计课程下章节学习时长。
     * @param courseId 课程ID
     * @param studentId 学生ID（可选）
     * @param chapterId 章节ID（可选）
     * @return 章节学习时长统计
     */
    List<java.util.Map<String, Object>> selectChapterStudyTimeStats(@Param("courseId") Long courseId,
                                                                     @Param("studentId") Long studentId,
                                                                     @Param("chapterId") Long chapterId);
}
