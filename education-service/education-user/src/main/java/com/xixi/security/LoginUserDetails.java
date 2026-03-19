package com.xixi.security;

import com.xixi.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LoginUserDetails implements UserDetails {
    private final Users user;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(Users user) {
        this.user = user;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(resolveRole(user.getRole())));
    }

    public Users getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getStatus());
    }

    private String resolveRole(Integer role) {
        if (role == null) {
            return "ROLE_USER";
        }
        return switch (role) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_STUDENT";
            case 3 -> "ROLE_TEACHER";
            case 4 -> "ROLE_ENTERPRISE";
            default -> "ROLE_USER";
        };
    }
}

