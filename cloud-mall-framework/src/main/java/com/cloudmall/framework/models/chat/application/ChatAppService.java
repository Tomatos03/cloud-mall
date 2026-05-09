package com.cloudmall.framework.models.chat.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudmall.framework.common.entity.PageParamsDTO;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.models.chat.dto.CreateSessionResult;
import com.cloudmall.framework.models.chat.entity.ChatMessage;
import com.cloudmall.framework.models.chat.entity.ChatSession;
import com.cloudmall.framework.models.chat.service.IChatMessageService;
import com.cloudmall.framework.models.chat.service.IChatSessionService;
import com.cloudmall.framework.models.chat.vo.ChatSessionVO;
import com.cloudmall.framework.models.store.IStoreService;
import com.cloudmall.framework.models.store.Store;
import com.cloudmall.framework.models.system.user.IUserService;
import com.cloudmall.framework.models.system.user.entity.User;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 聊天应用服务实现
 * 封装聊天业务逻辑
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Service
@RequiredArgsConstructor
public class ChatAppService implements IChatAppService {
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final IStoreService storeService;
    private final IUserService userService;

    @Override
    public IPage<ChatSessionVO> pageChatSession(PageParamsDTO paramsDTO) {
        Page<ChatSession> page = new Page<>(paramsDTO.getPage(), paramsDTO.getPageSize());
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        Long userId = AuthUserUtils.getUserId();
        wrapper.eq(ChatSession::getAgentId, userId)
               .or()
               .eq(ChatSession::getBuyerId, userId)
               .orderByDesc(ChatSession::getCreateTime);

        return chatSessionService.page(page, wrapper)
                                 .convert(this::conversationVO);
    }

    private ChatSessionVO conversationVO(ChatSession chatSession) {
        Long receiverId = determineReceiverId(chatSession);
        User receiverUser = userService.getById(receiverId);

        Long unReadCount = chatMessageService.countUnreadMessages(chatSession.getId());
        ChatMessage lastMessage = chatMessageService.getLastMessage(chatSession.getId());
        ChatSessionVO.ChatSessionVOBuilder builder = ChatSessionVO.builder()
                                                                  .id(chatSession.getId())
                                                                  .userId(receiverId)
                                                                  .name(receiverUser.getNickname())
                                                                  .avatar(receiverUser.getAvatarUrl())
                                                                  .unreadCount(unReadCount);

        if (lastMessage != null) {
            builder.lastMessageContent(lastMessage.getContent())
                   .lastTime(lastMessage.getCreateTime());
        }
        return builder.build();
    }

    private static Long determineReceiverId(ChatSession chatSession) {
        return chatSession.getBuyerId()
                          .equals(AuthUserUtils.getUserId())
                ? chatSession.getAgentId()
                : chatSession.getBuyerId();
    }

    @Override
    public void markReadForChatSession(Long sessionId) {
        ChatSession chatSession = chatSessionService.getById(sessionId);
        AssertUtils.notNull(chatSession, BizErrorCode.CONVERSATION_NOT_EXIST);

        Long senderId = AuthUserUtils.getUserId()
                                     .equals(chatSession.getBuyerId())
                ? chatSession.getAgentId()
                : chatSession.getBuyerId();

        chatMessageService.lambdaUpdate()
                          .eq(ChatMessage::getSessionId, sessionId)
                          .eq(ChatMessage::getSenderId, senderId)
                          .set(ChatMessage::getIsRead, true)
                          .update();
    }

    @Override
    public CreateSessionResult createChatSession(Long storeId) {
        Store store = storeService.getById(storeId);
        AssertUtils.notNull(store, BizErrorCode.STORE_NOT_EXIST);

        Long merchantId = store.getUserId();
        Long userId = AuthUserUtils.getUserId();
        ChatSession chatSession = chatSessionService.lambdaQuery()
                                                    .eq(ChatSession::getBuyerId, userId)
                                                    .eq(ChatSession::getAgentId, merchantId)
                                                    .one();
        if (chatSession == null) {
            chatSession = ChatSession.builder()
                                     .buyerId(AuthUserUtils.getUserId())
                                     .agentId(merchantId)
                                     .createTime(LocalDateTime.now())
                                     .build();
            chatSessionService.save(chatSession);
        }

        CreateSessionResult result = new CreateSessionResult();
        result.setSessionId(chatSession.getId());
        return result;
    }
}