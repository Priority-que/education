package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.SearchHistory;
import com.xixi.pojo.dto.talent.ResumeCompareDto;
import com.xixi.pojo.dto.talent.SearchHistoryPageQueryDto;
import com.xixi.pojo.dto.talent.TalentRecommendDto;
import com.xixi.pojo.dto.talent.TalentSearchQueryDto;
import com.xixi.service.TalentSearchService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才搜索与简历发现控制器。
 */
@RestController
@RequestMapping("/talent")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ENTERPRISE})
public class TalentSearchController {
    private final TalentSearchService talentSearchService;

    @MethodPurpose("人才搜索分页：按条件查询公开简历并记录搜索历史")
    @GetMapping("/search/page")
    public Result searchPage(
            TalentSearchQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request
    ) {
        Object data = talentSearchService.searchPage(query, HeaderParseUtil.parseUserId(userIdHeader), resolveClientIp(request));
        return Result.success(data);
    }

    @MethodPurpose("人才地图搜索：按地理范围查询公开简历")
    @GetMapping("/search/map")
    public Result searchMap(
            TalentSearchQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request
    ) {
        Object data = talentSearchService.searchMap(query, HeaderParseUtil.parseUserId(userIdHeader), resolveClientIp(request));
        return Result.success(data);
    }

    @MethodPurpose("智能推荐：按岗位条件返回候选人推荐列表")
    @PostMapping("/search/recommend")
    public Result recommend(
            @RequestBody(required = false) TalentRecommendDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            HttpServletRequest request
    ) {
        Object data = talentSearchService.recommend(dto, HeaderParseUtil.parseUserId(userIdHeader), resolveClientIp(request));
        return Result.success(data);
    }

    @MethodPurpose("搜索历史分页：查询企业人才检索历史记录")
    @GetMapping("/search/history/page")
    public Result searchHistoryPage(
            SearchHistoryPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<SearchHistory> page = talentSearchService.searchHistoryPage(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("清理搜索历史：删除指定时间之前的历史记录")
    @DeleteMapping("/search/history/cleanup")
    public Result cleanupSearchHistory(
            @RequestParam("beforeTime") LocalDateTime beforeTime,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        int affected = talentSearchService.cleanupSearchHistory(beforeTime, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("清理成功", affected);
    }

    @MethodPurpose("公开简历详情：按简历ID查询企业可见详情")
    @GetMapping("/resume/detail/{studentId}")
    public Result getResumeDetail(
            @PathVariable Long studentId,
            @RequestParam(value = "resumeId", required = false) Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Object detail = talentSearchService.getResumeDetail(
                studentId,
                resumeId,
                HeaderParseUtil.parseUserId(userIdHeader)
        );
        return Result.success(detail);
    }

    @MethodPurpose("简历对比：批量对比多份候选人简历")
    @PostMapping("/resume/compare")
    public Result compareResume(
            @RequestBody ResumeCompareDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        List<Object> compareList = talentSearchService.compareResume(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(compareList);
    }

    @MethodPurpose("候选人导出：按筛选条件导出CSV文本内容")
    @GetMapping("/resume/export")
    public Result exportResume(
            TalentSearchQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        String csv = talentSearchService.exportResumeCsv(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(csv);
    }

    @MethodPurpose("解析客户端真实IP地址")
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] values = xForwardedFor.split(",");
            if (values.length > 0 && !values[0].isBlank()) {
                return values[0].trim();
            }
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
