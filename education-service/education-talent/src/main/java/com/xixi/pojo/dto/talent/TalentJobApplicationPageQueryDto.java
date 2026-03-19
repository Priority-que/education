package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 企业端投递分页参数。
 */
@Data
public class TalentJobApplicationPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long jobId;
    private String status;
    private String keyword;
}
