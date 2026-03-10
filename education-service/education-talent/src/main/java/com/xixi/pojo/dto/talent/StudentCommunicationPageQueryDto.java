package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 学生端沟通记录分页参数。
 */
@Data
public class StudentCommunicationPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long applicationId;
    private Boolean isRead;
}
