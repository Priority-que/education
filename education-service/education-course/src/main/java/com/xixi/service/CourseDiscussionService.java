package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseDiscussionDto;
import com.xixi.pojo.query.CourseDiscussionQuery;
import com.xixi.pojo.vo.CourseDiscussionVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程讨论业务接口
 */
public interface CourseDiscussionService {

    /**
     * 根据ID查询讨论
     */
    CourseDiscussionVo getDiscussionById(Long id);

    /**
     * 分页查询讨论列表
     */
    IPage<CourseDiscussionVo> getDiscussionList(CourseDiscussionQuery query);

    /**
     * 新增讨论
     */
    Result addDiscussion(CourseDiscussionDto dto);

    /**
     * 更新讨论
     */
    Result updateDiscussion(CourseDiscussionDto dto);

    /**
     * 批量删除讨论
     */
    Result deleteDiscussion(List<Long> ids);
}
