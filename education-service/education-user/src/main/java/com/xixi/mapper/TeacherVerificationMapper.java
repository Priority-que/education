package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.TeacherVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeacherVerificationMapper extends BaseMapper<TeacherVerification> {
    TeacherVerification selectCurrentByTeacher(@Param("teacherId") Long teacherId);

    IPage<TeacherVerification> selectHistoryPage(
            Page<TeacherVerification> page,
            @Param("teacherId") Long teacherId
    );
}
