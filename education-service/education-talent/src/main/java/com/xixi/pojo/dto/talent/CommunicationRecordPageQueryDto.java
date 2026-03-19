package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 沟通记录分页参数。
 */
@Data
public class CommunicationRecordPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long applicationId;
    private Long jobId;
    private Long studentId;
    private String communicationType;
    private Boolean isRead;
}
