package com.cloudmall.framework.models.chat.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.common.entity.PageParamsDTO;
import com.cloudmall.framework.models.chat.dto.CreateSessionResult;
import com.cloudmall.framework.models.chat.vo.ChatSessionVO;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

/**
 * 聊天应用服务接口
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
public interface IChatAppService {

    IPage<ChatSessionVO> pageChatSession(PageParamsDTO paramsDTO);

    void markReadForChatSession(Long sessionId);

    CreateSessionResult createChatSession(Long storeId);
}