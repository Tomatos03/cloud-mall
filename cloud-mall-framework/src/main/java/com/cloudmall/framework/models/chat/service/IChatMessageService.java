package com.cloudmall.framework.models.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.chat.dto.IMPageParamsDTO;
import com.cloudmall.framework.models.chat.entity.ChatMessage;
import com.cloudmall.framework.models.chat.vo.MessageVO;

/**
 * 消息服务接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
public interface IChatMessageService extends IService<ChatMessage> {
    /**
     * 分页获取会话历史消息
     *
     * @param params 消息历史分页查询参数（包含会话ID、页码、每页数量）
     * @return 分页消息数据
     */
    IPage<MessageVO> pageMessageHistory(IMPageParamsDTO params);

    /**
     * 删除过期消息
     */
    void deleteExpiredMessages();

    ChatMessage getLastMessage(Long sessionId);

    Long countUnreadMessages(Long sessionId);
}