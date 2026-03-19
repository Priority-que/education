package com.xixi.pojo.vo.admin;

import com.xixi.entity.OperationLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户详情视图对象。
 */
@Data
public class AdminUserDetailVo {
    private Long userId;
    private String userName;
    private String userRole;
    private Integer operationCount;
    private LocalDateTime lastOperationTime;
    private Integer status;
    private String roleSource;
    private String statusSource;
    private List<OperationLog> recentLogs;
}
