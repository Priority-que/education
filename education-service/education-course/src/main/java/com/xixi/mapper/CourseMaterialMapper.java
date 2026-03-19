package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseMaterial;
import com.xixi.pojo.query.CourseMaterialQuery;
import com.xixi.pojo.vo.CourseMaterialVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程资料 Mapper（分页、根据ID查询、批量删除使用 XML，其余使用 MyBatis-Plus 内置方法）
 */
@Mapper
public interface CourseMaterialMapper extends BaseMapper<CourseMaterial> {

    /**
     * 分页查询资料列表（XML 实现）
     */
    Page<CourseMaterialVo> selectMaterialPage(Page<CourseMaterialVo> page, @Param("q") CourseMaterialQuery query);

    /**
     * 根据ID查询资料（XML 实现）
     */
    CourseMaterialVo getMaterialById(@Param("id") Long id);

    /**
     * 根据ID列表批量删除（XML 实现）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 根据章节ID列表查询资料列表（有序）
     */
    List<CourseMaterialVo> selectMaterialsByChapterIds(@Param("chapterIds") List<Long> chapterIds);
}
