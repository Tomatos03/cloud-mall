package com.cloudmall.framework.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/3
 */
@Data
public class AuthUser extends User {
    private Long userId;
    private Long storeId;
    /**
     * 当前 token 对应的账号类型（单值）
     */
    private String currentAccountType;

    /**
     * 账号可用的全部端类型（多值，来源于用户表 types）
     */
    private Set<String> availableAccountTypes = new HashSet<>();

    public AuthUser(String username, String password,
                    Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public AuthUser(Long userId, String username, String password,
                    Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public List<String> getRoleCodes() {
        return this.getAuthorities()
                   .stream()
                   .map(GrantedAuthority::getAuthority)
                   .toList();
    }
}
