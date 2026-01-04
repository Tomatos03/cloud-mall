package com.onlineshop.controller.common;

import com.onlineshop.framework.models.menu.Menu;
import com.onlineshop.framework.models.user.UserRole;
import com.onlineshop.framework.models.menu.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@RequestMapping("/manager/menu")
@RestController
public class MenuController {

    @Autowired
    private IMenuService menuService;

    @GetMapping("/{role}")
    public List<Menu> getRoleMenu(@PathVariable UserRole role){
        return menuService.getMenusByRole(role);
    }
}
