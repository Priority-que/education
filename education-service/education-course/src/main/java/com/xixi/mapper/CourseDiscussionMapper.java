package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseDiscussion;
import com.xixi.pojo.query.CourseDiscussionQuery;
import com.xixi.pojo.vo.CourseDiscussionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程讨论 Mapper（分页、根据ID查询、批量删除使用 XML，其余使用 MyBatis-Plus 内置方法）
 */
@Mapper
public interface CourseDiscussionMapper extends BaseMapper<CourseDiscussion> {

    /**
     * 分页查询讨论列表（XML 实现）
     */
    Page<CourseDiscussionVo> selectDiscussionPage(Page<CourseDiscussionVo> page, @Param("q") CourseDiscussionQuery query);

    /**
     * 根据ID查询讨论（XML 实现）
     */
    CourseDiscussionVo getDiscussionById(@Param("id") Long id);

    /**
     * 根据ID列表批量删除（XML 实现）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
