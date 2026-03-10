package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 企业看板总览数据。
 */
@Data
public class TalentDashboardOverviewVo {
    private Long enterpriseId;
    private LocalDate statDate;
    private Integer totalFavorites;
    private Integer totalContacts;
    private Integer totalInterviews;
    private Integer totalHires;
    private Integer totalSearches;
}
