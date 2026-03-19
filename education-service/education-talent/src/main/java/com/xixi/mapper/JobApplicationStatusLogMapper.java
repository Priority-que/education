package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.JobApplicationStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位投递状态流水 Mapper。
 */
@Mapper
public interface JobApplicationStatusLogMapper extends BaseMapper<JobApplicationStatusLog> {
    List<JobApplicationStatusLog> selectByApplicationId(@Param("applicationId") Long applicationId);
}
