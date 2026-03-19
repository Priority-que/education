package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.message.UserMessageBatchDeleteDto;
import com.xixi.pojo.dto.message.UserMessageBatchReadDto;
import com.xixi.pojo.query.message.UserMessageQuery;
import com.xixi.pojo.vo.message.UserMessageUnreadCountVo;
import com.xixi.pojo.vo.message.UserMessageVo;
import com.xixi.web.Result;

/**
 * 我的消息中心服务。
 */
public interface UserMessageCenterService {

    IPage<UserMessageVo> getMyMessagePage(Long userId, UserMessageQuery query);

    UserMessageVo getMyMessageDetail(Long userId, Long messageId);

    UserMessageUnreadCountVo getMyUnreadCount(Long userId);

    Result readMessage(Long userId, Long messageId);

    Result readMessageBatch(Long userId, UserMessageBatchReadDto dto);

    Result readAllMessages(Long userId, String messageType);

    Result deleteMessage(Long userId, Long messageId);

    Result deleteMessageBatch(Long userId, UserMessageBatchDeleteDto dto);
}

