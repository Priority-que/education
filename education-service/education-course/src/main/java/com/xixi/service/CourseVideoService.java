package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseVideoDto;
import com.xixi.pojo.query.CourseVideoQuery;
import com.xixi.pojo.vo.CourseVideoVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程视频业务接口
 */
public interface CourseVideoService {

    /**
     * 根据ID查询视频
     */
    CourseVideoVo getVideoById(Long id);

    /**
     * 分页查询视频列表
     */
    IPage<CourseVideoVo> getVideoList(CourseVideoQuery query);

    /**
     * 新增视频
     */
    Result addVideo(CourseVideoDto dto);

    /**
     * 更新视频
     */
    Result updateVideo(CourseVideoDto dto);

    /**
     * 批量删除视频
     */
    Result deleteVideo(List<Long> ids);
}
