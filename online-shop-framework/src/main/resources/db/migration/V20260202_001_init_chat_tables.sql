-- 创建会话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `store_id` BIGINT NOT NULL COMMENT '店铺ID',
  `last_message` VARCHAR(500) COMMENT '最后一条消息内容',
  `last_message_time` DATETIME COMMENT '最后一条消息时间',
  `buyer_unread_count` INT DEFAULT 0 COMMENT '买家未读数',
  `merchant_unread_count` INT DEFAULT 0 COMMENT '商家未读数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_buyer_store` (`buyer_id`, `store_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 创建消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
  `store_id` BIGINT NOT NULL COMMENT '店铺ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '消息内容（文本或图片URL）',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '消息类型：0=文本, 1=图片',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';