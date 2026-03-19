package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CourseChapter;
import com.xixi.entity.CourseMaterial;
import com.xixi.mapper.CourseChapterMapper;
import com.xixi.mapper.CourseMapper;
import com.xixi.mapper.CourseMaterialMapper;
import com.xixi.pojo.dto.CourseMaterialDto;
import com.xixi.pojo.query.CourseMaterialQuery;
import com.xixi.pojo.vo.CourseMaterialVo;
import com.xixi.service.CourseMaterialService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程资料业务实现。
 */
@Service
@RequiredArgsConstructor
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialMapper courseMaterialMapper;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;

    /**
     * 根据ID查询课程资料。
     */
    @Override
    public CourseMaterialVo getMaterialById(Long id) {
        return courseMaterialMapper.getMaterialById(id);
    }

    /**
     * 分页查询课程资料。
     */
    @Override
    public IPage<CourseMaterialVo> getMaterialList(CourseMaterialQuery query) {
        Page<CourseMaterialVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseMaterialMapper.selectMaterialPage(page, query);
    }

    /**
     * 新增课程资料。
     */
    @Override
    public Result addMaterial(CourseMaterialDto dto) {
        if (dto == null) {
            return Result.error("资料参数不能为空");
        }
        if (!courseExists(dto.getCourseId())) {
            return Result.error("课程不存在");
        }
        if (!chapterExists(dto.getChapterId())) {
            return Result.error("章节不存在");
        }
        if (!belongsToCourse(dto.getChapterId(), dto.getCourseId())) {
            return Result.error("章节不属于该课程");
        }
        try {
            CourseMaterial entity = BeanUtil.copyProperties(dto, CourseMaterial.class);
            entity.setCreatedTime(LocalDateTime.now());
            if (entity.getDownloadCount() == null) {
                entity.setDownloadCount(0);
            }
            courseMaterialMapper.insert(entity);
            return Result.success("新增资料成功");
        } catch (Exception e) {
            return Result.error("新增资料失败");
        }
    }

    /**
     * 更新课程资料。
     */
    @Override
    public Result updateMaterial(CourseMaterialDto dto) {
        if (dto == null || dto.getId() == null) {
            return Result.error("资料ID不能为空");
        }
        CourseMaterial material = courseMaterialMapper.selectById(dto.getId());
        if (material == null) {
            return Result.error("资料不存在");
        }
        Long targetCourseId = dto.getCourseId() != null ? dto.getCourseId() : material.getCourseId();
        Long targetChapterId = dto.getChapterId() != null ? dto.getChapterId() : material.getChapterId();
        if (!courseExists(targetCourseId)) {
            return Result.error("课程不存在");
        }
        if (!chapterExists(targetChapterId)) {
            return Result.error("章节不存在");
        }
        if (!belongsToCourse(targetChapterId, targetCourseId)) {
            return Result.error("章节不属于该课程");
        }
        try {
            CourseMaterial entity = BeanUtil.copyProperties(dto, CourseMaterial.class);
            courseMaterialMapper.updateById(entity);
            return Result.success("更新资料成功");
        } catch (Exception e) {
            return Result.error("更新资料失败");
        }
    }

    /**
     * 批量删除课程资料。
     */
    @Override
    public Result deleteMaterial(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的资料");
        }
        try {
            courseMaterialMapper.deleteByIds(ids);
            return Result.success("删除资料成功");
        } catch (Exception e) {
            return Result.error("删除资料失败");
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
     * 校验章节是否属于指定课程。
     */
    private boolean belongsToCourse(Long chapterId, Long courseId) {
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        return chapter != null && chapter.getCourseId() != null && chapter.getCourseId().equals(courseId);
    }
}
