package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.BlockchainRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlockchainRecordMapper extends BaseMapper<BlockchainRecord> {

    /**
     * 按区块高度查询记录。
     */
    BlockchainRecord selectByBlockHeight(@Param("blockHeight") Long blockHeight);

    /**
     * 按证书哈希查询记录。
     */
    BlockchainRecord selectByCertificateHash(@Param("certificateHash") String certificateHash);

    /**
     * 查询最新区块记录。
     */
    BlockchainRecord selectLatestBlock();
}
