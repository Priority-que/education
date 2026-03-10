package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Enterprises;
import com.xixi.mapper.EnterprisesMapper;
import com.xixi.pojo.dto.EnterprisesDto;
import com.xixi.pojo.query.EnterprisesQuery;
import com.xixi.pojo.vo.EnterprisesVo;
import com.xixi.service.EnterprisesService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.xixi.constant.PageConstant.PAGE_NUM;
import static com.xixi.constant.PageConstant.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterprisesServiceImpl implements EnterprisesService {
    
    private final EnterprisesMapper enterprisesMapper;
    
    @Override
    public IPage<EnterprisesVo> getPage(EnterprisesQuery enterprisesQuery) {
        IPage<EnterprisesVo> page = new Page<>(enterprisesQuery.getPageNum() != null ? enterprisesQuery.getPageNum() : PAGE_NUM,
                enterprisesQuery.getPageSize() != null ? enterprisesQuery.getPageSize() : PAGE_SIZE);
        IPage<EnterprisesVo> enterprisePage = enterprisesMapper.selectEnterprisePage(page, enterprisesQuery);
        return enterprisePage;
    }

    @Override
    public EnterprisesVo getEnterpriseById(Long id) {
        EnterprisesVo enterprise = enterprisesMapper.selectEnterpriseById(id);
        return enterprise;
    }

    /**
     * 根据用户ID查询企业信息（用于微服务内部身份映射）。
     */
    @Override
    public EnterprisesVo getEnterpriseByUserId(Long userId) {
        return enterprisesMapper.selectEnterpriseByUserId(userId);
    }

    @Override
    public Result addEnterprise(EnterprisesDto enterprisesDto) {
        try {
            // 将DTO转成实体类
            Enterprises enterprise = BeanUtil.toBean(enterprisesDto, Enterprises.class);
            // TODO 通过userId验证用户是否存在，需要调用其他微服务或查询users表
            enterprise.setCreatedTime(LocalDateTime.now());
            enterprise.setUpdatedTime(LocalDateTime.now());
            enterprisesMapper.insert(enterprise);
            return Result.success("添加企业成功");
        } catch (Exception e) {
            log.error("添加企业失败", e);
            return Result.error("添加企业失败");
        }
    }

    @Override
    public Result updateEnterprise(EnterprisesDto enterprisesDto) {
        try {
            // 将DTO转成实体类
            Enterprises enterprise = BeanUtil.toBean(enterprisesDto, Enterprises.class);
            enterprise.setUpdatedTime(LocalDateTime.now());
            // TODO 通过userId验证用户是否存在，需要调用其他微服务或查询users表
            enterprisesMapper.updateById(enterprise);
            return Result.success("修改企业成功");
        } catch (Exception e) {
            log.error("修改企业失败", e);
            return Result.error("修改企业失败");
        }
    }

    @Override
    public Result deleteEnterprise(List<Long> ids) {
        try {
            enterprisesMapper.deleteByIds(ids);
            return Result.success("删除企业成功");
        } catch (Exception e) {
            log.error("删除企业失败", e);
            return Result.error("删除企业失败");
        }
    }
}




