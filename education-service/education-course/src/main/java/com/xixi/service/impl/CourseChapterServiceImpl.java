package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.mapper.CourseChapterMapper;
import com.xixi.pojo.dto.CourseChapterDto;
import com.xixi.pojo.query.CourseChapterQuery;
import com.xixi.pojo.vo.CourseChapterVo;
import com.xixi.service.CourseChapterService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 课程章节业务实现（XML 映射）
 */
@Service
@RequiredArgsConstructor
public class CourseChapterServiceImpl implements CourseChapterService {

    private final CourseChapterMapper courseChapterMapper;

    /**
     * 分页查询课程章节。
     */
    @Override
    public IPage<CourseChapterVo> getChapterList(CourseChapterQuery query) {
        Page<CourseChapterVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseChapterMapper.selectChapterPage(page, query);
    }

    /**
     * 根据ID查询课程章节。
     */
    @Override
    public CourseChapterVo getChapterById(Long id) {
        return courseChapterMapper.selectChapterById(id);
    }

    /**
     * 新增课程章节。
     */
    @Override
    public Result addChapter(CourseChapterDto dto) {
        courseChapterMapper.insertChapter(dto);
        return Result.success("新增章节成功");
    }

    /**
     * 更新课程章节。
     */
    @Override
    public Result updateChapter(CourseChapterDto dto) {
        courseChapterMapper.updateChapter(dto);
        return Result.success("更新章节成功");
    }

    /**
     * 批量删除课程章节。
     */
    @Override
    public Result deleteChapter(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.error("请选择要删除的章节");
        }
        courseChapterMapper.deleteChapter(ids);
        return Result.success("删除章节成功");
    }
}
