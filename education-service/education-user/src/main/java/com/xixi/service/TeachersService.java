package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.TeachersDto;
import com.xixi.pojo.query.TeachersQuery;
import com.xixi.pojo.vo.TeachersVo;
import com.xixi.web.Result;

import java.util.List;

public interface TeachersService {
    /**
     * 分页查询教师列表
     */
    IPage<TeachersVo> getPage(TeachersQuery teachersQuery);

    /**
     * 根据ID查询教师信息
     */
    TeachersVo getTeacherById(Long id);

    /**
     * 添加教师
     */
    Result addTeacher(TeachersDto teachersDto);

    /**
     * 更新教师信息
     */
    Result updateTeacher(TeachersDto teachersDto);

    /**
     * 删除教师
     */
    Result deleteTeacher(List<Long> ids);

    Result getTeachersIdByName(String name);

    Result getTeachersNameById(Long id);

    Result getTeacherIdByUserId(Long userId);
}

















