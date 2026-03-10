package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.Teachers;
import com.xixi.pojo.query.TeachersQuery;
import com.xixi.pojo.vo.TeachersVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeachersMapper extends BaseMapper<Teachers> {
    /**
     * 分页查询教师列表（关联用户表）
     */
    IPage<TeachersVo> selectTeacherPage(IPage<TeachersVo> page, @Param("q") TeachersQuery teachersQuery);

    /**
     * 根据ID查询教师详情（关联用户表）
     */
    TeachersVo selectTeacherById(@Param("id") Long id);

    Integer getTeachersIdByName(String name);

    String getTeachersNameById(Long id);

    Long getTeacherIdByUserId(@Param("userId") Long userId);
}





