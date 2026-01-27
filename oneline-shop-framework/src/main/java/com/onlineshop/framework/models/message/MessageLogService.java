package com.onlineshop.framework.models.message;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.order.dto.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 消息服务实现类
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Slf4j
@Service
public class MessageLogService extends ServiceImpl<MessageMapper, MessageLog> implements IMessageLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveOrderMessageLog(Collection<OrderMessage> orderMessages) {
        List<MessageLog> messageLogs = orderMessages.stream()
                                                    .map(this::buildMessageLogFrom)
                                                    .toList();
        return saveBatch(messageLogs);
    }

    private MessageLog buildMessageLogFrom(OrderMessage orderMessage) {
        return MessageLog.builder()
                         .bizId(orderMessage.getOrderNo())
                         .bizType(orderMessage.getBizType())
                         .payload(orderMessage.getOrderJson())
                         .topic(orderMessage.getTopic())
                         .build();
    }
}
