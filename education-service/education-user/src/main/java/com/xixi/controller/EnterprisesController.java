package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.EnterprisesDto;
import com.xixi.pojo.query.EnterprisesQuery;
import com.xixi.pojo.vo.EnterprisesVo;
import com.xixi.service.EnterprisesService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enterprises")
@RequiredArgsConstructor
public class EnterprisesController {
    private final EnterprisesService enterprisesService;
    
    @GetMapping("/getPage")
    public Result getPage(EnterprisesQuery enterprisesQuery) {
        IPage<EnterprisesVo> page = enterprisesService.getPage(enterprisesQuery);
        return Result.success(page);
    }
    
    @GetMapping("getEnterpriseById/{id}")
    public Result getEnterpriseById(@PathVariable Long id) {
        EnterprisesVo enterprise = enterprisesService.getEnterpriseById(id);
        return Result.success(enterprise);
    }

    /**
     * 按用户ID查询企业信息（供内部服务调用）。
     */
    @GetMapping("getEnterpriseByUserId/{userId}")
    public Result getEnterpriseByUserId(@PathVariable Long userId) {
        EnterprisesVo enterprise = enterprisesService.getEnterpriseByUserId(userId);
        return Result.success(enterprise);
    }
    
    @PostMapping("/addEnterprise")
    public Result add(EnterprisesDto enterprisesDto) {
        return enterprisesService.addEnterprise(enterprisesDto);
    }
    
    @PostMapping("/updateEnterprise")
    public Result update(EnterprisesDto enterprisesDto) {
        return enterprisesService.updateEnterprise(enterprisesDto);
    }
    
    @PostMapping("/deleteEnterprise")
    public Result delete(@RequestParam List<Long> ids) {
        return enterprisesService.deleteEnterprise(ids);
    }
}
