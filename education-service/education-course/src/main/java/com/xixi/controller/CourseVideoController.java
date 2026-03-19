package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseVideoDto;
import com.xixi.pojo.query.CourseVideoQuery;
import com.xixi.pojo.vo.CourseVideoVo;
import com.xixi.service.CourseVideoService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程视频接口。
 */
@RestController
@RequestMapping("/courseVideo")
@RequiredArgsConstructor
public class CourseVideoController {

    private final CourseVideoService courseVideoService;

    /**
     * 根据ID查询视频。
     */
    @GetMapping("/getVideoById/{id}")
    public Result getVideoById(@PathVariable Long id) {
        CourseVideoVo vo = courseVideoService.getVideoById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询视频列表。
     */
    @GetMapping("/getVideoList")
    public Result getVideoList(CourseVideoQuery query) {
        IPage<CourseVideoVo> page = courseVideoService.getVideoList(query);
        return Result.success(page);
    }

    /**
     * 新增视频。
     */
    @PostMapping("/addVideo")
    public Result addVideo(@RequestBody CourseVideoDto dto) {
        return courseVideoService.addVideo(dto);
    }

    /**
     * 更新视频。
     */
    @PutMapping("/updateVideo")
    public Result updateVideo(@RequestBody CourseVideoDto dto) {
        return courseVideoService.updateVideo(dto);
    }

    /**
     * 批量删除视频。
     */
    @DeleteMapping("/deleteVideo")
    public Result deleteVideo(@RequestParam List<Long> ids) {
        return courseVideoService.deleteVideo(ids);
    }
}
