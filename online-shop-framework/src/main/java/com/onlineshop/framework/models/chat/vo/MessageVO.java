package com.onlineshop.framework.models.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息视图对象
 * 对应前端所需的 MessageItem 数据格式
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageVO {
    private Long sessionId;
    private Long userId;
    /**
     * 消息内容（文本或图片URL）
     */
    private String content;
    private String type;

    /**
     * 消息时间 (ISO 8601格式)
     */
    private String time;
}