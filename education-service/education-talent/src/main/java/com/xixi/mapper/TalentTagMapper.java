package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.TalentTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人才标签数据访问接口。
 */
@Mapper
public interface TalentTagMapper extends BaseMapper<TalentTag> {

    /**
     * 查询企业标签列表。
     */
    List<TalentTag> selectByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 统计企业标签重名数量。
     */
    Long countByEnterpriseAndName(
            @Param("enterpriseId") Long enterpriseId,
            @Param("tagName") String tagName,
            @Param("excludeId") Long excludeId
    );

    /**
     * 更新企业标签信息。
     */
    int updateTagByEnterprise(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("tagName") String tagName,
            @Param("tagColor") String tagColor,
            @Param("description") String description,
            @Param("sortOrder") Integer sortOrder
    );

    /**
     * 删除企业标签。
     */
    int deleteByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);
}
