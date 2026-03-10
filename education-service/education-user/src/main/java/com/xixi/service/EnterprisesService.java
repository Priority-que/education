package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.EnterprisesDto;
import com.xixi.pojo.query.EnterprisesQuery;
import com.xixi.pojo.vo.EnterprisesVo;
import com.xixi.web.Result;

import java.util.List;

public interface EnterprisesService {
    /**
     * 分页查询企业列表
     */
    IPage<EnterprisesVo> getPage(EnterprisesQuery enterprisesQuery);

    /**
     * 根据ID查询企业信息
     */
    EnterprisesVo getEnterpriseById(Long id);

    /**
     * 根据用户ID查询企业信息
     */
    EnterprisesVo getEnterpriseByUserId(Long userId);

    /**
     * 添加企业
     */
    Result addEnterprise(EnterprisesDto enterprisesDto);

    /**
     * 更新企业信息
     */
    Result updateEnterprise(EnterprisesDto enterprisesDto);

    /**
     * 删除企业
     */
    Result deleteEnterprise(List<Long> ids);
}

















