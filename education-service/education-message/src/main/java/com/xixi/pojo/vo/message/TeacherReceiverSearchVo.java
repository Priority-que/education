package com.xixi.pojo.vo.message;

import lombok.Data;

@Data
public class TeacherReceiverSearchVo {
    private Long userId;
    private String displayName;
    private String subTitle;
    private Boolean selectable;
}

