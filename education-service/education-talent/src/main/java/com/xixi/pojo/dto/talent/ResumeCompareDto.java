package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.util.List;

/**
 * 简历对比请求参数。
 */
@Data
public class ResumeCompareDto {
    private List<Long> studentIds;
}
