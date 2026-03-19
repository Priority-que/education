package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.Users;
import com.xixi.pojo.query.UserQuery;
import com.xixi.pojo.vo.UsersVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    IPage<UsersVo> selectUserPage(IPage<UsersVo> page, @Param("q") UserQuery userQuery);

    Users selectByUsername(@Param("username") String username);

    Users selectByPhone(@Param("phone") String phone);

    int updateLastLogin(@Param("id") Long id, @Param("ip") String ip, @Param("time") LocalDateTime time);

    Integer selectStatusById(@Param("id") Long id);

    Integer selectRoleById(@Param("id") Long id);
}
