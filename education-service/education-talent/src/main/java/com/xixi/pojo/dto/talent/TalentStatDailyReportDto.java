package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 日统计重建参数。
 */
@Data
public class TalentStatDailyReportDto {
    private Long enterpriseId;
    private LocalDate statDate;
}
