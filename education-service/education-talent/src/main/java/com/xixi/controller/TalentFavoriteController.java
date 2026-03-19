package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.TalentFavorite;
import com.xixi.entity.TalentTag;
import com.xixi.pojo.dto.talent.TalentFavoriteCreateDto;
import com.xixi.pojo.dto.talent.TalentFavoritePageQueryDto;
import com.xixi.pojo.dto.talent.TalentFavoriteStatusUpdateDto;
import com.xixi.pojo.dto.talent.TalentFavoriteUpdateDto;
import com.xixi.pojo.dto.talent.TalentTagCreateDto;
import com.xixi.pojo.dto.talent.TalentTagUpdateDto;
import com.xixi.service.TalentFavoriteService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人才收藏与标签管理控制器。
 */
@RestController
@RequestMapping("/talent")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ENTERPRISE})
public class TalentFavoriteController {
    private final TalentFavoriteService talentFavoriteService;

    @MethodPurpose("收藏候选人：创建企业收藏记录")
    @PostMapping("/favorite/create")
    public Result createFavorite(
            @RequestBody TalentFavoriteCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long id = talentFavoriteService.createFavorite(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("收藏成功", id);
    }

    @MethodPurpose("收藏列表分页：按状态/标签/关键词查询收藏")
    @GetMapping("/favorite/page")
    public Result pageFavorites(
            TalentFavoritePageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<TalentFavorite> page = talentFavoriteService.pageFavorites(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("收藏详情：查询单条收藏记录")
    @GetMapping("/favorite/detail/{favoriteId}")
    public Result getFavoriteDetail(
            @PathVariable Long favoriteId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        TalentFavorite detail = talentFavoriteService.getFavoriteDetail(favoriteId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(detail);
    }

    @MethodPurpose("更新收藏：修改标签、评分和备注")
    @PutMapping("/favorite/update/{favoriteId}")
    public Result updateFavorite(
            @PathVariable Long favoriteId,
            @RequestBody TalentFavoriteUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentFavoriteService.updateFavorite(favoriteId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("更新成功");
    }

    @MethodPurpose("更新收藏状态：执行候选人状态流转")
    @PutMapping("/favorite/status/{favoriteId}")
    public Result updateFavoriteStatus(
            @PathVariable Long favoriteId,
            @RequestBody TalentFavoriteStatusUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentFavoriteService.updateFavoriteStatus(favoriteId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("状态更新成功");
    }

    @MethodPurpose("取消收藏：删除收藏记录")
    @DeleteMapping("/favorite/delete/{favoriteId}")
    public Result deleteFavorite(
            @PathVariable Long favoriteId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentFavoriteService.deleteFavorite(favoriteId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("删除成功");
    }

    @MethodPurpose("标签列表：查询企业自定义人才标签")
    @GetMapping("/tag/list")
    public Result listTags(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        List<TalentTag> list = talentFavoriteService.listTags(HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(list);
    }

    @MethodPurpose("创建标签：新增企业自定义标签")
    @PostMapping("/tag/create")
    public Result createTag(
            @RequestBody TalentTagCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long id = talentFavoriteService.createTag(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("创建成功", id);
    }

    @MethodPurpose("更新标签：修改标签名称与属性")
    @PutMapping("/tag/update/{tagId}")
    public Result updateTag(
            @PathVariable Long tagId,
            @RequestBody TalentTagUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentFavoriteService.updateTag(tagId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("更新成功");
    }

    @MethodPurpose("删除标签：移除企业标签配置")
    @DeleteMapping("/tag/delete/{tagId}")
    public Result deleteTag(
            @PathVariable Long tagId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentFavoriteService.deleteTag(tagId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("删除成功");
    }
}
