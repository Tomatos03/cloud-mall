package com.onlineshop.framework.security;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/2
 */
@Component
@RequiredArgsConstructor
public class UserLoadService implements UserDetailsService {
    private final IUserService userService;
    private final IStoreService storeService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.queryUserByUsername(username);
        List<Role> roles = userService.queryRolesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = convertSimpleGrantedAuthorityList(roles);
        AuthUser authUser = new AuthUser(user.getId(), username, user.getPassword(), authorities);
        authUser.setAccountTypes(parseAccountTypes(user.getTypes()));

        supplementStoreInfoForMerchant(authUser);
        return authUser;
    }

    private Set<String> parseAccountTypes(String types) {
        return new HashSet<>(Arrays.asList(types.split(",")));
    }

    private void supplementStoreInfoForMerchant(AuthUser authUser) {
        if (!authUser.getAccountTypes().contains(AccountType.MERCHANT.getCode())) {
            return;
        }
        Store store = storeService.queryStoreByUserId(authUser.getUserId());
        AssertUtils.notNull(store, BizErrorCode.MERCHANT_STORE_NOT_FOUND);
        authUser.setStoreId(store.getId());
    }

    private List<SimpleGrantedAuthority> convertSimpleGrantedAuthorityList(List<Role> roles) {
        return roles.stream()
                    .map(role ->
                                 new SimpleGrantedAuthority(
                                         role.getName()
                                 )
                    )
                    .toList();
    }
}
