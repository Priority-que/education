package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseCategory;
import com.xixi.mapper.CourseCategoryMapper;
import com.xixi.pojo.dto.CourseCategoryDto;
import com.xixi.pojo.query.CourseCategoryQuery;
import com.xixi.pojo.vo.CourseCategoryVo;
import com.xixi.service.CourseCategoryService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    /**
     * 分页查询课程分类。
     */
    @Override
    public IPage<CourseCategoryVo> getCategoryList(CourseCategoryQuery query) {
        Page<CourseCategoryVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseCategoryMapper.selectCategoryPage(page, query);
    }

    /**
     * 根据ID查询课程分类。
     */
    @Override
    public CourseCategoryVo getCategoryById(Long id) {
        return courseCategoryMapper.selectCategoryById(id);
    }

    /**
     * 新增课程分类。
     */
    @Override
    public Result addCategory(CourseCategoryDto categoryDto) {
        CourseCategory courseCategory = BeanUtil.copyProperties(categoryDto, CourseCategory.class);
        courseCategoryMapper.insert(courseCategory);
        return Result.success("新增分类成功");
    }

    /**
     * 更新课程分类。
     */
    @Override
    public Result updateCategory(CourseCategoryDto categoryDto) {
        try{
            CourseCategory courseCategory = BeanUtil.copyProperties(categoryDto, CourseCategory.class);
            courseCategoryMapper.updateById(courseCategory);
            return Result.success("更新分类成功");
        }catch (Exception e){
            return Result.error("更新分类失败");
        }

    }

    /**
     * 批量删除课程分类。
     */
    @Override
    public Result deleteCategory(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的分类");
        }
        courseCategoryMapper.deleteByIds(ids);
        return Result.success("删除分类成功");
    }

    /**
     * 根据分类名称查询分类ID。
     */
    @Override
    public Long getIdByName(String name) {
        return courseCategoryMapper.getIdByName(name);
    }
}
