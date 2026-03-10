package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.admin.AdminUserRoleUpdateDto;
import com.xixi.pojo.dto.admin.AdminUserStatusUpdateDto;
import com.xixi.pojo.query.admin.AdminUserPageQuery;
import com.xixi.pojo.vo.admin.AdminUserDetailVo;
import com.xixi.pojo.vo.admin.AdminUserPageVo;
import com.xixi.web.Result;

/**
 * 用户管理编排服务接口。
 */
public interface AdminUserService {
    IPage<AdminUserPageVo> getUserPage(AdminUserPageQuery query);

    AdminUserDetailVo getUserDetail(Long userId);

    Result updateUserRole(Long userId, AdminUserRoleUpdateDto dto, Long adminId);

    Result updateUserStatus(Long userId, AdminUserStatusUpdateDto dto, Long adminId);
}
