-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键，自增',
  `product_id` BIGINT NOT NULL COMMENT '商品ID（外键）',
  `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
  `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `stock` INT NOT NULL COMMENT '秒杀库存',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_start_time` (`start_time`),
  INDEX `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS `seckill_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键，自增',
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_id` BIGINT COMMENT '关联订单ID',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `status` INT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退货',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_seckill_id` (`seckill_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_status` (`status`),
  FOREIGN KEY (`seckill_id`) REFERENCES `seckill_activity`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';