package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseFavorite;
import com.xixi.mapper.CourseFavoriteMapper;
import com.xixi.mapper.CourseMapper;
import com.xixi.openfeign.user.EducationUserClient;
import com.xixi.pojo.dto.CourseFavoriteDto;
import com.xixi.pojo.query.CourseFavoriteQuery;
import com.xixi.pojo.vo.CourseFavoriteVo;
import com.xixi.service.CourseFavoriteService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程收藏业务实现。
 */
@Service
@RequiredArgsConstructor
public class CourseFavoriteServiceImpl implements CourseFavoriteService {

    private final CourseFavoriteMapper courseFavoriteMapper;
    private final CourseMapper courseMapper;
    private final EducationUserClient educationUserClient;

    /**
     * 根据ID查询课程收藏。
     */
    @Override
    public CourseFavoriteVo getFavoriteById(Long id) {
        return courseFavoriteMapper.getFavoriteById(id);
    }

    /**
     * 分页查询课程收藏。
     */
    @Override
    public IPage<CourseFavoriteVo> getFavoriteList(CourseFavoriteQuery query) {
        Page<CourseFavoriteVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseFavoriteMapper.selectFavoritePage(page, query);
    }

    /**
     * 新增课程收藏。
     */
    @Override
    public Result addFavorite(CourseFavoriteDto dto) {
        if (dto == null) {
            return Result.error("收藏参数不能为空");
        }
        if (!courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (!userExists(dto.getUserId())) {
            return Result.error("用户不存在");
        }
        Integer count = courseFavoriteMapper.countByCourseIdAndUserId(dto.getCourseId(), dto.getUserId());
        if (count != null && count > 0) {
            return Result.error("请勿重复收藏");
        }
        try {
            CourseFavorite entity = BeanUtil.copyProperties(dto, CourseFavorite.class);
            entity.setCreatedTime(LocalDateTime.now());
            courseFavoriteMapper.insert(entity);
            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error("收藏失败");
        }
    }

    /**
     * 批量删除课程收藏。
     */
    @Override
    public Result deleteFavorite(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要取消的收藏");
        }
        try {
            courseFavoriteMapper.deleteByIds(ids);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error("取消收藏失败");
        }
    }

    /**
     * 校验课程是否存在。
     */
    private boolean courseExists(Long courseId) {
        return courseId != null && courseMapper.selectById(courseId) != null;
    }

    /**
     * 通过 OpenFeign 校验用户是否存在。
     */
    private boolean userExists(Long userId) {
        if (userId == null) {
            return false;
        }
        Result result = educationUserClient.getUserById(userId);
        return result != null && Integer.valueOf(200).equals(result.getCode()) && result.getData() != null;
    }
}
