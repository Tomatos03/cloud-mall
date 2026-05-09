package com.cloudmall.framework.models.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.models.chat.entity.ChatSession;
import com.cloudmall.framework.models.chat.mapper.ChatSessionMapper;
import com.cloudmall.framework.models.chat.service.IChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会话服务实现
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {
}