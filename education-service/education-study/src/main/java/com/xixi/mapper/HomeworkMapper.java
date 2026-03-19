package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Homework;
import com.xixi.pojo.query.HomeworkQuery;
import com.xixi.pojo.vo.HomeworkVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 作业 Mapper。
 */
@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    /**
     * 分页查询课程作业列表（学生端）。
     */
    Page<HomeworkVo> selectHomeworkPage(Page<HomeworkVo> page, @Param("q") HomeworkQuery query);

    /**
     * 根据作业 ID 查询作业详情。
     */
    HomeworkVo selectHomeworkDetail(@Param("homeworkId") Long homeworkId);

    /**
     * 分页查询作业列表（教师端）。
     */
    Page<HomeworkVo> selectTeacherHomeworkPage(Page<HomeworkVo> page, @Param("q") HomeworkQuery query);

    /**
     * 根据作业 ID 与教师 ID 查询作业。
     */
    Homework selectTeacherHomeworkById(@Param("homeworkId") Long homeworkId, @Param("teacherId") Long teacherId);

    /**
     * 教师删除自己的作业。
     */
    int deleteTeacherHomework(@Param("homeworkId") Long homeworkId, @Param("teacherId") Long teacherId);
}