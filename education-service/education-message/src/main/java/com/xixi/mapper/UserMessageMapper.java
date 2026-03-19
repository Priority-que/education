package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
    int insertBatch(@Param("list") List<UserMessage> list);

    List<Long> selectDeliveredUserIds(
            @Param("relatedId") Long relatedId,
            @Param("relatedType") String relatedType,
            @Param("userIds") List<Long> userIds
    );
}
