package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.StudentCourse;
import com.xixi.pojo.query.StudentCourseQuery;
import com.xixi.pojo.query.TeacherStudentCourseQuery;
import com.xixi.pojo.vo.StudentCourseVo;
import com.xixi.pojo.vo.TeacherStudentCourseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生选课Mapper接口
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {
    
    /**
     * 分页查询学生课程列表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<StudentCourseVo> selectMyCoursesPage(Page<StudentCourseVo> page, @Param("q") StudentCourseQuery query);
    
    /**
     * 根据学生ID和课程ID查询选课记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录
     */
    StudentCourse selectByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据学生ID和课程ID查询任意状态选课记录（含已退课）。
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录
     */
    StudentCourse selectAnyByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询课程学习详情
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 课程学习详情
     */
    StudentCourseVo selectCourseDetail(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 教师端：分页查询选课学生名单
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    Page<TeacherStudentCourseVo> selectTeacherStudentListPage(Page<TeacherStudentCourseVo> page, @Param("q") TeacherStudentCourseQuery query);
    
    /**
     * 教师端：查询学生学习详情
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 学生学习详情
     */
    TeacherStudentCourseVo selectTeacherStudentDetail(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    /**
     * 教师端：查询课程所有学生（用于导出）
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<TeacherStudentCourseVo> selectAllStudentsByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据学生ID查询选课记录（用于学习概览统计）。
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<StudentCourse> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 教师端监控：查询课程下学生选课记录。
     * @param courseId 课程ID
     * @param studentId 学生ID（可选）
     * @return 课程学生选课记录
     */
    List<TeacherStudentCourseVo> selectTeacherMonitorStudentCourses(@Param("courseId") Long courseId,
                                                                    @Param("studentId") Long studentId);
}
