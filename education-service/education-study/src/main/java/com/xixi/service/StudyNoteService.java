package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudyNoteCommentDto;
import com.xixi.pojo.dto.StudyNoteCreateDto;
import com.xixi.pojo.dto.StudyNoteUpdateDto;
import com.xixi.pojo.query.StudyNoteQuery;
import com.xixi.pojo.vo.StudyNoteVo;
import com.xixi.web.Result;

/**
 * 学习笔记服务接口
 */
public interface StudyNoteService {
    
    /**
     * 创建笔记
     * @param dto 创建笔记DTO
     * @return 结果
     */
    Result createNote(StudyNoteCreateDto dto);
    
    /**
     * 编辑笔记
     * @param dto 更新笔记DTO
     * @return 结果
     */
    Result updateNote(StudyNoteUpdateDto dto);
    
    /**
     * 删除笔记
     * @param noteId 笔记ID
     * @param studentId 学生ID（用于权限验证）
     * @return 结果
     */
    Result deleteNote(Long noteId, Long studentId);
    
    /**
     * 查看我的笔记列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<StudyNoteVo> getMyNotes(StudyNoteQuery query);
    
    /**
     * 查看课程笔记列表（包括公开笔记）
     * @param courseId 课程ID
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<StudyNoteVo> getCourseNotes(Long courseId, StudyNoteQuery query);
    
    /**
     * 笔记点赞
     * @param noteId 笔记ID
     * @return 结果
     */
    Result likeNote(Long noteId);

    /**
     * 评论学习笔记
     * @param dto 评论参数
     * @return 评论结果
     */
    Result commentNote(StudyNoteCommentDto dto);
}
