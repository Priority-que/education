package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.StudyNote;
import com.xixi.pojo.query.StudyNoteQuery;
import com.xixi.pojo.vo.StudyNoteVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习笔记Mapper接口
 */
@Mapper
public interface StudyNoteMapper extends BaseMapper<StudyNote> {
    
    /**
     * 分页查询学习笔记
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<StudyNoteVo> selectStudyNotePage(Page<StudyNoteVo> page, @Param("q") StudyNoteQuery query);
    
    /**
     * 增加点赞数
     * @param noteId 笔记ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("noteId") Long noteId);

    /**
     * 增加评论数
     * @param noteId 笔记ID
     * @return 影响行数
     */
    int incrementCommentCount(@Param("noteId") Long noteId);

    /**
     * 查询学生在某课程下的全部笔记（按时间倒序）
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 笔记列表
     */
    List<StudyNote> selectByStudentAndCourse(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId);

    /**
     * 根据学生ID统计笔记总数。
     * @param studentId 学生ID
     * @return 笔记总数
     */
    Long selectCountByStudentId(@Param("studentId") Long studentId);
}
