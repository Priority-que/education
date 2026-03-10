package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseCategoryDto;
import com.xixi.pojo.query.CourseCategoryQuery;
import com.xixi.pojo.vo.CourseCategoryVo;
import com.xixi.service.CourseCategoryService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程分类管理接口。
 */
@RestController
@RequestMapping("/courseCategory")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    /**
     * 分页查询分类列表。
     */
    @GetMapping("/getCategoryList")
    public Result list(CourseCategoryQuery query) {
        IPage<CourseCategoryVo> categoryPage = courseCategoryService.getCategoryList(query);
        return Result.success(categoryPage);
    }

    /**
     * 根据ID查询分类。
     */
    @GetMapping("/getCategoryById/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(courseCategoryService.getCategoryById(id));
    }

    /**
     * 新增分类。
     */
    @PostMapping("/add")
    public Result add(@RequestBody CourseCategoryDto categoryDto) {
        return courseCategoryService.addCategory(categoryDto);
    }

    /**
     * 更新分类。
     */
    @PutMapping("/update")
    public Result update(@RequestBody CourseCategoryDto categoryDto) {
        return courseCategoryService.updateCategory(categoryDto);
    }

    /**
     * 批量删除分类。
     */
    @DeleteMapping("/deleteById")
    public Result delete(@RequestParam List<Long> ids) {
        return courseCategoryService.deleteCategory(ids);
    }
}
