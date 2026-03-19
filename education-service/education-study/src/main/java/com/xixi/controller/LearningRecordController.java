package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.LearningRecordSaveDto;
import com.xixi.pojo.query.LearningRecordQuery;
import com.xixi.pojo.vo.LearningRecordVo;
import com.xixi.pojo.query.TeacherLearningMonitorQuery;
import com.xixi.pojo.vo.TeacherLearningMonitorVo;
import com.xixi.pojo.vo.VideoProgressVo;
import com.xixi.service.LearningRecordService;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学习记录控制器
 */
@RestController
@RequestMapping("/study/learningRecord")
@RequiredArgsConstructor
public class LearningRecordController {
    
    private final LearningRecordService learningRecordService;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 保存学习进度
     * @param dto 学习记录DTO
     * @return 结果
     */
    @PostMapping("/save")
    public Result saveLearningRecord(@RequestBody LearningRecordSaveDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return learningRecordService.saveLearningRecord(dto);
    }
    
    /**
     * 获取视频学习进度
     * @param courseId 课程ID
     * @param videoId 视频ID
     * @param studentId 学生ID
     * @return 视频进度
     */
    @GetMapping("/progress/{courseId}/{videoId}")
    public Result getVideoProgress(@PathVariable Long courseId, 
                                   @PathVariable Long videoId) {
        VideoProgressVo vo = learningRecordService.getVideoProgress(
                currentStudentResolver.requireCurrentStudentId(), courseId, videoId);
        return Result.success(vo);
    }
    
    /**
     * 查询学习记录列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result getLearningRecordList(LearningRecordQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<LearningRecordVo> page = learningRecordService.getLearningRecordList(query);
        return Result.success(page);
    }

    /**
     * 教师端查看学生学习进度监控。
     * @param query 查询条件
     * @return 学习监控结果
     */
    @GetMapping("/teacher/monitor")
    public Result getTeacherLearningMonitor(TeacherLearningMonitorQuery query) {
        TeacherLearningMonitorVo vo = learningRecordService.getTeacherLearningMonitor(query);
        return Result.success(vo);
    }
}
