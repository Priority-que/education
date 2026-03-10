package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Users;
import com.xixi.exception.BizException;
import com.xixi.mapper.UsersMapper;
import com.xixi.pojo.dto.UserProfileUpdateDto;
import com.xixi.pojo.dto.UsersDto;
import com.xixi.pojo.query.UserQuery;
import com.xixi.pojo.vo.UsersVo;
import com.xixi.service.RedisTokenService;
import com.xixi.service.UsersService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static com.xixi.constant.PageConstant.PAGE_NUM;
import static com.xixi.constant.PageConstant.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,32}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 64;

    private final UsersMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenService redisTokenService;

    @Override
    public IPage<UsersVo> getPage(UserQuery userQuery) {
        IPage<UsersVo> page = new Page<>(
                userQuery.getPageNum() != null ? userQuery.getPageNum() : PAGE_NUM,
                userQuery.getPageSize() != null ? userQuery.getPageSize() : PAGE_SIZE
        );
        return userMapper.selectUserPage(page, userQuery);
    }

    @Override
    public Result addUser(UsersDto usersDTO) {
        try {
            Users users = BeanUtil.toBean(usersDTO, Users.class);
            if (StringUtils.hasText(users.getPassword())) {
                users.setPassword(passwordEncoder.encode(users.getPassword()));
            }
            users.setAvatar(null);
            users.setCreatedTime(LocalDateTime.now());
            users.setUpdatedTime(LocalDateTime.now());
            userMapper.insert(users);
            return Result.success("添加用户成功");
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return Result.error("添加用户失败");
        }
    }

    @Override
    public Result updateUser(UsersDto usersDTO) {
        try {
            Users users = BeanUtil.toBean(usersDTO, Users.class);
            if (StringUtils.hasText(users.getPassword())) {
                users.setPassword(passwordEncoder.encode(users.getPassword()));
            }
            users.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(users);
            return Result.success("修改用户成功");
        } catch (Exception e) {
            log.error("修改用户失败", e);
            return Result.error("修改用户失败");
        }
    }

    @Override
    public Result deleteUser(List<Integer> ids) {
        try {
            userMapper.deleteByIds(ids);
            return Result.success("删除用户成功");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败");
        }
    }

    @Override
    public UsersVo getUserById(Integer id) {
        Users users = userMapper.selectById(id);
        if (users == null) {
            return null;
        }
        return BeanUtil.toBean(users, UsersVo.class);
    }

    @Override
    public UsersVo getMyProfile(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        Users users = userMapper.selectById(userId);
        if (users == null) {
            throw new BizException(404, "用户不存在");
        }
        return BeanUtil.toBean(users, UsersVo.class);
    }

    @Override
    public Result updateMyProfile(Long userId, UserProfileUpdateDto dto) {
        if (userId == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }

        Users users = userMapper.selectById(userId);
        if (users == null) {
            throw new BizException(404, "用户不存在");
        }

        String username = normalizeUsername(dto.getUsername());
        String realName = trimToNull(dto.getRealName());
        String nickname = trimToNull(dto.getNickname());
        String avatar = trimToNull(dto.getAvatar());
        String email = trimToNull(dto.getEmail());
        String phone = normalizePhone(dto.getPhone());
        Boolean gender = dto.getGender();
        LocalDate birthday = dto.getBirthday();
        String currentPassword = trimToNull(dto.getCurrentPassword());
        String nextPassword = resolveNextPassword(dto);
        String confirmPassword = trimToNull(dto.getConfirmPassword());

        boolean changed = false;
        boolean passwordChanged = false;
        if (username != null && !username.equals(users.getUsername())) {
            Users existsByUsername = userMapper.selectByUsername(username);
            if (existsByUsername != null && !existsByUsername.getId().equals(userId)) {
                throw new BizException(409, "用户名已被占用，请更换后重试");
            }
            users.setUsername(username);
            changed = true;
        }
        if (realName != null && !realName.equals(users.getRealName())) {
            users.setRealName(realName);
            changed = true;
        }
        if (nickname != null && !nickname.equals(users.getNickname())) {
            users.setNickname(nickname);
            changed = true;
        }
        if (avatar != null && !avatar.equals(users.getAvatar())) {
            users.setAvatar(avatar);
            changed = true;
        }
        if (email != null && !email.equals(users.getEmail())) {
            users.setEmail(email);
            changed = true;
        }
        if (phone != null && !phone.equals(users.getPhone())) {
            Users exists = userMapper.selectOne(new LambdaQueryWrapper<Users>()
                    .eq(Users::getPhone, phone)
                    .ne(Users::getId, userId)
                    .last("LIMIT 1"));
            if (exists != null) {
                throw new BizException(409, "手机号已被占用，请更换后重试");
            }
            users.setPhone(phone);
            changed = true;
        }
        if (gender != null && !gender.equals(users.getGender())) {
            users.setGender(gender);
            changed = true;
        }
        if (birthday != null && !birthday.equals(users.getBirthday())) {
            users.setBirthday(birthday);
            changed = true;
        }
        if (nextPassword != null) {
            validatePassword(nextPassword);
            if (confirmPassword != null && !nextPassword.equals(confirmPassword)) {
                throw new BizException(400, "两次输入的新密码不一致");
            }
            if (currentPassword != null && StringUtils.hasText(users.getPassword())
                    && !passwordEncoder.matches(currentPassword, users.getPassword())) {
                throw new BizException(400, "当前密码不正确");
            }
            boolean sameAsOld = StringUtils.hasText(users.getPassword())
                    && passwordEncoder.matches(nextPassword, users.getPassword());
            if (!sameAsOld) {
                users.setPassword(passwordEncoder.encode(nextPassword));
                changed = true;
                passwordChanged = true;
            }
        }

        if (!changed) {
            return Result.success("未检测到资料变更");
        }

        users.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(users);
        if (passwordChanged) {
            redisTokenService.incrTokenVersion(userId);
            redisTokenService.removeAllRefreshTokenForUser(userId);
        }
        return Result.success("资料更新成功", BeanUtil.toBean(users, UsersVo.class));
    }

    private String resolveNextPassword(UserProfileUpdateDto dto) {
        String newPassword = trimToNull(dto.getNewPassword());
        if (newPassword != null) {
            return newPassword;
        }
        return trimToNull(dto.getPassword());
    }

    private String normalizePhone(String phone) {
        String normalized = trimToNull(phone);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replace(" ", "");
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new BizException(400, "手机号格式不正确，请输入11位手机号");
        }
        return normalized;
    }

    private String normalizeUsername(String username) {
        String normalized = trimToNull(username);
        if (normalized == null) {
            return null;
        }
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new BizException(400, "用户名格式不正确，支持4-32位字母、数字或下划线");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new BizException(400, "密码长度需在6到64位之间");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
