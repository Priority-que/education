package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseComment;
import com.xixi.mapper.CourseCommentMapper;
import com.xixi.mapper.CourseMapper;
import com.xixi.openfeign.user.EducationUserClient;
import com.xixi.pojo.dto.CourseCommentDto;
import com.xixi.pojo.query.CourseCommentQuery;
import com.xixi.pojo.vo.CourseCommentVo;
import com.xixi.service.CourseCommentService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程评论业务实现。
 */
@Service
@RequiredArgsConstructor
public class CourseCommentServiceImpl implements CourseCommentService {

    private final CourseCommentMapper courseCommentMapper;
    private final CourseMapper courseMapper;
    private final EducationUserClient educationUserClient;

    /**
     * 根据ID查询课程评论。
     */
    @Override
    public CourseCommentVo getCommentById(Long id) {
        return courseCommentMapper.getCommentById(id);
    }

    /**
     * 分页查询课程评论。
     */
    @Override
    public IPage<CourseCommentVo> getCommentList(CourseCommentQuery query) {
        Page<CourseCommentVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseCommentMapper.selectCommentPage(page, query);
    }

    /**
     * 新增课程评论。
     */
    @Override
    public Result addComment(CourseCommentDto dto) {
        if (dto == null) {
            return Result.error("评论参数不能为空");
        }
        if (!courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (!userExists(dto.getUserId())) {
            return Result.error("用户不存在");
        }
        try {
            CourseComment entity = BeanUtil.copyProperties(dto, CourseComment.class);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            if (entity.getLikeCount() == null) {
                entity.setLikeCount(0);
            }
            if (entity.getReplyCount() == null) {
                entity.setReplyCount(0);
            }
            courseCommentMapper.insert(entity);
            return Result.success("新增评论成功");
        } catch (Exception e) {
            return Result.error("新增评论失败");
        }
    }

    /**
     * 更新课程评论。
     */
    @Override
    public Result updateComment(CourseCommentDto dto) {
        if (dto == null || dto.getId() == null) {
            return Result.error("评论ID不能为空");
        }
        if (courseCommentMapper.selectById(dto.getId()) == null) {
            return Result.error("评论不存在");
        }
        if (dto.getCourseId() != null && !courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (dto.getUserId() != null && !userExists(dto.getUserId())) {
            return Result.error("用户不存在");
        }
        try {
            CourseComment entity = BeanUtil.copyProperties(dto, CourseComment.class);
            entity.setUpdatedTime(LocalDateTime.now());
            courseCommentMapper.updateById(entity);
            return Result.success("更新评论成功");
        } catch (Exception e) {
            return Result.error("更新评论失败");
        }
    }

    /**
     * 批量删除课程评论。
     */
    @Override
    public Result deleteComment(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的评论");
        }
        try {
            courseCommentMapper.deleteByIds(ids);
            return Result.success("删除评论成功");
        } catch (Exception e) {
            return Result.error("删除评论失败");
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
