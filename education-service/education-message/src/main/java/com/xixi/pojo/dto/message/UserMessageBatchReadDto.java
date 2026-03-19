package com.xixi.pojo.dto.message;

import lombok.Data;

import java.util.List;

/**
 * 我的消息批量已读请求参数。
 */
@Data
public class UserMessageBatchReadDto {
    private List<Long> messageIds;
}

