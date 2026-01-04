package com.onlineshop.framework.models.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
