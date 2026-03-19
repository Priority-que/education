package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.CourseAccessVerifyDto;
import com.xixi.pojo.dto.CourseDto;
import com.xixi.pojo.query.CourseQuery;
import com.xixi.pojo.vo.CourseDetailVo;
import com.xixi.pojo.vo.CourseVo;
import com.xixi.web.Result;

import java.math.BigDecimal;
import java.util.List;

public interface CourseService {
    IPage<CourseVo> getCourseList(CourseQuery courseQuery);

    IPage<CourseVo> getTeacherCourseList(CourseQuery courseQuery, Long operatorUserId);

    Result addCourse(CourseDto courseDto, Long operatorUserId);

    Result updateCourse(CourseDto courseDto, Long operatorUserId);

    Result deleteCourse(List<Integer> ids);

    CourseVo getCourseById(Long id);
    
    /**
     * 获取课程详情（聚合章节、视频、资料）
     */
    CourseDetailVo getCourseDetail(Long id);

    Result updateCourseCurrentStudentNumber(Long id, Integer status);

    Result updateCourseViewCount(Long id, Integer status);

    Result updateCourseLikeCount(Long id, Integer status);

    Result updateCourseRating(Long id, BigDecimal rating);

    Result verifyAccess(CourseAccessVerifyDto dto);
}
