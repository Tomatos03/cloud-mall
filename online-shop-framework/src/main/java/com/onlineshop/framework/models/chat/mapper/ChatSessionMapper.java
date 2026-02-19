package com.onlineshop.framework.models.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}