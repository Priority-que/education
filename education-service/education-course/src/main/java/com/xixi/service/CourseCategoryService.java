package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseCategoryDto;
import com.xixi.pojo.query.CourseCategoryQuery;
import com.xixi.pojo.vo.CourseCategoryVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程分类业务接口
 */
public interface CourseCategoryService {

    /**
     * 分页查询分类列表
     */
    IPage<CourseCategoryVo> getCategoryList(CourseCategoryQuery query);

    /**
     * 根据ID查询
     */
    CourseCategoryVo getCategoryById(Long id);

    /**
     * 新增分类
     */
    Result addCategory(CourseCategoryDto categoryDto);

    /**
     * 更新分类
     */
    Result updateCategory(CourseCategoryDto categoryDto);

    /**
     * 批量删除
     */
    Result deleteCategory(List<Long> ids);

    /**
     * 根据名称获取ID
     */
    Long getIdByName(String name);
}
