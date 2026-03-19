package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.BlockchainNode;
import com.xixi.pojo.vo.admin.BlockchainNodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 区块链节点 Mapper。
 */
@Mapper
public interface BlockchainNodeMapper extends BaseMapper<BlockchainNode> {
    IPage<BlockchainNodeVo> selectNodePage(
            Page<BlockchainNodeVo> page,
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    BlockchainNodeVo selectNodeDetail(@Param("nodeId") Long nodeId);
}
