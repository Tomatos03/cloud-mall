package com.onlineshop.framework.security;

import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.system.role.entity.Role;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

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
        supplementStoreInfoForMerchant(authUser);
        return authUser;
    }

    private void supplementStoreInfoForMerchant(AuthUser authUser) {
        Store store = storeService.queryStoreByUserId(authUser.getUserId());
        if (store == null) {
            return;
        }
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
