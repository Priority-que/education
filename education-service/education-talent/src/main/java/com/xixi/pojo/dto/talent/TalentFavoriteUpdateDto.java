package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.util.List;

/**
 * 更新人才收藏参数。
 */
@Data
public class TalentFavoriteUpdateDto {
    private List<String> tags;
    private Integer rating;
    private String notes;
}
