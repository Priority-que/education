package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.ResumeViewLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResumeViewLogMapper extends BaseMapper<ResumeViewLog> {
    @Select("""
            SELECT viewer_type AS viewerType, COUNT(*) AS totalCount
            FROM resume_view_log
            WHERE resume_id = #{resumeId}
            GROUP BY viewer_type
            """)
    List<Map<String, Object>> countViewerTypeDistribution(@Param("resumeId") Long resumeId);
}
