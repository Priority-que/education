package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseFavoriteDto;
import com.xixi.pojo.query.CourseFavoriteQuery;
import com.xixi.pojo.vo.CourseFavoriteVo;
import com.xixi.service.CourseFavoriteService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程收藏接口。
 */
@RestController
@RequestMapping("/courseFavorite")
@RequiredArgsConstructor
public class CourseFavoriteController {

    private final CourseFavoriteService courseFavoriteService;

    /**
     * 根据ID查询收藏记录。
     */
    @GetMapping("/getFavoriteById/{id}")
    public Result getFavoriteById(@PathVariable Long id) {
        CourseFavoriteVo vo = courseFavoriteService.getFavoriteById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询收藏列表。
     */
    @GetMapping("/getFavoriteList")
    public Result getFavoriteList(CourseFavoriteQuery query) {
        IPage<CourseFavoriteVo> page = courseFavoriteService.getFavoriteList(query);
        return Result.success(page);
    }

    /**
     * 新增收藏。
     */
    @PostMapping("/addFavorite")
    public Result addFavorite(@RequestBody CourseFavoriteDto dto) {
        return courseFavoriteService.addFavorite(dto);
    }

    /**
     * 批量取消收藏。
     */
    @DeleteMapping("/deleteFavorite")
    public Result deleteFavorite(@RequestParam List<Long> ids) {
        return courseFavoriteService.deleteFavorite(ids);
    }
}
