package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.CourseAccess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程访问控制 Mapper。
 */
@Mapper
public interface CourseAccessMapper extends BaseMapper<CourseAccess> {
    CourseAccess selectByCourseId(@Param("courseId") Long courseId);

    List<CourseAccess> selectByCourseIds(@Param("courseIds") List<Long> courseIds);
}
