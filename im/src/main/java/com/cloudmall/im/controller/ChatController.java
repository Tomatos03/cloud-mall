package com.cloudmall.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.common.entity.PageParamsDTO;
import com.cloudmall.framework.models.chat.application.IChatAppService;
import com.cloudmall.framework.models.chat.dto.CreateSessionResult;
import com.cloudmall.framework.models.chat.dto.IMPageParamsDTO;
import com.cloudmall.framework.models.chat.service.IChatMessageService;
import com.cloudmall.framework.models.chat.vo.ChatSessionVO;
import com.cloudmall.framework.models.chat.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 * 提供聊天相关的API接口，符合 IM_API_SPEC 规范
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final IChatMessageService chatMessageService;
    private final IChatAppService chatAppService;

    @PostMapping("/session/create")
    public CreateSessionResult createChatSession(@RequestParam Long storeId) {
        return chatAppService.createChatSession(storeId);
    }

    /**
     * 获取当前用户的所有会话列表
     *
     * @param pageParams 分页参数（page: 页码，pageSize: 每页数量）
     * @return 分页会话列表
     */
    @GetMapping("/sessions")
    public IPage<ChatSessionVO> queryChatSession(@Valid PageParamsDTO pageParams) {
        return chatAppService.pageChatSession(pageParams);
    }

    /**
     * 获取指定会话的消息历史
     * GET /chat/history?conversationId=1&page=1&pageSize=20
     *
     * @param params 消息历史分页查询参数（包含会话ID、页码、每页数量）
     * @return 分页消息列表
     */
    @GetMapping("/history")
    public IPage<MessageVO> getMessageHistory(@Valid IMPageParamsDTO params) {
        return chatMessageService.pageMessageHistory(params);
    }

    /**
     * 标记会话为已读
     * POST /chat/read/{conversationId}
     *
     * @param sessionId 会话ID
     */
    @PutMapping("/read/{sessionId}")
    public void markAsRead(@PathVariable Long sessionId) {
        chatAppService.markReadForChatSession(sessionId);
    }
}