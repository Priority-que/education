package com.xixi.openfeign.course;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

/**
 * education-course 课程相关远程调用
 */
@FeignClient(name = "education-course", contextId = "educationCourseClient")
public interface EducationCourseClient {

    /**
     * 根据课程ID获取课程详情（包含章节、视频、资料）
     */
    @GetMapping("/course/getCourseDetail/{id}")
    Result getCourseDetail(@PathVariable Long id);
    
    /**
     * 根据课程ID获取课程基本信息
     */
    @GetMapping("/course/getCourseById/{id}")
    Result getCourseById(@PathVariable("id") Long id);
    
    /**
     * 更新课程学生数量
     */
    @PutMapping("/course/updateCourseStudentNumber")
    Result updateCourseCurrentStudentNumber(@RequestParam Long id, @RequestParam Integer status);

    @PutMapping("/course/updateCourseViewCount")
    Result updateCourseViewCount(@RequestParam Long id, @RequestParam Integer status);

    @PutMapping("/course/updateCourseLikeCount")
    Result updateCourseLikeCount(@RequestParam Long id, @RequestParam Integer status);

    @PutMapping("/course/updateCourseRating")
    Result updateCourseRating(@RequestParam Long id, @RequestParam BigDecimal rating);
}

