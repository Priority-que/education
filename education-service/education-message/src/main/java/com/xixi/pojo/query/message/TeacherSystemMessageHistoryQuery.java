package com.xixi.pojo.query.message;

import lombok.Data;

@Data
public class TeacherSystemMessageHistoryQuery {
    private Long pageNum = 1L;
    private Long pageSize = 20L;
    private String status;
    private String messageType;
    private String keyword;
}

