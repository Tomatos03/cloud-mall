package com.onlineshop.framework.models.chat.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.chat.dto.IMPageParamsDTO;
import com.onlineshop.framework.models.chat.entity.ChatMessage;
import com.onlineshop.framework.models.chat.mapper.ChatMessageMapper;
import com.onlineshop.framework.models.chat.service.IChatMessageService;
import com.onlineshop.framework.models.chat.vo.MessageVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息服务实现
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    /**
     * 分页获取会话历史消息
     */
    @Override
    public IPage<MessageVO> pageMessageHistory(IMPageParamsDTO params) {
        Page<ChatMessage> page = new Page<>(calculateReversePage(params), params.getPageSize());

        IPage<ChatMessage> messagePage = lambdaQuery().eq(ChatMessage::getSessionId, params.getSessionId())
                                                      .orderByAsc(ChatMessage::getCreateTime)
                                                      .page(page);

        return messagePage.convert(
                msg -> MessageVO.builder()
                                .userId(msg.getSenderId())
                                .content(msg.getContent())
                                .type(msg.getType())
                                .time(msg.getCreateTime())
                                .build()
        );
    }

    private Integer calculateReversePage(IMPageParamsDTO params) {
        Long totalCount = lambdaQuery().eq(ChatMessage::getSessionId, params.getSessionId())
                                       .count();
        Integer pageSize = params.getPageSize();
        Long totalPage = (totalCount + pageSize - 1) / pageSize; // 计算总页数
        return Math.toIntExact(totalPage - params.getPage() + 1); // 反转页
    }

    /**
     * 删除过期消息（定时任务调用）
     */
    @Override
    public void deleteExpiredMessages() {
        remove(query().lt("expire_time", LocalDateTime.now()));
    }

    @Override
    public ChatMessage getLastMessage(Long sessionId) {
        return lambdaQuery()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("limit 1")
                .one();
    }

    @Override
    public Long countUnreadMessages(Long sessionId) {
        return lambdaQuery().eq(ChatMessage::getSessionId, sessionId)
                            .eq(ChatMessage::getIsRead, false)
                            .ne(ChatMessage::getSenderId, AuthUserUtils.getUserId())
                            .count();
    }
}