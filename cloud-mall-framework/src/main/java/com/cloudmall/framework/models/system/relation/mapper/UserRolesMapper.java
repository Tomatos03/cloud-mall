package com.cloudmall.framework.models.system.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.system.relation.entity.UserRoles;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联表 Mapper 接口
 * 使用 mybatis-plus BaseMapper 提供的方法完成操作
 */
@Mapper
public interface UserRolesMapper extends BaseMapper<UserRoles> {
}