package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseVideo;
import com.xixi.mapper.CourseChapterMapper;
import com.xixi.mapper.CourseVideoMapper;
import com.xixi.pojo.dto.CourseVideoDto;
import com.xixi.pojo.query.CourseVideoQuery;
import com.xixi.pojo.vo.CourseVideoVo;
import com.xixi.service.CourseVideoService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程视频业务实现。
 */
@Service
@RequiredArgsConstructor
public class CourseVideoServiceImpl implements CourseVideoService {

    private final CourseVideoMapper courseVideoMapper;
    private final CourseChapterMapper courseChapterMapper;

    /**
     * 根据ID查询课程视频。
     */
    @Override
    public CourseVideoVo getVideoById(Long id) {
        return courseVideoMapper.getVideoById(id);
    }

    /**
     * 分页查询课程视频。
     */
    @Override
    public IPage<CourseVideoVo> getVideoList(CourseVideoQuery query) {
        Page<CourseVideoVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseVideoMapper.selectVideoPage(page, query);
    }

    /**
     * 新增课程视频。
     */
    @Override
    public Result addVideo(CourseVideoDto dto) {
        if (dto == null) {
            return Result.error("视频参数不能为空");
        }
        if (!chapterExists(dto.getChapterId())) {
            return Result.error("章节不存在");
        }
        try {
            CourseVideo entity = BeanUtil.copyProperties(dto, CourseVideo.class);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            if (entity.getViewCount() == null) {
                entity.setViewCount(0);
            }
            courseVideoMapper.insert(entity);
            return Result.success("新增视频成功");
        } catch (Exception e) {
            return Result.error("新增视频失败");
        }
    }

    /**
     * 更新课程视频。
     */
    @Override
    public Result updateVideo(CourseVideoDto dto) {
        if (dto == null || dto.getId() == null) {
            return Result.error("视频ID不能为空");
        }
        if (courseVideoMapper.selectById(dto.getId()) == null) {
            return Result.error("视频不存在");
        }
        if (dto.getChapterId() != null && !chapterExists(dto.getChapterId())) {
            return Result.error("章节不存在");
        }
        try {
            CourseVideo entity = BeanUtil.copyProperties(dto, CourseVideo.class);
            entity.setUpdatedTime(LocalDateTime.now());
            courseVideoMapper.updateById(entity);
            return Result.success("更新视频成功");
        } catch (Exception e) {
            return Result.error("更新视频失败");
        }
    }

    /**
     * 批量删除课程视频。
     */
    @Override
    public Result deleteVideo(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的视频");
        }
        try {
            courseVideoMapper.deleteByIds(ids);
            return Result.success("删除视频成功");
        } catch (Exception e) {
            return Result.error("删除视频失败");
        }
    }

    /**
     * 校验章节是否存在。
     */
    private boolean chapterExists(Long chapterId) {
        return chapterId != null && courseChapterMapper.selectById(chapterId) != null;
    }
}
