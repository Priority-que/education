package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseChapterDto;
import com.xixi.pojo.query.CourseChapterQuery;
import com.xixi.pojo.vo.CourseChapterVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 课程章节业务接口
 */
public interface CourseChapterService {

    IPage<CourseChapterVo> getChapterList(CourseChapterQuery query);

    CourseChapterVo getChapterById(Long id);

    Result addChapter(CourseChapterDto dto);

    Result updateChapter(CourseChapterDto dto);

    Result deleteChapter(List<Long> ids);
}
