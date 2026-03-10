package com.xixi.pojo.dto.message;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TeacherTargetPreviewDto {
    private String targetType;
    private Map<String, Object> targetSpec;
    /**
     * 兼容旧版 payload：targetType=USER + targetValue
     */
    private List<Object> targetValue;
}

