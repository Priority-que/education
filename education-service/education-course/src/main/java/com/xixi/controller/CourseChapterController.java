package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseChapterDto;
import com.xixi.pojo.query.CourseChapterQuery;
import com.xixi.pojo.vo.CourseChapterVo;
import com.xixi.service.CourseChapterService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程章节管理接口。
 */
@RestController
@RequestMapping("/courseChapter")
@RequiredArgsConstructor
public class CourseChapterController {

    private final CourseChapterService courseChapterService;

    /**
     * 分页查询章节列表。
     */
    @GetMapping("/getChapterList")
    public Result getChapterList(CourseChapterQuery query) {
        IPage<CourseChapterVo> chapterPage = courseChapterService.getChapterList(query);
        return Result.success(chapterPage);
    }

    /**
     * 根据ID查询章节。
     */
    @GetMapping("/getChapterById/{id}")
    public Result getChapterById(@PathVariable Long id) {
        return Result.success(courseChapterService.getChapterById(id));
    }

    /**
     * 新增章节。
     */
    @PostMapping("/addChapter")
    public Result addChapter(@RequestBody CourseChapterDto dto) {
        return courseChapterService.addChapter(dto);
    }

    /**
     * 更新章节。
     */
    @PutMapping("/updateChapter")
    public Result updateChapter(@RequestBody CourseChapterDto dto) {
        return courseChapterService.updateChapter(dto);
    }

    /**
     * 批量删除章节。
     */
    @DeleteMapping("/deleteChapter")
    public Result deleteChapter(@RequestParam List<Long> ids) {
        return courseChapterService.deleteChapter(ids);
    }
}
