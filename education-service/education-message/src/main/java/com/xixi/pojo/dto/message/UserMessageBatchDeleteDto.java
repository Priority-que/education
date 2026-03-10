package com.xixi.pojo.dto.message;

import lombok.Data;

import java.util.List;

/**
 * 我的消息批量删除请求参数。
 */
@Data
public class UserMessageBatchDeleteDto {
    private List<Long> messageIds;
}

