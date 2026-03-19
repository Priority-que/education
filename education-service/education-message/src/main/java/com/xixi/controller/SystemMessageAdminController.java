package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.SystemMessageUpdateDto;
import com.xixi.pojo.query.message.SystemMessageQuery;
import com.xixi.pojo.vo.message.SystemMessageDetailVo;
import com.xixi.service.SystemMessageAdminService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-系统消息管理接口（6.1~6.7）。
 */
@RestController
@RequestMapping("/message/admin/system")
@RequiredArgsConstructor
public class SystemMessageAdminController {
    private final SystemMessageAdminService systemMessageAdminService;

    @MethodPurpose("管理员创建系统消息草稿")
    @RoleRequired({RoleConstants.ADMIN})
    @PostMapping("/create")
    public Result createSystemMessage(
            @RequestBody SystemMessageCreateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return systemMessageAdminService.createSystemMessage(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员修改系统消息草稿")
    @RoleRequired({RoleConstants.ADMIN})
    @PutMapping("/update")
    public Result updateSystemMessage(
            @RequestBody SystemMessageUpdateDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return systemMessageAdminService.updateSystemMessage(request, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员发布系统消息并异步投递用户消息")
    @RoleRequired({RoleConstants.ADMIN})
    @PostMapping("/publish/{id}")
    public Result publishSystemMessage(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return systemMessageAdminService.publishSystemMessage(id, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员撤回已发布系统消息")
    @RoleRequired({RoleConstants.ADMIN})
    @PostMapping("/withdraw/{id}")
    public Result withdrawSystemMessage(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return systemMessageAdminService.withdrawSystemMessage(id, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员删除系统消息")
    @RoleRequired({RoleConstants.ADMIN})
    @DeleteMapping("/delete/{id}")
    public Result deleteSystemMessage(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return systemMessageAdminService.deleteSystemMessage(id, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理员查询系统消息详情")
    @RoleRequired({RoleConstants.ADMIN})
    @GetMapping("/detail/{id}")
    public Result getSystemMessageDetail(@PathVariable Long id) {
        SystemMessageDetailVo vo = systemMessageAdminService.getSystemMessageDetail(id);
        return Result.success(vo);
    }

    @MethodPurpose("管理员分页查询系统消息列表")
    @RoleRequired({RoleConstants.ADMIN})
    @GetMapping("/page")
    public Result getSystemMessagePage(SystemMessageQuery query) {
        IPage<SystemMessageDetailVo> page = systemMessageAdminService.getSystemMessagePage(query);
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
