-- 秒杀表设计重构迁移脚本
-- 目的：分离秒杀申请和秒杀商品，建立正确的审核流程

-- ==================== 秒杀申请表（新增）====================
-- 用于存储商家对秒杀活动的申请记录
CREATE TABLE IF NOT EXISTS seckill_application (
    id              bigint auto_increment primary key,
    activity_id     bigint not null comment '活动ID（外键）',
    product_id      bigint not null comment '商品ID（外键）',
    merchant_id     bigint not null comment '商家ID（外键）',
    seckill_price   decimal(10, 2) not null comment '秒杀价格',
    stock           int not null comment '秒杀库存',
    audit_id        bigint comment '审核记录ID（外键，关联审核表）',
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    
    unique key uk_activity_product_merchant (activity_id, product_id, merchant_id),
    index idx_merchant (merchant_id),
    index idx_audit (audit_id),
    index idx_activity (activity_id),
    
    foreign key (activity_id) references seckill_activity(id)
) comment '秒杀申请表（待审核或已拒绝的申请）';

-- ==================== 修改秒杀商品表 ====================
-- 移除audit_id，因为只有已通过审核的商品才会在此表
-- 添加application_id用于追踪来源

-- 备份原秒杀商品表数据（如果需要的话）
-- CREATE TABLE seckill_goods_backup AS SELECT * FROM seckill_goods;

-- 删除原外键约束（如果存在）
ALTER TABLE seckill_goods DROP FOREIGN KEY IF EXISTS seckill_goods_ibfk_1;

-- 删除不需要的audit_id字段
ALTER TABLE seckill_goods DROP COLUMN IF EXISTS audit_id;

-- 添加application_id字段，用于追踪来源申请
ALTER TABLE seckill_goods ADD COLUMN IF NOT EXISTS application_id bigint comment '申请记录ID（可选，用于追踪来源）' after merchant_id;

-- 添加索引
ALTER TABLE seckill_goods ADD INDEX IF NOT EXISTS idx_application (application_id);

-- 重新添加外键约束
ALTER TABLE seckill_goods ADD CONSTRAINT seckill_goods_ibfk_1 
    FOREIGN KEY (activity_id) REFERENCES seckill_activity(id);

-- ==================== 说明 ====================
-- 秒杀审核流程：
-- 1. 商家提交秒杀申请 → 插入 seckill_application
-- 2. 创建审核记录 → 插入 audit (target_type='SECKILL_GOODS')
-- 3. 管理员审核 → 更新 audit 表
-- 4. 审核通过 → 插入 seckill_goods，删除 seckill_application
-- 5. 审核拒绝 → 只更新 audit 表，seckill_application 保留作为历史记录
