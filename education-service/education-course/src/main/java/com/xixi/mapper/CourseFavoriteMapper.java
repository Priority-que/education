package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseFavorite;
import com.xixi.pojo.query.CourseFavoriteQuery;
import com.xixi.pojo.vo.CourseFavoriteVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程收藏 Mapper（分页、根据ID查询、批量删除使用 XML，其余使用 MyBatis-Plus 内置方法）
 */
@Mapper
public interface CourseFavoriteMapper extends BaseMapper<CourseFavorite> {

    /**
     * 分页查询收藏列表（XML 实现）
     */
    Page<CourseFavoriteVo> selectFavoritePage(Page<CourseFavoriteVo> page, @Param("q") CourseFavoriteQuery query);

    /**
     * 根据ID查询收藏（XML 实现）
     */
    CourseFavoriteVo getFavoriteById(@Param("id") Long id);

    /**
     * 根据ID列表批量删除（XML 实现）
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据课程ID和用户ID统计收藏记录数。
     */
    Integer countByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);
}
