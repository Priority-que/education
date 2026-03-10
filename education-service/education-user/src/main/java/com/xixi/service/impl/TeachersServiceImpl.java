package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Teachers;
import com.xixi.mapper.TeachersMapper;
import com.xixi.pojo.dto.TeachersDto;
import com.xixi.pojo.query.TeachersQuery;
import com.xixi.pojo.vo.TeachersVo;
import com.xixi.service.TeachersService;
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
public class TeachersServiceImpl implements TeachersService {

    private final TeachersMapper teachersMapper;

    @Override
    public IPage<TeachersVo> getPage(TeachersQuery teachersQuery) {
        IPage<TeachersVo> page = new Page<>(
                teachersQuery.getPageNum() != null ? teachersQuery.getPageNum() : PAGE_NUM,
                teachersQuery.getPageSize() != null ? teachersQuery.getPageSize() : PAGE_SIZE
        );
        return teachersMapper.selectTeacherPage(page, teachersQuery);
    }

    @Override
    public TeachersVo getTeacherById(Long id) {
        return teachersMapper.selectTeacherById(id);
    }

    @Override
    public Result addTeacher(TeachersDto teachersDto) {
        try {
            Teachers teacher = BeanUtil.toBean(teachersDto, Teachers.class);
            teacher.setCreatedTime(LocalDateTime.now());
            teacher.setUpdatedTime(LocalDateTime.now());
            teachersMapper.insert(teacher);
            return Result.success("添加教师成功");
        } catch (Exception e) {
            log.error("添加教师失败", e);
            return Result.error("添加教师失败");
        }
    }

    @Override
    public Result updateTeacher(TeachersDto teachersDto) {
        try {
            Teachers teacher = BeanUtil.toBean(teachersDto, Teachers.class);
            teacher.setUpdatedTime(LocalDateTime.now());
            teachersMapper.updateById(teacher);
            return Result.success("修改教师成功");
        } catch (Exception e) {
            log.error("修改教师失败", e);
            return Result.error("修改教师失败");
        }
    }

    @Override
    public Result deleteTeacher(List<Long> ids) {
        try {
            teachersMapper.deleteByIds(ids);
            return Result.success("删除教师成功");
        } catch (Exception e) {
            log.error("删除教师失败", e);
            return Result.error("删除教师失败");
        }
    }

    @Override
    public Result getTeachersIdByName(String name) {
        Integer teacherId = teachersMapper.getTeachersIdByName(name);
        return Result.success(teacherId);
    }

    @Override
    public Result getTeachersNameById(Long id) {
        String teacherName = teachersMapper.getTeachersNameById(id);
        return Result.success("教师姓名", teacherName);
    }

    @Override
    public Result getTeacherIdByUserId(Long userId) {
        Long teacherId = teachersMapper.getTeacherIdByUserId(userId);
        return Result.success(teacherId);
    }
}
