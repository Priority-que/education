package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.StudentsDto;
import com.xixi.pojo.query.StudentsQuery;
import com.xixi.pojo.vo.StudentsVo;
import com.xixi.web.Result;

import java.util.List;

public interface StudentsService {
    /**
     * 分页查询学生列表
     */
    IPage<StudentsVo> getPage(StudentsQuery studentsQuery);

    /**
     * 根据ID查询学生信息
     */
    StudentsVo getStudentById(Long id);

    /**
     * 根据用户ID查询学生信息
     */
    StudentsVo getStudentByUserId(Long userId);

    /**
     * 添加学生
     */
    Result addStudent(StudentsDto studentsDto);

    /**
     * 更新学生信息
     */
    Result updateStudent(StudentsDto studentsDto);

    /**
     * 删除学生
     */
    Result deleteStudent(List<Long> ids);
}

















