package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseVideo;
import com.xixi.pojo.query.CourseVideoQuery;
import com.xixi.pojo.vo.CourseVideoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程视频 Mapper（分页、根据ID查询、批量删除使用 XML，其余使用 MyBatis-Plus 内置方法）
 */
@Mapper
public interface CourseVideoMapper extends BaseMapper<CourseVideo> {

    /**
     * 分页查询视频列表（XML 实现）
     */
    Page<CourseVideoVo> selectVideoPage(Page<CourseVideoVo> page, @Param("q") CourseVideoQuery query);

    /**
     * 根据ID查询视频（XML 实现）
     */
    CourseVideoVo getVideoById(@Param("id") Long id);

    /**
     * 根据ID列表批量删除（XML 实现）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 根据章节ID列表查询视频列表（有序）
     */
    List<CourseVideoVo> selectVideosByChapterIds(@Param("chapterIds") List<Long> chapterIds);
}
