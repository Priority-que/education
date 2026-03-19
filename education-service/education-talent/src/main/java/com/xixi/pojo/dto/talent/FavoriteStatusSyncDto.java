package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 内部收藏状态同步参数。
 */
@Data
public class FavoriteStatusSyncDto {
    private Long enterpriseId;
    private Long favoriteId;
    private String status;
}
