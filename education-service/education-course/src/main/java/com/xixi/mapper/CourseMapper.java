package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Course;
import com.xixi.pojo.query.CourseQuery;
import com.xixi.pojo.vo.CourseVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import java.math.BigDecimal;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表。
     */
    Page<CourseVo> selectCoursePage(Page<CourseVo> page, @Param("q") CourseQuery query);

    /**
     * 根据ID查询课程详情（包含分类名称）。
     */
    CourseVo getCourseById(Long id);

    /**
     * 课程当前学习人数 +1。
     */
    int addStudentNumber(@Param("id") Long id);

    /**
     * 课程当前学习人数 -1。
     */
    int reduceStudentNumber(@Param("id") Long id);

    int addViewCount(@Param("id") Long id);

    int reduceViewCount(@Param("id") Long id);

    int addLikeCount(@Param("id") Long id);

    int reduceLikeCount(@Param("id") Long id);

    int addCourseRating(@Param("id") Long id, @Param("rating") BigDecimal rating);
}
