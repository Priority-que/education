package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudentCourseDto;
import com.xixi.pojo.dto.StudentCourseJoinDto;
import com.xixi.pojo.query.StudentCourseQuery;
import com.xixi.pojo.query.TeacherStudentCourseQuery;
import com.xixi.pojo.vo.StudentCourseDetailVo;
import com.xixi.pojo.vo.StudentCourseVo;
import com.xixi.pojo.vo.TeacherStudentDetailVo;
import com.xixi.pojo.vo.TeacherStudentCourseVo;
import com.xixi.service.StudentCourseService;
import com.xixi.support.CurrentStudentResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生选课控制器
 */
@RestController
@RequestMapping("/study/studentCourse")
@RequiredArgsConstructor
public class StudentCourseController {
    
    private final StudentCourseService studentCourseService;
    private final CurrentStudentResolver currentStudentResolver;
    
    /**
     * 加入课程
     * @param dto 选课DTO
     * @return 结果
     */
    @PostMapping("/join")
    public Result joinCourse(@RequestBody StudentCourseJoinDto dto) {
        dto.setStudentId(currentStudentResolver.requireCurrentStudentId());
        return studentCourseService.joinCourse(dto);
    }
    
    /**
     * 退出课程
     * @return 结果
     */
    @DeleteMapping("/quit")
    public Result quitCourse(@RequestParam Long courseId) {
        return studentCourseService.quitCourse(currentStudentResolver.requireCurrentStudentId(), courseId);
    }
    
    /**
     * 查看我的课程列表
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/myCourses")
    public Result getMyCourses(StudentCourseQuery query) {
        query.setStudentId(currentStudentResolver.requireCurrentStudentId());
        IPage<StudentCourseVo> page = studentCourseService.getMyCourses(query);
        return Result.success(page);
    }
    
    /**
     * 查看课程学习详情
     * @return 课程学习详情
     */
    @GetMapping("/detail")
    public Result getCourseDetail(StudentCourseDto dto) {
        StudentCourseDetailVo detail = studentCourseService.getCourseDetail(
                currentStudentResolver.requireCurrentStudentId(), dto.getCourseId());
        return Result.success(detail);
    }
    
    /**
     * 教师端：查看选课学生名单
     * @param courseId 课程ID
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/teacher/{courseId}/students")
    public Result getTeacherStudentList(@PathVariable Long courseId, TeacherStudentCourseQuery query) {
        query.setCourseId(courseId);
        IPage<TeacherStudentCourseVo> page = studentCourseService.getTeacherStudentList(query);
        return Result.success(page);
    }
    
    /**
     * 教师端：查看学生学习详情
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 学生学习详情
     */
    @GetMapping("/teacher/studentDetail")
    public Result getTeacherStudentDetail(@RequestParam Long courseId, @RequestParam Long studentId) {
        TeacherStudentDetailVo detail = studentCourseService.getTeacherStudentDetail(courseId, studentId);
        return Result.success(detail);
    }
    
    /**
     * 教师端：导出学生名单
     * @param courseId 课程ID
     * @return 学生列表
     */
    @GetMapping("/export/{courseId}")
    public Result exportStudentList(@PathVariable Long courseId) {
        List<TeacherStudentCourseVo> studentList = studentCourseService.exportStudentList(courseId);
        return Result.success(studentList);
    }
}
