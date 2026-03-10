package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseCategory;
import com.xixi.pojo.query.CourseCategoryQuery;
import com.xixi.pojo.vo.CourseCategoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程分类 Mapper。
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 分页查询课程分类。
     */
    Page<CourseCategoryVo> selectCategoryPage(Page<CourseCategoryVo> page, @Param("q") CourseCategoryQuery query);

    /**
     * 根据ID查询课程分类。
     */
    CourseCategoryVo selectCategoryById(@Param("id") Long id);

    /**
     * 根据分类名称查询分类ID。
     */
    Long getIdByName(@Param("name") String name);
}
