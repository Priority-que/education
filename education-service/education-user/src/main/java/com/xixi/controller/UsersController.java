package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.pojo.dto.UserProfileUpdateDto;
import com.xixi.pojo.dto.UsersDto;
import com.xixi.pojo.query.UserQuery;
import com.xixi.pojo.vo.UsersVo;
import com.xixi.service.UsersService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    @GetMapping("/getPage")
    public Result getPage(UserQuery userQuery) {
        IPage<UsersVo> page = usersService.getPage(userQuery);
        return Result.success(page);
    }

    @GetMapping("getUserById/{id}")
    public Result getUserById(@PathVariable Integer id) {
        UsersVo user = usersService.getUserById(id);
        return Result.success(user);
    }

    @PostMapping("/addUser")
    public Result add(UsersDto usersDTO) {
        return usersService.addUser(usersDTO);
    }

    @PostMapping("/updateUser")
    public Result update(UsersDto usersDTO) {
        return usersService.updateUser(usersDTO);
    }

    @PostMapping("/deleteUser")
    public Result delete(@RequestParam List<Integer> ids) {
        return usersService.deleteUser(ids);
    }

    @GetMapping("/profile/me")
    public Result getMyProfile(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        UsersVo user = usersService.getMyProfile(parseLong(userIdHeader));
        return Result.success(user);
    }

    @PutMapping("/profile/me")
    public Result updateMyProfile(
            @RequestBody(required = false) UserProfileUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return usersService.updateMyProfile(parseLong(userIdHeader), dto);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
