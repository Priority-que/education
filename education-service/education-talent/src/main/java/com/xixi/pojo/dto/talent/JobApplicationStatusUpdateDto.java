package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 更新投递状态参数。
 */
@Data
public class JobApplicationStatusUpdateDto {
    private String status;
    private String remark;
}
