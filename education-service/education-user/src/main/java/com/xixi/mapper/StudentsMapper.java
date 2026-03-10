package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.Students;
import com.xixi.pojo.query.StudentsQuery;
import com.xixi.pojo.vo.StudentsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentsMapper extends BaseMapper<Students> {
    /**
     * 分页查询学生列表（关联用户表）
     */
    IPage<StudentsVo> selectStudentPage(IPage<StudentsVo> page, @Param("q") StudentsQuery studentsQuery);

    /**
     * 根据ID查询学生详情（关联用户表）
     */
    StudentsVo selectStudentById(@Param("id") Long id);

    /**
     * 根据用户ID查询学生详情（关联用户表）
     */
    StudentsVo selectStudentByUserId(@Param("userId") Long userId);
}

















