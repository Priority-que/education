package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 企业人才摘要数据。
 */
@Data
public class EnterpriseTalentSummaryVo {
    private Long enterpriseId;
    private Integer totalFavorites;
    private Integer totalContacts;
    private Integer totalInterviews;
    private Integer totalHires;
    private Integer totalSearches;
    private LocalDate lastStatDate;
}
