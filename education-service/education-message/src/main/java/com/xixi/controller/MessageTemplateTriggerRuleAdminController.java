package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.TemplateTriggerRuleCreateDto;
import com.xixi.pojo.dto.message.TemplateTriggerRuleUpdateDto;
import com.xixi.pojo.query.message.TemplateTriggerRuleQuery;
import com.xixi.pojo.vo.message.TemplateTriggerRuleDetailVo;
import com.xixi.service.TemplateTriggerRuleAdminService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-模板触发规则管理接口。
 */
@RestController
@RequestMapping("/message/admin/template/rule")
@RequiredArgsConstructor
public class MessageTemplateTriggerRuleAdminController {
    private final TemplateTriggerRuleAdminService templateTriggerRuleAdminService;

    @MethodPurpose("管理员创建模板触发规则")
    @RoleRequired({RoleConstants.ADMIN})
    @PostMapping("/create")
    public Result createRule(
            @RequestBody TemplateTriggerRuleCreateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return templateTriggerRuleAdminService.createRule(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员更新模板触发规则")
    @RoleRequired({RoleConstants.ADMIN})
    @PutMapping("/update")
    public Result updateRule(
            @RequestBody TemplateTriggerRuleUpdateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return templateTriggerRuleAdminService.updateRule(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员更新模板触发规则状态")
    @RoleRequired({RoleConstants.ADMIN})
    @PutMapping("/status/{id}")
    public Result updateRuleStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return templateTriggerRuleAdminService.updateRuleStatus(id, status, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员删除模板触发规则")
    @RoleRequired({RoleConstants.ADMIN})
    @DeleteMapping("/delete/{id}")
    public Result deleteRule(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return templateTriggerRuleAdminService.deleteRule(id, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员分页查询模板触发规则")
    @RoleRequired({RoleConstants.ADMIN})
    @GetMapping("/page")
    public Result getRulePage(TemplateTriggerRuleQuery query) {
        IPage<TemplateTriggerRuleDetailVo> page = templateTriggerRuleAdminService.getRulePage(query);
        return Result.success(page);
    }

    @MethodPurpose("解析请求头中的用户ID")
    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

