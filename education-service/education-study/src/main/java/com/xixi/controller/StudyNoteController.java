package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudyNoteCommentDto;
import com.xixi.pojo.dto.StudyNoteCreateDto;
import com.xixi.pojo.dto.StudyNoteUpdateDto;
import com.xixi.pojo.query.StudyNoteQuery;
import com.xixi.pojo.vo.StudyNoteVo;
import com.xixi.service.StudyNoteService;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学习笔记控制器
 */
@RestController
@RequestMapping("/study/studyNote")
@RequiredArgsConstructor
public class StudyNoteController {
    
    private final StudyNoteService studyNoteService;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 创建笔记
     * @param dto 创建笔记DTO
     * @return 结果
     */
    @PostMapping("/create")
    public Result createNote(@RequestBody StudyNoteCreateDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return studyNoteService.createNote(dto);
    }
    
    /**
     * 编辑笔记
     * @param dto 更新笔记DTO
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateNote(@RequestBody StudyNoteUpdateDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return studyNoteService.updateNote(dto);
    }
    
    /**
     * 删除笔记
     * @param noteId 笔记ID
     * @param studentId 学生ID
     * @return 结果
     */
    @DeleteMapping("/delete/{noteId}")
    public Result deleteNote(@PathVariable Long noteId) {
        return studyNoteService.deleteNote(noteId, currentStudentResolver.requireCurrentStudentId());
    }
    
    /**
     * 查看我的笔记列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/myNotes")
    public Result getMyNotes(StudyNoteQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<StudyNoteVo> page = studyNoteService.getMyNotes(query);
        return Result.success(page);
    }
    
    /**
     * 查看课程笔记列表（公开笔记）
     * @param courseId 课程ID
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/courseNotes/{courseId}")
    public Result getCourseNotes(@PathVariable Long courseId, StudyNoteQuery query) {
        IPage<StudyNoteVo> page = studyNoteService.getCourseNotes(courseId, query);
        return Result.success(page);
    }
    
    /**
     * 笔记点赞
     * @param noteId 笔记ID
     * @return 结果
     */
    @PostMapping("/like/{noteId}")
    public Result likeNote(@PathVariable Long noteId) {
        return studyNoteService.likeNote(noteId);
    }

    /**
     * 评论学习笔记
     * @param dto 评论参数
     * @return 评论结果
     */
    @PostMapping("/comment")
    public Result commentNote(@RequestBody StudyNoteCommentDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return studyNoteService.commentNote(dto);
    }
}
