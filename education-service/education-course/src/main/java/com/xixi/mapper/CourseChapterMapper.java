package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseChapter;
import com.xixi.pojo.dto.CourseChapterDto;
import com.xixi.pojo.query.CourseChapterQuery;
import com.xixi.pojo.vo.CourseChapterVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程章节 Mapper。
 */
@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    /**
     * 分页查询章节列表。
     */
    Page<CourseChapterVo> selectChapterPage(Page<CourseChapterVo> page, @Param("q") CourseChapterQuery query);

    /**
     * 根据ID查询章节。
     */
    CourseChapterVo selectChapterById(@Param("id") Long id);

    /**
     * 新增章节。
     */
    int insertChapter(@Param("dto") CourseChapterDto dto);

    /**
     * 更新章节。
     */
    int updateChapter(@Param("dto") CourseChapterDto dto);

    /**
     * 批量删除章节。
     */
    int deleteChapter(@Param("ids") List<Long> ids);

    /**
     * 根据课程ID查询章节列表（按排序字段升序）。
     */
    List<CourseChapterVo> selectChaptersByCourseId(@Param("courseId") Long courseId);
}
