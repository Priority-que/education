package com.xixi.pojo.vo.message;

import lombok.Data;

/**
 * 我的消息未读统计。
 */
@Data
public class UserMessageUnreadCountVo {
    private Long totalUnread;
    private Long courseUnread;
    private Long certificateUnread;
    private Long jobUnread;
    private Long systemUnread;
}

