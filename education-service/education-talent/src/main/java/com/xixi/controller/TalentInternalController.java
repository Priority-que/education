package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.pojo.dto.talent.FavoriteStatusSyncDto;
import com.xixi.pojo.dto.talent.TalentStatDailyReportDto;
import com.xixi.pojo.vo.talent.EnterpriseTalentSummaryVo;
import com.xixi.pojo.vo.talent.TalentStatDailyResultVo;
import com.xixi.service.TalentInternalService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人才服务内部协同控制器。
 */
@RestController
@RequestMapping("/talent/internal")
@RequiredArgsConstructor
public class TalentInternalController {
    private final TalentInternalService talentInternalService;

    @MethodPurpose("内部日统计：上报并重建企业人才运营日快照")
    @PostMapping("/stat/daily")
    public Result rebuildDailyStat(@RequestBody(required = false) TalentStatDailyReportDto dto) {
        TalentStatDailyResultVo result = talentInternalService.rebuildDailyStat(dto);
        return Result.success(result);
    }

    @MethodPurpose("内部摘要：查询企业人才运营汇总")
    @GetMapping("/enterprise/{enterpriseId}/summary")
    public Result getEnterpriseSummary(@PathVariable Long enterpriseId) {
        EnterpriseTalentSummaryVo summary = talentInternalService.getEnterpriseSummary(enterpriseId);
        return Result.success(summary);
    }

    @MethodPurpose("内部状态同步：同步收藏状态变更")
    @PostMapping("/favorite/status-sync")
    public Result syncFavoriteStatus(@RequestBody FavoriteStatusSyncDto dto) {
        talentInternalService.syncFavoriteStatus(dto);
        return Result.success("同步成功");
    }
}
