package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.StudyNote;
import com.xixi.mapper.StudyNoteMapper;
import com.xixi.pojo.dto.StudyNoteCommentDto;
import com.xixi.pojo.dto.StudyNoteCreateDto;
import com.xixi.pojo.dto.StudyNoteUpdateDto;
import com.xixi.pojo.query.StudyNoteQuery;
import com.xixi.pojo.vo.StudyNoteVo;
import com.xixi.service.StudyNoteService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 学习笔记服务实现类
 */
@Service
@RequiredArgsConstructor
public class StudyNoteServiceImpl implements StudyNoteService {

    private final StudyNoteMapper studyNoteMapper;

    /**
     * 创建学习笔记
     * @param dto 创建参数
     * @return 创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createNote(StudyNoteCreateDto dto) {
        try {
            if (dto == null || dto.getStudentId() == null || dto.getCourseId() == null) {
                return Result.error("studentId 和 courseId 不能为空");
            }
            if (isBlank(dto.getNoteContent())) {
                return Result.error("笔记内容不能为空");
            }

            StudyNote studyNote = new StudyNote();
            studyNote.setStudentId(dto.getStudentId());
            studyNote.setCourseId(dto.getCourseId());
            studyNote.setChapterId(dto.getChapterId());
            studyNote.setVideoId(dto.getVideoId());
            studyNote.setNoteTitle(dto.getNoteTitle());
            studyNote.setNoteContent(dto.getNoteContent());
            studyNote.setVideoTimestamp(dto.getVideoTimestamp());
            studyNote.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
            studyNote.setLikeCount(0);
            studyNote.setCommentCount(0);
            studyNoteMapper.insert(studyNote);

            return Result.success("笔记创建成功");
        } catch (Exception e) {
            return Result.error("创建笔记失败：" + e.getMessage());
        }
    }

    /**
     * 修改学习笔记
     * @param dto 修改参数
     * @return 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateNote(StudyNoteUpdateDto dto) {
        try {
            if (dto == null || dto.getNoteId() == null || dto.getStudentId() == null) {
                return Result.error("noteId 和 studentId 不能为空");
            }

            StudyNote studyNote = studyNoteMapper.selectById(dto.getNoteId());
            if (studyNote == null) {
                return Result.error("笔记不存在");
            }
            if (!studyNote.getStudentId().equals(dto.getStudentId())) {
                return Result.error("无权修改此笔记");
            }

            studyNote.setNoteTitle(dto.getNoteTitle());
            studyNote.setNoteContent(dto.getNoteContent());
            if (dto.getIsPublic() != null) {
                studyNote.setIsPublic(dto.getIsPublic());
            }
            studyNote.setUpdatedTime(LocalDateTime.now());
            studyNoteMapper.updateById(studyNote);

            return Result.success("笔记更新成功");
        } catch (Exception e) {
            return Result.error("更新笔记失败：" + e.getMessage());
        }
    }

    /**
     * 删除学习笔记
     * @param noteId 笔记ID
     * @param studentId 学生ID
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteNote(Long noteId, Long studentId) {
        try {
            StudyNote studyNote = studyNoteMapper.selectById(noteId);
            if (studyNote == null) {
                return Result.error("笔记不存在");
            }
            if (!studyNote.getStudentId().equals(studentId)) {
                return Result.error("无权删除此笔记");
            }
            studyNoteMapper.deleteById(noteId);
            return Result.success("笔记删除成功");
        } catch (Exception e) {
            return Result.error("删除笔记失败：" + e.getMessage());
        }
    }

    /**
     * 查询我的笔记列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public IPage<StudyNoteVo> getMyNotes(StudyNoteQuery query) {
        if (query == null) {
            query = new StudyNoteQuery();
        }
        IPage<StudyNoteVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        query.setOnlyPublic(false);
        return studyNoteMapper.selectStudyNotePage((Page<StudyNoteVo>) page, query);
    }

    /**
     * 查询课程公开笔记列表
     * @param courseId 课程ID
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public IPage<StudyNoteVo> getCourseNotes(Long courseId, StudyNoteQuery query) {
        if (query == null) {
            query = new StudyNoteQuery();
        }
        IPage<StudyNoteVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        query.setCourseId(courseId);
        query.setOnlyPublic(true);
        return studyNoteMapper.selectStudyNotePage((Page<StudyNoteVo>) page, query);
    }

    /**
     * 点赞学习笔记
     * @param noteId 笔记ID
     * @return 点赞结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likeNote(Long noteId) {
        try {
            int rows = studyNoteMapper.incrementLikeCount(noteId);
            if (rows > 0) {
                return Result.success("点赞成功");
            }
            return Result.error("笔记不存在");
        } catch (Exception e) {
            return Result.error("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 评论学习笔记（当前版本仅累计评论数）
     * @param dto 评论参数
     * @return 评论结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result commentNote(StudyNoteCommentDto dto) {
        try {
            if (dto == null || dto.getNoteId() == null || dto.getStudentId() == null) {
                return Result.error("noteId 和 studentId 不能为空");
            }
            if (isBlank(dto.getCommentContent())) {
                return Result.error("评论内容不能为空");
            }
            if (dto.getCommentContent().trim().length() > 500) {
                return Result.error("评论内容长度不能超过500字符");
            }

            StudyNote studyNote = studyNoteMapper.selectById(dto.getNoteId());
            if (studyNote == null) {
                return Result.error("笔记不存在");
            }

            if (!Boolean.TRUE.equals(studyNote.getIsPublic()) && !dto.getStudentId().equals(studyNote.getStudentId())) {
                return Result.error("私有笔记不允许他人评论");
            }

            int rows = studyNoteMapper.incrementCommentCount(dto.getNoteId());
            if (rows <= 0) {
                return Result.error("评论失败，笔记不存在或已删除");
            }

            StudyNote latest = studyNoteMapper.selectById(dto.getNoteId());
            return Result.success("评论成功", latest == null ? null : latest.getCommentCount());
        } catch (Exception e) {
            return Result.error("评论失败：" + e.getMessage());
        }
    }

    /**
     * 判断字符串是否为空白
     * @param text 文本
     * @return 是否为空白
     */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
