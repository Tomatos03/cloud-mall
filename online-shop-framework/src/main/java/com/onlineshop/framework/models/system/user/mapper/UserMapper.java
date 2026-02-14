package com.onlineshop.framework.models.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
