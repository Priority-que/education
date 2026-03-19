package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseDiscussionDto;
import com.xixi.pojo.query.CourseDiscussionQuery;
import com.xixi.pojo.vo.CourseDiscussionVo;
import com.xixi.service.CourseDiscussionService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程讨论接口。
 */
@RestController
@RequestMapping("/courseDiscussion")
@RequiredArgsConstructor
public class CourseDiscussionController {

    private final CourseDiscussionService courseDiscussionService;

    /**
     * 根据ID查询讨论。
     */
    @GetMapping("/getDiscussionById/{id}")
    public Result getDiscussionById(@PathVariable Long id) {
        CourseDiscussionVo vo = courseDiscussionService.getDiscussionById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询讨论列表。
     */
    @GetMapping("/getDiscussionList")
    public Result getDiscussionList(CourseDiscussionQuery query) {
        IPage<CourseDiscussionVo> page = courseDiscussionService.getDiscussionList(query);
        return Result.success(page);
    }

    /**
     * 新增讨论。
     */
    @PostMapping("/addDiscussion")
    public Result addDiscussion(@RequestBody CourseDiscussionDto dto) {
        return courseDiscussionService.addDiscussion(dto);
    }

    /**
     * 更新讨论。
     */
    @PutMapping("/updateDiscussion")
    public Result updateDiscussion(@RequestBody CourseDiscussionDto dto) {
        return courseDiscussionService.updateDiscussion(dto);
    }

    /**
     * 批量删除讨论。
     */
    @DeleteMapping("/deleteDiscussion")
    public Result deleteDiscussion(@RequestParam List<Long> ids) {
        return courseDiscussionService.deleteDiscussion(ids);
    }
}
