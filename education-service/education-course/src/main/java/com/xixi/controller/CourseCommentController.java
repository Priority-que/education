package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseCommentDto;
import com.xixi.pojo.query.CourseCommentQuery;
import com.xixi.pojo.vo.CourseCommentVo;
import com.xixi.service.CourseCommentService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程评论接口。
 */
@RestController
@RequestMapping("/courseComment")
@RequiredArgsConstructor
public class CourseCommentController {

    private final CourseCommentService courseCommentService;

    /**
     * 根据ID查询评论。
     */
    @GetMapping("/getCommentById/{id}")
    public Result getCommentById(@PathVariable Long id) {
        CourseCommentVo vo = courseCommentService.getCommentById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询评论列表。
     */
    @GetMapping("/getCommentList")
    public Result getCommentList(CourseCommentQuery query) {
        IPage<CourseCommentVo> page = courseCommentService.getCommentList(query);
        return Result.success(page);
    }

    /**
     * 新增评论。
     */
    @PostMapping("/addComment")
    public Result addComment(@RequestBody CourseCommentDto dto) {
        return courseCommentService.addComment(dto);
    }

    /**
     * 更新评论。
     */
    @PutMapping("/updateComment")
    public Result updateComment(@RequestBody CourseCommentDto dto) {
        return courseCommentService.updateComment(dto);
    }

    /**
     * 批量删除评论。
     */
    @DeleteMapping("/deleteComment")
    public Result deleteComment(@RequestParam List<Long> ids) {
        return courseCommentService.deleteComment(ids);
    }
}
