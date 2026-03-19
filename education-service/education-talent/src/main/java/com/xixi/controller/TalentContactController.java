package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.talent.TalentContactCreateDto;
import com.xixi.pojo.dto.talent.TalentContactPageQueryDto;
import com.xixi.pojo.dto.talent.TalentContactUpdateDto;
import com.xixi.pojo.vo.talent.TalentContactPageVo;
import com.xixi.service.TalentContactService;
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

/**
 * 联系人管理控制器。
 */
@RestController
@RequestMapping("/talent/contact")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ENTERPRISE})
public class TalentContactController {
    private final TalentContactService talentContactService;

    @MethodPurpose("联系人分页查询")
    @GetMapping("/page")
    public Result page(
            TalentContactPageQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<TalentContactPageVo> page = talentContactService.page(query, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("创建联系人")
    @PostMapping("/create")
    public Result create(
            @RequestBody TalentContactCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long contactId = talentContactService.create(dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("创建成功", contactId);
    }

    @MethodPurpose("更新联系人")
    @PutMapping("/update/{contactId}")
    public Result update(
            @PathVariable Long contactId,
            @RequestBody TalentContactUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentContactService.update(contactId, dto, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("更新成功");
    }

    @MethodPurpose("删除联系人")
    @DeleteMapping("/delete/{contactId}")
    public Result delete(
            @PathVariable Long contactId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        talentContactService.delete(contactId, HeaderParseUtil.parseUserId(userIdHeader));
        return Result.success("删除成功");
    }
}
