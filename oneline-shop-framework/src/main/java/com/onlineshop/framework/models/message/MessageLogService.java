package com.onlineshop.framework.models.message;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@Service
public class MessageLogService extends ServiceImpl<MessageMapper, MessageLog> implements IMessageLogService {
}
