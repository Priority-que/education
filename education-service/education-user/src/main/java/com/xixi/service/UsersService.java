package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.UserProfileUpdateDto;
import com.xixi.pojo.dto.UsersDto;
import com.xixi.pojo.query.UserQuery;
import com.xixi.pojo.vo.UsersVo;
import com.xixi.web.Result;

import java.util.List;

public interface UsersService {
    IPage<UsersVo> getPage(UserQuery userQuery);

    Result addUser(UsersDto usersDTO);

    Result updateUser(UsersDto usersDTO);

    Result deleteUser(List<Integer> ids);

    UsersVo getUserById(Integer id);

    UsersVo getMyProfile(Long userId);

    Result updateMyProfile(Long userId, UserProfileUpdateDto dto);
}
