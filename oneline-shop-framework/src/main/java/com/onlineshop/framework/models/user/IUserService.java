package com.onlineshop.framework.models.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.user.dto.ManagerUserInfoDTO;
import com.onlineshop.framework.models.user.vo.UserInfoVO;

/**
 * 用户服务接口
 *
 * @author Tomatos
 * @date 2025/12/20
 */
public interface IUserService extends IService<User> {
    IPage<User> getUsersPage(int current, int size);

    UserInfoVO getUserInfo();

    ManagerUserInfoDTO getManagerUserInfo();

    boolean updateUserInfo(UserInfoVO userInfoVO);
}
