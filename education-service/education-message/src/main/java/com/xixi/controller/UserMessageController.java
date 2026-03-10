package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.message.UserMessageBatchDeleteDto;
import com.xixi.pojo.dto.message.UserMessageBatchReadDto;
import com.xixi.pojo.query.message.UserMessageQuery;
import com.xixi.pojo.vo.message.UserMessageUnreadCountVo;
import com.xixi.pojo.vo.message.UserMessageVo;
import com.xixi.service.UserMessageCenterService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的消息中心接口（8.1~8.8）。
 */
@RestController
@RequestMapping("/message/user")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN, RoleConstants.STUDENT, RoleConstants.TEACHER, RoleConstants.ENTERPRISE})
public class UserMessageController {
    private final UserMessageCenterService userMessageCenterService;

    @MethodPurpose("我的消息8.1：分页查询当前登录用户消息")
    @GetMapping("/page")
    public Result getMyMessagePage(
            UserMessageQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<UserMessageVo> page = userMessageCenterService.getMyMessagePage(parseUserId(userIdHeader), query);
        return Result.success(page);
    }

    @MethodPurpose("我的消息8.2：查询当前登录用户消息详情")
    @GetMapping("/detail/{id}")
    public Result getMyMessageDetail(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        UserMessageVo vo = userMessageCenterService.getMyMessageDetail(parseUserId(userIdHeader), id);
        return Result.success(vo);
    }

    @MethodPurpose("我的消息8.3：查询当前登录用户未读消息统计")
    @GetMapping("/unread/count")
    public Result getMyUnreadCount(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        UserMessageUnreadCountVo vo = userMessageCenterService.getMyUnreadCount(parseUserId(userIdHeader));
        return Result.success(vo);
    }

    @MethodPurpose("我的消息8.4：标记单条消息已读")
    @PutMapping("/read/{id}")
    public Result readMessage(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return userMessageCenterService.readMessage(parseUserId(userIdHeader), id);
    }

    @MethodPurpose("我的消息8.5：批量标记消息已读")
    @PutMapping("/read/batch")
    public Result readMessageBatch(
            @RequestBody UserMessageBatchReadDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return userMessageCenterService.readMessageBatch(parseUserId(userIdHeader), request);
    }

    @MethodPurpose("我的消息8.6：标记全部消息已读")
    @PutMapping("/read/all")
    public Result readAllMessages(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestParam(value = "messageType", required = false) String messageType
    ) {
        return userMessageCenterService.readAllMessages(parseUserId(userIdHeader), messageType);
    }

    @MethodPurpose("我的消息8.7：删除单条消息")
    @DeleteMapping("/delete/{id}")
    public Result deleteMessage(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return userMessageCenterService.deleteMessage(parseUserId(userIdHeader), id);
    }

    @MethodPurpose("我的消息8.8：批量删除消息")
    @DeleteMapping("/delete/batch")
    public Result deleteMessageBatch(
            @RequestBody UserMessageBatchDeleteDto request,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return userMessageCenterService.deleteMessageBatch(parseUserId(userIdHeader), request);
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

