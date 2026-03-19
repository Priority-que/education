package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.CourseMqConsumeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程消息消费日志 Mapper。
 */
@Mapper
public interface CourseMqConsumeLogMapper extends BaseMapper<CourseMqConsumeLog> {

    /**
     * 根据eventId统计消费次数。
     */
    Integer countByEventId(@Param("eventId") String eventId);
}
