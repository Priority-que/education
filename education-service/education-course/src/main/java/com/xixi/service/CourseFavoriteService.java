package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseFavoriteDto;
import com.xixi.pojo.query.CourseFavoriteQuery;
import com.xixi.pojo.vo.CourseFavoriteVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程收藏业务接口
 */
public interface CourseFavoriteService {

    /**
     * 根据ID查询收藏
     */
    CourseFavoriteVo getFavoriteById(Long id);

    /**
     * 分页查询收藏列表
     */
    IPage<CourseFavoriteVo> getFavoriteList(CourseFavoriteQuery query);

    /**
     * 新增收藏
     */
    Result addFavorite(CourseFavoriteDto dto);

    /**
     * 批量删除收藏
     */
    Result deleteFavorite(List<Long> ids);
}
