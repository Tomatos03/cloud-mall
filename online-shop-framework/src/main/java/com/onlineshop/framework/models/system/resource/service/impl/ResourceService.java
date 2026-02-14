package com.onlineshop.framework.models.system.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.system.resource.Resource;
import com.onlineshop.framework.models.system.resource.ResourceMapper;
import com.onlineshop.framework.models.system.resource.ResourceMeta;
import com.onlineshop.framework.models.system.resource.dto.MenuFormData;
import com.onlineshop.framework.models.system.resource.enums.ResourceType;
import com.onlineshop.framework.models.system.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 资源应用服务实现类
 * 使用 mybatis-plus 直接查询实现菜单树功能
 */
@Service
@RequiredArgsConstructor
public class ResourceService extends ServiceImpl<ResourceMapper, Resource> implements IResourceService {
    public List<Resource> queryMenuResourcesByIds(Collection<Long> resourceIds) {
        return lambdaQuery()
                .in(Resource::getId, resourceIds)
                .and(w -> w
                        .eq(Resource::getType,
                            ResourceType.MENU.getCode())
                        .or()
                        .eq(Resource::getType,
                            ResourceType.CATALOG.getCode())
                )
                .list();
    }

    public List<Resource> getResourcesByIds(Collection<Long> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(Resource::getId, resourceIds)
                .list();
    }

    @Override
    public void addMenu(MenuFormData form) {
        Resource resource = convertToResource(form);
        save(resource);
    }

    @Override
    public void updateMenu(Long id, MenuFormData form) {
        Resource resource = convertToResource(form);
        resource.setId(id);
        updateById(resource);
    }

    @Override
    public void deleteMenu(Collection<Long> ids) {
        removeByIds(ids);
    }

    @Override
    public List<Resource> queryMenuResources() {
        return lambdaQuery()
                .and(w -> w
                        .eq(Resource::getType,
                            ResourceType.MENU.getCode())
                        .or()
                        .eq(Resource::getType,
                            ResourceType.CATALOG.getCode())
                )
                .list();
    }

    @Override
    public List<Resource> queryResources() {
        return lambdaQuery()
                .and(w -> w
                        .eq(Resource::getType,
                            ResourceType.MENU.getCode())
                        .or()
                        .eq(Resource::getType,
                            ResourceType.CATALOG.getCode())
                        .or()
                        .eq(Resource::getType,
                            ResourceType.BUTTON.getCode())
                )
                .list();
    }

    private Resource convertToResource(MenuFormData form) {
        ResourceMeta meta = ResourceMeta.builder()
                                        .path(form.getPath())
                                        .component(form.getComponent())
                                        .icon(form.getIcon())
                                        .label(form.getLabel())
                                        .build();
        return Resource.builder()
                       .code(form.getCode())
                       .type(form.getType())
                       .sort(form.getSort())
                       .enable(form.getIsEnable())
                       .parentId(form.getParentId())
                       .meta(meta)
                       .build();
    }
}