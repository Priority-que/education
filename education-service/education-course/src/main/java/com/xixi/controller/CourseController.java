package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.pojo.dto.CourseAccessVerifyDto;
import com.xixi.pojo.dto.CourseDto;
import com.xixi.pojo.query.CourseQuery;
import com.xixi.pojo.vo.CourseDetailVo;
import com.xixi.pojo.vo.CourseVo;
import com.xixi.service.CourseService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程管理控制器
 */
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 根据课程ID查询课程基础信息
     */
    @GetMapping("/getCourseById/{id}")
    public Result getCourseById(@PathVariable Long id) {
        CourseVo course = courseService.getCourseById(id);
        return Result.success(course);
    }

    /**
     * 根据课程ID查询课程详情
     */
    @GetMapping("/getCourseDetail/{id}")
    public Result getCourseDetail(@PathVariable Long id) {
        CourseDetailVo courseDetail = courseService.getCourseDetail(id);
        return Result.success(courseDetail);
    }

    /**
     * 分页查询课程列表
     */
    @GetMapping("/getCourseList")
    public Result getCourseList(CourseQuery courseQuery) {
        IPage<CourseVo> courseList = courseService.getCourseList(courseQuery);
        return Result.success(courseList);
    }

    /**
     * 教师端：根据当前用户解析 teacherId 并查询本人课程列表
     */
    @GetMapping("/getTeacherCourseList")
    public Result getTeacherCourseList(
            CourseQuery courseQuery,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<CourseVo> courseList = courseService.getTeacherCourseList(courseQuery, parseUserId(userIdHeader));
        return Result.success(courseList);
    }

    /**
     * 新增课程
     */
    @PostMapping("/addCourse")
    public Result addCourse(
            @RequestBody CourseDto courseDto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return courseService.addCourse(courseDto, parseUserId(userIdHeader));
    }

    /**
     * 更新课程
     */
    @PutMapping("/updateCourse")
    public Result updateCourse(
            @RequestBody CourseDto courseDto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return courseService.updateCourse(courseDto, parseUserId(userIdHeader));
    }

    /**
     * 校验课程访问权限
     */
    @PostMapping("/verifyAccess")
    public Result verifyAccess(@RequestBody CourseAccessVerifyDto dto) {
        return courseService.verifyAccess(dto);
    }

    /**
     * 更新课程当前学习人数，status=1 增加 1，status=0 减少 1
     */
    @PutMapping("/updateCourseStudentNumber")
    public Result updateCourseCurrentStudentNumber(@RequestParam Long id, @RequestParam Integer status) {
        return courseService.updateCourseCurrentStudentNumber(id, status);
    }

    /**
     * 更新课程浏览量，status=1 增加 1，status=0 减少 1
     */
    @PutMapping("/updateCourseViewCount")
    public Result updateCourseViewCount(@RequestParam Long id, @RequestParam Integer status) {
        return courseService.updateCourseViewCount(id, status);
    }

    /**
     * 更新课程点赞量，status=1 增加 1，status=0 减少 1
     */
    @PutMapping("/updateCourseLikeCount")
    public Result updateCourseLikeCount(@RequestParam Long id, @RequestParam Integer status) {
        return courseService.updateCourseLikeCount(id, status);
    }

    /**
     * 更新课程评分
     */
    @PutMapping("/updateCourseRating")
    public Result updateCourseRating(@RequestParam Long id, @RequestParam BigDecimal rating) {
        return courseService.updateCourseRating(id, rating);
    }

    /**
     * 批量删除课程
     */
    @DeleteMapping("/deleteCourse")
    public Result deleteCourse(@RequestParam List<Integer> ids) {
        return courseService.deleteCourse(ids);
    }

    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
