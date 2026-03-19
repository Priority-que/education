package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史分页参数。
 */
@Data
public class SearchHistoryPageQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
