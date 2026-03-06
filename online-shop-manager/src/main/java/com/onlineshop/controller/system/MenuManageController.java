package com.onlineshop.controller.system;

import com.onlineshop.framework.models.system.application.ISystemAppService;
import com.onlineshop.framework.models.system.resource.dto.MenuFormData;
import com.onlineshop.framework.models.system.resource.service.IResourceService;
import com.onlineshop.framework.models.system.resource.vo.MenuNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('menu:view')")
public class MenuManageController {
    private final IResourceService resourceService;
    private final ISystemAppService systemAppService;

    @GetMapping("/user-tree")
    @PreAuthorize("isAuthenticated()")
    public MenuNodeVO getUserMenuTree() {
        return systemAppService.getUserMenuTree();
    }

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public MenuNodeVO getResourceTree() {
        return systemAppService.queryResourceTree();
    }

    /**
     * 添加菜单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('menu:add')")
    public void addMenu(@RequestBody MenuFormData form) {
        resourceService.addMenu(form);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:edit')")
    public void updateMenu(@PathVariable Long id, @RequestBody MenuFormData form) {
        resourceService.updateMenu(id, form);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('menu:delete')")
    public void deleteMenu(@RequestBody Collection<Long> ids) {
        resourceService.deleteMenu(ids);
    }
}
