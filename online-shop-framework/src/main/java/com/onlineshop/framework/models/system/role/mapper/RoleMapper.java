package com.onlineshop.framework.models.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.system.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 Mapper 接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}