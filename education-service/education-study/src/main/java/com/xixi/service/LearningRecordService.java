package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.LearningRecordSaveDto;
import com.xixi.pojo.query.LearningRecordQuery;
import com.xixi.pojo.query.TeacherLearningMonitorQuery;
import com.xixi.pojo.vo.LearningRecordVo;
import com.xixi.pojo.vo.TeacherLearningMonitorVo;
import com.xixi.pojo.vo.VideoProgressVo;
import com.xixi.web.Result;

/**
 * 学习记录服务接口
 */
public interface LearningRecordService {
    
    /**
     * 保存学习进度
     * @param dto 学习记录DTO
     * @return 结果
     */
    Result saveLearningRecord(LearningRecordSaveDto dto);
    
    /**
     * 获取视频学习进度
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param videoId 视频ID
     * @return 视频进度
     */
    VideoProgressVo getVideoProgress(Long studentId, Long courseId, Long videoId);
    
    /**
     * 查询学习记录列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<LearningRecordVo> getLearningRecordList(LearningRecordQuery query);

    /**
     * 教师端查看学生学习进度监控。
     * @param query 查询条件
     * @return 学习监控结果
     */
    TeacherLearningMonitorVo getTeacherLearningMonitor(TeacherLearningMonitorQuery query);
}
