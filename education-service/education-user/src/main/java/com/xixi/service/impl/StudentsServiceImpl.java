package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Students;
import com.xixi.mapper.StudentsMapper;
import com.xixi.pojo.dto.StudentsDto;
import com.xixi.pojo.query.StudentsQuery;
import com.xixi.pojo.vo.StudentsVo;
import com.xixi.service.StudentsService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.xixi.constant.PageConstant.PAGE_NUM;
import static com.xixi.constant.PageConstant.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentsServiceImpl implements StudentsService {
    
    private final StudentsMapper studentsMapper;
    
    @Override
    public IPage<StudentsVo> getPage(StudentsQuery studentsQuery) {
        IPage<StudentsVo> page = new Page<>(studentsQuery.getPageNum() != null ? studentsQuery.getPageNum() : PAGE_NUM,
                studentsQuery.getPageSize() != null ? studentsQuery.getPageSize() : PAGE_SIZE);
        IPage<StudentsVo> studentPage = studentsMapper.selectStudentPage(page, studentsQuery);
        return studentPage;
    }

    @Override
    public StudentsVo getStudentById(Long id) {
        StudentsVo student = studentsMapper.selectStudentById(id);
        return student;
    }

    @Override
    public StudentsVo getStudentByUserId(Long userId) {
        return studentsMapper.selectStudentByUserId(userId);
    }

    @Override
    public Result addStudent(StudentsDto studentsDto) {
        try {
            // 将DTO转成实体类
            Students student = BeanUtil.copyProperties(studentsDto, Students.class);
            // TODO 通过userId验证用户是否存在，需要调用其他微服务或查询users表
            student.setCreatedTime(LocalDateTime.now());
            student.setUpdatedTime(LocalDateTime.now());
            studentsMapper.insert(student);
            return Result.success("添加学生成功");
        } catch (Exception e) {
            log.error("添加学生失败", e);
            return Result.error("添加学生失败");
        }
    }

    @Override
    public Result updateStudent(StudentsDto studentsDto) {
        try {
            // 将DTO转成实体类
            Students student = BeanUtil.copyProperties(studentsDto, Students.class);
            student.setUpdatedTime(LocalDateTime.now());
            // TODO 通过userId验证用户是否存在，需要调用其他微服务或查询users表
            studentsMapper.updateById(student);
            return Result.success("修改学生成功");
        } catch (Exception e) {
            log.error("修改学生失败", e);
            return Result.error("修改学生失败");
        }
    }

    @Override
    public Result deleteStudent(List<Long> ids) {
        try {
            studentsMapper.deleteByIds(ids);
            return Result.success("删除学生成功");
        } catch (Exception e) {
            log.error("删除学生失败", e);
            return Result.error("删除学生失败");
        }
    }
}





