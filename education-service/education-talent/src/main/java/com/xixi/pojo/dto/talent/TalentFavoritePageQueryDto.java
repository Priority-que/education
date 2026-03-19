package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 收藏列表分页参数。
 */
@Data
public class TalentFavoritePageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String status;
    private String tagName;
    private String keyword;
}
