package com.onlineshop.framework.models.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.system.user.entity.UserQualification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户资质认证 Mapper
 */
@Mapper
public interface UserQualificationMapper extends BaseMapper<UserQualification> {
}