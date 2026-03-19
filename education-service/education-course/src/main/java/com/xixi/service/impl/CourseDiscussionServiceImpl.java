package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseDiscussion;
import com.xixi.mapper.CourseChapterMapper;
import com.xixi.mapper.CourseDiscussionMapper;
import com.xixi.mapper.CourseMapper;
import com.xixi.openfeign.user.EducationUserClient;
import com.xixi.pojo.dto.CourseDiscussionDto;
import com.xixi.pojo.query.CourseDiscussionQuery;
import com.xixi.pojo.vo.CourseDiscussionVo;
import com.xixi.service.CourseDiscussionService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程讨论业务实现。
 */
@Service
@RequiredArgsConstructor
public class CourseDiscussionServiceImpl implements CourseDiscussionService {

    private final CourseDiscussionMapper courseDiscussionMapper;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final EducationUserClient educationUserClient;

    /**
     * 根据ID查询课程讨论。
     */
    @Override
    public CourseDiscussionVo getDiscussionById(Long id) {
        return courseDiscussionMapper.getDiscussionById(id);
    }

    /**
     * 分页查询课程讨论。
     */
    @Override
    public IPage<CourseDiscussionVo> getDiscussionList(CourseDiscussionQuery query) {
        Page<CourseDiscussionVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseDiscussionMapper.selectDiscussionPage(page, query);
    }

    /**
     * 新增课程讨论。
     */
    @Override
    public Result addDiscussion(CourseDiscussionDto dto) {
        if (dto == null) {
            return Result.error("讨论参数不能为空");
        }
        if (!courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (dto.getChapterId() != null && !chapterExists(dto.getChapterId())) {
            return Result.error("章节不存在");
        }
        if (!userExists(dto.getUserId())) {
            return Result.error("用户不存在");
        }
        if (dto.getParentId() != null && courseDiscussionMapper.selectById(dto.getParentId()) == null) {
            return Result.error("父讨论不存在");
        }
        try {
            CourseDiscussion entity = BeanUtil.copyProperties(dto, CourseDiscussion.class);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            if (entity.getLikeCount() == null) {
                entity.setLikeCount(0);
            }
            if (entity.getReplyCount() == null) {
                entity.setReplyCount(0);
            }
            courseDiscussionMapper.insert(entity);
            return Result.success("新增讨论成功");
        } catch (Exception e) {
            return Result.error("新增讨论失败");
        }
    }

    /**
     * 更新课程讨论。
     */
    @Override
    public Result updateDiscussion(CourseDiscussionDto dto) {
        if (dto == null || dto.getId() == null) {
            return Result.error("讨论ID不能为空");
        }
        if (courseDiscussionMapper.selectById(dto.getId()) == null) {
            return Result.error("讨论不存在");
        }
        if (dto.getCourseId() != null && !courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (dto.getChapterId() != null && !chapterExists(dto.getChapterId())) {
            return Result.error("章节不存在");
        }
        if (dto.getUserId() != null && !userExists(dto.getUserId())) {
            return Result.error("用户不存在");
        }
        if (dto.getParentId() != null && courseDiscussionMapper.selectById(dto.getParentId()) == null) {
            return Result.error("父讨论不存在");
        }
        try {
            CourseDiscussion entity = BeanUtil.copyProperties(dto, CourseDiscussion.class);
            entity.setUpdatedTime(LocalDateTime.now());
            courseDiscussionMapper.updateById(entity);
            return Result.success("更新讨论成功");
        } catch (Exception e) {
            return Result.error("更新讨论失败");
        }
    }

    /**
     * 批量删除课程讨论。
     */
    @Override
    public Result deleteDiscussion(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的讨论");
        }
        try {
            courseDiscussionMapper.deleteByIds(ids);
            return Result.success("删除讨论成功");
        } catch (Exception e) {
            return Result.error("删除讨论失败");
        }
    }

    /**
     * 校验课程是否存在。
     */
    private boolean courseExists(Long courseId) {
        return courseId != null && courseMapper.selectById(courseId) != null;
    }

    /**
     * 校验章节是否存在。
     */
    private boolean chapterExists(Long chapterId) {
        return chapterId != null && courseChapterMapper.selectById(chapterId) != null;
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
