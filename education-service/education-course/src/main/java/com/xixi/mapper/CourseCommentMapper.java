package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseComment;
import com.xixi.pojo.query.CourseCommentQuery;
import com.xixi.pojo.vo.CourseCommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程评价 Mapper（分页、根据ID查询、批量删除使用 XML，其余可使用 MyBatis-Plus 内置方法）
 */
@Mapper
public interface CourseCommentMapper extends BaseMapper<CourseComment> {

    /**
     * 分页查询评价列表（XML 实现）
     */
    Page<CourseCommentVo> selectCommentPage(Page<CourseCommentVo> page, @Param("q") CourseCommentQuery query);

    /**
     * 根据ID查询评价（XML 实现）
     */
    CourseCommentVo getCommentById(@Param("id") Long id);

    /**
     * 根据ID列表批量删除（XML 实现）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
