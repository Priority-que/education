package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.util.List;

/**
 * 创建人才收藏参数。
 */
@Data
public class TalentFavoriteCreateDto {
    private Long resumeId;
    private Long studentId;
    private List<String> tags;
    private Integer rating;
    private String notes;
}
