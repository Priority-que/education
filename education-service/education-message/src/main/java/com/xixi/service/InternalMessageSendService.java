package com.xixi.service;

import com.xixi.mq.InternalMessageSendCommand;
import com.xixi.pojo.dto.message.InternalSendByTemplateDto;
import com.xixi.pojo.dto.message.InternalSendToRoleDto;
import com.xixi.pojo.dto.message.InternalSendToUserDto;
import com.xixi.pojo.dto.message.InternalSendToUsersDto;
import com.xixi.web.Result;

/**
 * 内部消息投递服务。
 */
public interface InternalMessageSendService {

    Result sendToUser(InternalSendToUserDto dto, Long operatorId, Integer operatorRole);

    Result sendToUsers(InternalSendToUsersDto dto, Long operatorId, Integer operatorRole);

    Result sendToRole(InternalSendToRoleDto dto, Long operatorId, Integer operatorRole);

    Result sendByTemplate(InternalSendByTemplateDto dto, Long operatorId, Integer operatorRole);

    int consumeAsyncCommand(InternalMessageSendCommand command);
}

