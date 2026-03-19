package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseMaterialDto;
import com.xixi.pojo.query.CourseMaterialQuery;
import com.xixi.pojo.vo.CourseMaterialVo;
import com.xixi.service.CourseMaterialService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程资料接口。
 */
@RestController
@RequestMapping("/courseMaterial")
@RequiredArgsConstructor
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;

    /**
     * 根据ID查询资料。
     */
    @GetMapping("/getMaterialById/{id}")
    public Result getMaterialById(@PathVariable Long id) {
        CourseMaterialVo vo = courseMaterialService.getMaterialById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询资料列表。
     */
    @GetMapping("/getMaterialList")
    public Result getMaterialList(CourseMaterialQuery query) {
        IPage<CourseMaterialVo> page = courseMaterialService.getMaterialList(query);
        return Result.success(page);
    }

    /**
     * 新增资料。
     */
    @PostMapping("/addMaterial")
    public Result addMaterial(@RequestBody CourseMaterialDto dto) {
        return courseMaterialService.addMaterial(dto);
    }

    /**
     * 更新资料。
     */
    @PutMapping("/updateMaterial")
    public Result updateMaterial(@RequestBody CourseMaterialDto dto) {
        return courseMaterialService.updateMaterial(dto);
    }

    /**
     * 批量删除资料。
     */
    @DeleteMapping("/deleteMaterial")
    public Result deleteMaterial(@RequestParam List<Long> ids) {
        return courseMaterialService.deleteMaterial(ids);
    }
}
