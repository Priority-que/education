package com.xixi.pojo.query;

import lombok.Data;

@Data
public class EnterprisesQuery {
    private Integer pageNum;
    private Integer pageSize;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 行业
     */
    private String industry;
    /**
     * 联系人
     */
    private String contactPerson;
}
