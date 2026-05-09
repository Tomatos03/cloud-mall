package com.cloudmall.framework.application.system;

import com.cloudmall.framework.models.system.resource.vo.MenuNodeVO;
import com.cloudmall.framework.models.system.user.vo.UserInfoVO;

/**
 * 系统应用服务接口
 * 提供系统级别的应用功能
 *
 * @author Tomatos
 * @date 2026/2/3
 */
public interface ISystemAppService {
    UserInfoVO getUserInfo();

    /**
     * 获取指定用户的菜单树
     * 
     * 该方法复用以下两个方法:
     * 1. 查询用户拥有的角色(可能有多个)
     * 2. 获取角色拥有的菜单资源
     *
     * @return 菜单树列表
     */
    MenuNodeVO getUserMenuTree();

    MenuNodeVO queryResourceTree();
}