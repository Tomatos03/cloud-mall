-- =====================================================
-- 订单表结构优化：支持父子订单
-- 创建时间：2025-01-24
-- 说明：为支持购物车多店铺结算，引入父子订单概念
-- =====================================================

-- 1. 添加订单类型字段
ALTER TABLE orders ADD COLUMN order_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单（单店铺）' AFTER status;

-- 2. 修改 parent_id 字段注释（如果已存在）
ALTER TABLE orders MODIFY COLUMN parent_id BIGINT COMMENT '父订单ID（多店铺场景）';

-- 3. 修改 store_id 字段允许为 NULL（父订单没有店铺）
ALTER TABLE orders MODIFY COLUMN store_id BIGINT NULL COMMENT '店铺ID（父订单为NULL）';

-- 4. 修改 num 字段注释
ALTER TABLE orders MODIFY COLUMN num BIGINT COMMENT '订单项数量（父订单表示子订单数量）';

-- 5. 添加索引优化查询性能
ALTER TABLE orders ADD INDEX idx_parent_id (parent_id) COMMENT '父订单ID索引';
ALTER TABLE orders ADD INDEX idx_order_type (order_type) COMMENT '订单类型索引';
ALTER TABLE orders ADD INDEX idx_user_order_type (user_id, order_type) COMMENT '用户订单类型联合索引';

-- 6. 添加检查约束（可选，MySQL 8.0.16+）
-- ALTER TABLE orders ADD CONSTRAINT chk_parent_order 
--     CHECK (
--         (order_type = 'PARENT' AND store_id IS NULL) OR 
--         (order_type IN ('SUB', 'NORMAL') AND store_id IS NOT NULL)
--     );

-- =====================================================
-- 数据迁移说明
-- =====================================================
-- 1. 所有现有订单默认为 NORMAL 类型（已通过 DEFAULT 处理）
-- 2. parent_id 为 NULL 的订单保持不变
-- 3. 新创建的订单会根据业务逻辑设置正确的 order_type

-- =====================================================
-- 回滚脚本（如需要）
-- =====================================================
-- ALTER TABLE orders DROP INDEX idx_parent_id;
-- ALTER TABLE orders DROP INDEX idx_order_type;
-- ALTER TABLE orders DROP INDEX idx_user_order_type;
-- ALTER TABLE orders DROP COLUMN order_type;
-- ALTER TABLE orders MODIFY COLUMN store_id BIGINT NOT NULL;