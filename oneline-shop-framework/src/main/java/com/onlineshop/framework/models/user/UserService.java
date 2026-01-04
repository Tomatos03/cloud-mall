package com.onlineshop.framework.models.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.store.StoreService;
import com.onlineshop.framework.models.user.dto.ManagerUserInfoDTO;
import com.onlineshop.framework.models.user.vo.UserInfoVO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StoreService storeService;

    public UserService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public IPage<User> getUsersPage(int current, int size) {
        return this.page(new Page<>(current, size));
    }

    @Override
    public UserInfoVO getUserInfo() {
        User user = getById(UserContextHolder.getUserId());

        return UserInfoVO.builder()
                         .nickname(user.getNickname())
                         .avatarUrl(user.getAvatarUrl())
                         .phone(user.getPhone())
                         .email(user.getEmail())
                         .bio(user.getBio())
                         .uid(user.getId()
                                  .toString())
                         .build();
    }

    @Override
    public ManagerUserInfoDTO getManagerUserInfo() {
        User user = getById(UserContextHolder.getUserId());
        return createManagerUserInfo(user);
    }

    private ManagerUserInfoDTO createManagerUserInfo(User user) {
        ManagerUserInfoDTO managerUserInfoDTO = new ManagerUserInfoDTO();
        managerUserInfoDTO.setNickname(user.getNickname());
        managerUserInfoDTO.setEmail(user.getEmail());
        managerUserInfoDTO.setPhone(user.getPhone());
        managerUserInfoDTO.setUid(user.getId());
        managerUserInfoDTO.setAvatarUrl(user.getAvatarUrl());

        UserRole role = user.getRole();
        managerUserInfoDTO.setRole(role.getCode());
        if (UserRole.MERCHANT == role) {
            Store store = storeService.queryUserStore();
            managerUserInfoDTO.setStoreName(store.getName());
            managerUserInfoDTO.setStoreId(store.getId());
        }
        return managerUserInfoDTO;
    }

    @Override
    public boolean updateUserInfo(UserInfoVO userInfoVO) {
        return updateById(
                User.builder()
                    .id(UserContextHolder.getUserId())
                    .nickname(userInfoVO.getNickname())
                    .avatarUrl(userInfoVO.getAvatarUrl())
                    .phone(userInfoVO.getPhone())
                    .email(userInfoVO.getEmail())
                    .bio(userInfoVO.getBio())
                    .build()
        );
    }
}
