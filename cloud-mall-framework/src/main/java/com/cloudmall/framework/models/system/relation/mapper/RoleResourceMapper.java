package com.cloudmall.framework.models.system.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.system.relation.entity.RoleResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色资源关联表 Mapper 接口
 * 使用 mybatis-plus BaseMapper 提供的方法完成操作
 */
@Mapper
public interface RoleResourceMapper extends BaseMapper<RoleResource> {
}