package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 联系人分页查询参数。
 */
@Data
public class TalentContactPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String status;
    private Long studentId;
    private Long applicationId;
    private Long jobId;
    private String sourceType;
}
