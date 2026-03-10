package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 日统计重建结果数据。
 */
@Data
public class TalentStatDailyResultVo {
    private LocalDate statDate;
    private Integer enterpriseCount;
    private Integer successCount;
}
