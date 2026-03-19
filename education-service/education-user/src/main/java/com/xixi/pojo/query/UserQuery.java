package com.xixi.pojo.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQuery {
    private Integer pageNum =1;
    private Integer pageSize =10;
    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Boolean status;
}
