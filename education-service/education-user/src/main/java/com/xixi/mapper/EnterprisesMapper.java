package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.Enterprises;
import com.xixi.pojo.query.EnterprisesQuery;
import com.xixi.pojo.vo.EnterprisesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterprisesMapper extends BaseMapper<Enterprises> {
    /**
     * 分页查询企业列表（关联用户表）
     */
    IPage<EnterprisesVo> selectEnterprisePage(IPage<EnterprisesVo> page, @Param("q") EnterprisesQuery enterprisesQuery);

    /**
     * 根据ID查询企业详情（关联用户表）
     */
    EnterprisesVo selectEnterpriseById(@Param("id") Long id);

    /**
     * 根据用户ID查询企业详情（关联用户表）
     */
    EnterprisesVo selectEnterpriseByUserId(@Param("userId") Long userId);
}

















