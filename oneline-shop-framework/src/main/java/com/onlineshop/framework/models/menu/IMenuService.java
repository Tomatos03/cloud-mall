package com.onlineshop.framework.models.menu;

import com.onlineshop.framework.models.user.UserRole;
import java.util.List;

public interface IMenuService {
    List<Menu> getMenusByRole(UserRole role);
}

