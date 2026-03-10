package com.xixi.pojo.query;

import lombok.Data;

/**
 * 企业认证历史分页查询参数。
 */
@Data
public class EnterpriseVerificationHistoryQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
