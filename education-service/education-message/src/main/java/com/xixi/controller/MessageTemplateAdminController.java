package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.MessageTemplateCreateDto;
import com.xixi.pojo.dto.message.MessageTemplateUpdateDto;
import com.xixi.pojo.query.message.MessageTemplateQuery;
import com.xixi.pojo.vo.message.MessageTemplateDetailVo;
import com.xixi.service.MessageTemplateAdminService;
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
 * 管理员-模板管理接口（5.1~5.6）。
 */
@RestController
@RequestMapping("/message/admin/template")
@RequiredArgsConstructor
public class MessageTemplateAdminController {
    private final MessageTemplateAdminService messageTemplateAdminService;

    @MethodPurpose("管理员创建通知模板")
    @RoleRequired({RoleConstants.ADMIN})
    @PostMapping("/create")
    public Result createTemplate(
            @RequestBody MessageTemplateCreateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return messageTemplateAdminService.createTemplate(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员修改通知模板")
    @RoleRequired({RoleConstants.ADMIN})
    @PutMapping("/update")
    public Result updateTemplate(
            @RequestBody MessageTemplateUpdateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return messageTemplateAdminService.updateTemplate(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员启用或禁用模板")
    @RoleRequired({RoleConstants.ADMIN})
    @PutMapping("/status/{id}")
    public Result updateTemplateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return messageTemplateAdminService.updateTemplateStatus(id, status, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员删除模板")
    @RoleRequired({RoleConstants.ADMIN})
    @DeleteMapping("/delete/{id}")
    public Result deleteTemplate(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return messageTemplateAdminService.deleteTemplate(id, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员查询模板详情")
    @RoleRequired({RoleConstants.ADMIN})
    @GetMapping("/detail/{id}")
    public Result getTemplateDetail(@PathVariable Long id) {
        MessageTemplateDetailVo vo = messageTemplateAdminService.getTemplateDetail(id);
        return Result.success(vo);
    }

    @MethodPurpose("管理员分页查询模板列表")
    @RoleRequired({RoleConstants.ADMIN})
    @GetMapping("/page")
    public Result getTemplatePage(MessageTemplateQuery query) {
        IPage<MessageTemplateDetailVo> page = messageTemplateAdminService.getTemplatePage(query);
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
