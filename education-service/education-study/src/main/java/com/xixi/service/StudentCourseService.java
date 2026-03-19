package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudentCourseJoinDto;
import com.xixi.pojo.query.StudentCourseQuery;
import com.xixi.pojo.query.TeacherStudentCourseQuery;
import com.xixi.pojo.vo.StudentCourseDetailVo;
import com.xixi.pojo.vo.StudentCourseVo;
import com.xixi.pojo.vo.TeacherStudentDetailVo;
import com.xixi.pojo.vo.TeacherStudentCourseVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 学生选课服务接口
 */
public interface StudentCourseService {
    
    /**
     * 加入课程
     * @param dto 选课DTO
     * @return 结果
     */
    Result joinCourse(StudentCourseJoinDto dto);
    
    /**
     * 退出课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 结果
     */
    Result quitCourse(Long studentId, Long courseId);
    
    /**
     * 查看我的课程列表
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<StudentCourseVo> getMyCourses(StudentCourseQuery query);
    
    /**
     * 查看课程学习详情
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 课程学习详情
     */
    StudentCourseDetailVo getCourseDetail(Long studentId, Long courseId);
    
    /**
     * 教师端：查看选课学生名单
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<TeacherStudentCourseVo> getTeacherStudentList(TeacherStudentCourseQuery query);
    
    /**
     * 教师端：查看学生学习详情
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 学生学习详情
     */
    TeacherStudentDetailVo getTeacherStudentDetail(Long courseId, Long studentId);
    
    /**
     * 教师端：导出学生名单
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<TeacherStudentCourseVo> exportStudentList(Long courseId);
}


