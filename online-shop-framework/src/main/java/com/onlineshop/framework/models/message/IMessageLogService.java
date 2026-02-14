package com.onlineshop.framework.models.message;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.order.dto.OrderMessage;

import java.util.Collection;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
public interface IMessageLogService extends IService<MessageLog> {
    boolean batchSaveOrderMessageLog(Collection<OrderMessage> orderMessages);
}
