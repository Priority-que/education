package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseCommentDto;
import com.xixi.pojo.query.CourseCommentQuery;
import com.xixi.pojo.vo.CourseCommentVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程评价业务接口
 */
public interface CourseCommentService {

    /**
     * 根据ID查询评价
     */
    CourseCommentVo getCommentById(Long id);

    /**
     * 分页查询评价列表
     */
    IPage<CourseCommentVo> getCommentList(CourseCommentQuery query);

    /**
     * 新增评价
     */
    Result addComment(CourseCommentDto dto);

    /**
     * 更新评价
     */
    Result updateComment(CourseCommentDto dto);

    /**
     * 批量删除评价
     */
    Result deleteComment(List<Long> ids);
}
