package com.cloudmall.framework.models.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.system.resource.Resource;
import com.cloudmall.framework.models.system.resource.dto.MenuFormData;

import java.util.Collection;
import java.util.List;

/**
 * 资源应用服务接口
 */
public interface IResourceService extends IService<Resource> {
    List<Resource> queryMenuResourcesByIds(Collection<Long> resourceIds);

    /**
     * 按ID列表查询所有资源
     */
    List<Resource> getResourcesByIds(Collection<Long> resourceIds);
    
    List<Resource> queryMenuResources();
    List<Resource> queryResources();

    /**
     * 添加菜单
     */
    void addMenu(MenuFormData form);

    /**
     * 更新菜单
     */
    void updateMenu(Long id, MenuFormData form);

    /**
     * 删除菜单
     */
    void deleteMenu(Collection<Long> ids);
}