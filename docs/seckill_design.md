# 秒杀活动设计文档

## 一、设计概览

### 核心原则

✅ **只有审核通过的秒杀商品才会在数据库表中出现**

### 系统架构

```
商家提交申请 → 创建审核记录(audit表) → 管理员审核
                                    ↓
                          ┌─────────┴─────────┐
                          ↓                   ↓
                      审核通过              审核拒绝
                          ↓                   ↓
                    创建seckill_goods      无记录
                          ↓
                    秒杀商品上线销售
```

### 表职责

| 表名 | 职责 | 特点 |
|-----|------|------|
| `seckill_activity` | 活动管理 | 平台维护，定义秒杀活动 |
| `audit` | 审核流程管理 | 通用表，记录所有审核历史 |
| `seckill_goods` | 已通过商品 | 只包含审核通过的商品，与审核完全解耦 |

---

## 二、各角色工作流

### 🛡️ 管理端工作流程

#### 1. 创建秒杀活动

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1.1 | 创建活动主题 | 填写活动名称（如"618大促"、"周末狂欢"） |
| 1.2 | 选择活动小时 | 选择开始小时（0-23，每个小时为一场） |
| 1.3 | 设置活动日期 | 选择具体的活动日期 |
| 1.4 | 发布活动 | 活动进入"报名中"状态，开放商家申请 |

#### 2. 审核商家申请

| 步骤 | 操作 | 说明 |
|------|------|------|
| 2.1 | 查看申请列表 | 按活动、商家、状态筛选 |
| 2.2 | 审核商品信息 | 检查商品状态、库存 |
| 2.3 | 审核价格 | 检查秒杀价格是否合理 |
| 2.4 | 通过/驳回 | 通过后商品进入活动，驳回需填写原因 |

#### 3. 活动管理

| 步骤 | 操作 | 说明 |
|------|------|------|
| 3.1 | 活动列表 | 查看所有活动及状态 |
| 3.2 | 活动详情 | 查看活动信息、已通过商品列表 |
| 3.3 | 手动结束 | 紧急情况下可提前结束活动 |

---

### 🏪 商家端工作流程

#### 1. 申请加入活动

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1.1 | 浏览活动列表 | 查看平台发布的"报名中"活动 |
| 1.2 | 选择活动 | 选择要参与的活动 |
| 1.3 | 选择商品 | 从自己的商品中选择参与秒杀的商品 |
| 1.4 | 设置秒杀价格 | 输入秒杀价格（需低于原价） |
| 1.5 | 设置秒杀库存 | 设置参与秒杀的库存数量 |
| 1.6 | 提交申请 | 提交给管理端审核 |

#### 2. 查看申请状态

| 步骤 | 操作 | 说明 |
|------|------|------|
| 2.1 | 申请列表 | 查看所有申请记录 |
| 2.2 | 状态跟踪 | 待审核/已通过/已驳回 |
| 2.3 | 查看详情 | 查看申请详情及驳回原因 |
| 2.4 | 重新申请 | 已驳回申请可修改后重新提交 |

---

### 📱 客户端工作流程

#### 1. 发现活动

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1.1 | 首页推荐 | 活动轮播图、推荐位 |
| 1.2 | 秒杀专区 | 独立活动页面，展示所有秒杀活动 |
| 1.3 | 商品详情页 | 商品显示"参与X秒杀活动"标签 |
| 1.4 | 消息通知 | 活动开始前提醒 |

#### 2. 参与秒杀

| 步骤 | 操作 | 说明 |
|------|------|------|
| 2.1 | 选择活动 | 浏览不同秒杀活动 |
| 2.2 | 查看商品 | 活动内的秒杀商品列表 |
| 2.3 | 设置提醒 | 订阅活动开始通知 |
| 2.4 | 倒计时等待 | 活动开始前显示倒计时 |
| 2.5 | 立即抢购 | 点击进入商品详情/下单页 |
| 2.6 | 下单支付 | 限时支付（如15分钟） |

---

## 三、状态流转图

### 活动状态

```
报名中 → 进行中 → 已结束
  ↓
 管理端开始活动
 ↓
商家申请加入
```

### 审核状态（通过统一审核表管理）

```
待审核 → 已通过 → 秒杀商品上线
  ↓
已驳回 (可重新申请)
```

---

## 四、库存管理设计

### 4.0 库存管理方案（方案3：实时库存 + 快速回收）

本设计采用**拼多多方案**的思路，核心思想是**不预占库存，而是实时检查和共享库存**。

#### 设计目标

- ✅ **最大化库存利用率** - 不浪费库存预留
- ✅ **灵活支持多渠道销售** - 同一商品既能秒杀销售，也能常规销售
- ✅ **申请零成本** - 商家可灵活申请秒杀，不影响常规库存
- ✅ **快速失败检查** - 秒杀开始或用户下单时实时验证库存

#### 核心原理

```
库存管理三个阶段：

1. 申请阶段（商家申请秒杀）
   ├─ 常规库存 (products.stock) ：无变化
   ├─ 秒杀申报库存：仅记录在 audit 表
   ├─ 实际物理库存：保持不变
   └─ 目的：灵活申请，不冻结库存

2. 审核阶段（管理员审核通过）
   ├─ 创建 seckill_goods 记录（stock = 申报数）
   ├─ 常规库存：保持不变（不预占）
   ├─ 关键：创建时检查 products.stock >= 申报库存
   └─ 目的：创建秒杀商品，但保留库存灵活性

3. 销售阶段（秒杀进行中）
   ├─ 用户下单时，实时计算可售库存：
   │  └─ 可售 = min(seckill_goods.stock, products.stock)
   ├─ 扣减两个库存：
   │  ├─ seckill_goods.sold_count + 1
   │  └─ products.stock - 1（同步）
   └─ 目的：精确控制，防止超卖
```

#### 库存检查规则

| 时机 | 检查内容 | 说明 |
|------|---------|------|
| 申请提交时 | `申报库存 ≤ products.stock` | 确保当前有足够库存 |
| 审核通过时 | `申报库存 ≤ products.stock` | 再次检查（以防库存变化） |
| 秒杀开始时 | `products.stock ≥ 0` | 最后确认库存状态 |
| 用户下单时 | `(stock - sold_count) > 0 AND products.stock > 0` | 双重检查防超卖 |

#### 库存变动时间线示例

```
初始状态：
├─ products.stock = 100         （常规库存）
├─ seckill_goods.stock = 50     （申报库存）
└─ seckill_goods.sold_count = 0 （已售 = 0）

↓ 常规渠道销售了 10 件

常规销售后：
├─ products.stock = 90           （实时扣减）
├─ seckill_goods.stock = 50      （申报库存不变）
├─ seckill_goods.sold_count = 0  （秒杀未开始）
└─ 秒杀可售实际上限 = min(50, 90) = 50 件

↓ 秒杀开始，用户购买 1 件

用户购买后：
├─ products.stock = 89           （同步扣减）
├─ seckill_goods.stock = 50      （申报库存不变）
├─ seckill_goods.sold_count = 1  （秒杀已售 1 件）
└─ 秒杀剩余可售 = min(50-1, 89) = 49 件

↓ 用户下单后支付超时（15分钟未支付）

订单取消后：
├─ products.stock = 90           （恢复）
├─ seckill_goods.stock = 50      （申报库存不变）
├─ seckill_goods.sold_count = 0  （恢复）
└─ 库存恢复，其他用户可购买
```

#### 防超卖原子操作

```sql
-- 关键的库存扣减必须原子操作
-- 同时检查秒杀库存和常规库存
UPDATE seckill_goods sg
SET sold_count = sold_count + 1
WHERE id = 123
  AND (stock - sold_count) > 0;  -- 秒杀库存充足

UPDATE products p
SET stock = stock - 1
WHERE id = 456
  AND stock > 0;  -- 常规库存充足

-- 两个 UPDATE 必须是同一个事务
-- 如果任何一个失败，整个事务回滚
```

---

## 五、数据库表结构设计

### 5.1 核心设计原则

- **只有审核通过的秒杀商品才会在 `seckill_goods` 表中出现**
- `seckill_goods` 与 `audit` 表完全解耦
- 审核历史由通用 `audit` 表独立维护
- 表结构简洁，职责清晰
- **秒杀库存和常规库存实时共享**，不预占

### 5.2 表结构

```sql
-- 秒杀活动表（平台创建）
CREATE TABLE seckill_activity (
    id              bigint auto_increment primary key,
    name            varchar(255) not null comment '活动名称',
    start_hour      tinyint not null comment '开始小时（0-23）',
    activity_date   date not null comment '活动日期',
    status          tinyint default 0 not null comment '状态：0报名中 1进行中 2已结束',
    max_items       int comment '活动最大商品数',
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    -- 防止同一小时在同一天重复创建
    unique key uk_date_hour (activity_date, start_hour),
    index idx_status (status),
    index idx_date (activity_date)
) comment '秒杀活动表';

-- 秒杀商品表（只包含已通过审核的商品）
CREATE TABLE seckill_goods (
    id              bigint auto_increment primary key,
    activity_id     bigint not null comment '活动ID（外键）',
    goods_id      bigint not null comment '商品ID（外键）',
    merchant_id     bigint not null comment '商家ID（外键）',
    seckill_price   decimal(10, 2) not null comment '秒杀价格',
    stock           int not null comment '秒杀申报库存（申报时确定，不变）',
    sold_count      int default 0 comment '已售数量（实时更新）',
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    
    unique key uk_activity_product (activity_id, goods_id),
    index idx_merchant (merchant_id),
    foreign key (activity_id) references seckill_activity(id)
) comment '秒杀商品表 - 只包含审核通过的秒杀商品（方案3：实时库存）';

-- products 表（常规商品表，与秒杀共享库存）
-- 注：products 表结构由商品模块维护，此处仅说明库存字段
-- CREATE TABLE products (
--     id              bigint auto_increment primary key,
--     ... 其他字段 ...
--     stock           int not null comment '商品库存（秒杀与常规渠道共享）',
--     ... 其他字段 ...
-- );
```

**关键字段说明：**

- `seckill_goods.stock` - 秒杀申报库存（创建后永不改变）
- `seckill_goods.sold_count` - 秒杀已售数量（实时递增）
- `products.stock` - 常规商品库存（与秒杀共享，实时扣减）

**库存计算公式：**

```
秒杀可售库存 = min(seckill_goods.stock - seckill_goods.sold_count, products.stock)
剩余常规库存 = products.stock
总体库存消耗 = seckill_goods.sold_count + (products.stock 被秒杀扣减的部分)
```

### 5.3 审核表关联说明

秒杀申请审核通过的完整流程：

```
1. 商家提交申请
   ├─ 创建 audit 记录
   │  ├─ target_type = 'SECKILL_GOODS'
   │  ├─ target_id = null（暂无关联）
   │  ├─ status = '待审核'
   │  └─ snapshot = 申请的商品信息（JSON，包含申报库存）
   │
2. 管理员审核通过
   ├─ 检查：products.stock >= 申报库存 ✓
   ├─ 更新 audit.status = '通过'
   ├─ 更新 audit.audit_time = 当前时间
   ├─ 创建 seckill_goods 记录（stock = 申报库存）
   └─ 更新 audit.target_id = seckill_goods.id（可选）
   │
3. 管理员审核拒绝
   ├─ 更新 audit.status = '拒绝'
   ├─ 更新 audit.reason = 拒绝原因
   └─ 不创建 seckill_goods 记录
```

---

## 六、API接口设计

### 管理端接口

```
POST   /admin/seckill/activities           创建秒杀活动
GET    /admin/seckill/activities           活动列表
GET    /admin/seckill/activities/:id      活动详情
PUT    /admin/seckill/activities/:id      更新活动
DELETE /admin/seckill/activities/:id      删除活动
POST   /admin/seckill/activities/:id/start 开始活动

GET    /admin/seckill/applies              申请列表
POST   /admin/seckill/applies/:id/approve 通过申请（需检查库存）
POST   /admin/seckill/applies/:id/reject  驳回申请
```

### 商家端接口

```
GET    /merchant/seckill/activities       可报名活动列表
GET    /merchant/seckill/activities/:id   活动详情
GET    /merchant/seckill/applies           我的申请列表
POST   /merchant/seckill/applies           提交申请（需检查库存）
GET    /merchant/seckill/applies/:id       申请详情
PUT    /merchant/seckill/applies/:id       修改申请（仅待审核/已驳回，需检查库存）
DELETE /merchant/seckill/applies/:id       取消申请
GET    /merchant/seckill/my-products       活动中我的商品
GET    /merchant/seckill/stats/:activityId 活动数据统计
```

### 客户端接口

```
GET    /client/seckill/activities          秒杀活动列表
GET    /client/seckill/activities/:id      活动详情
GET    /client/seckill/products/:activityId 活动商品列表（实时可售库存）
GET    /client/seckill/product/:id         秒杀商品详情（实时可售库存）
POST   /client/seckill/order               下单接口（原子扣减库存）
```

**关键接口说明：**

| 接口 | 库存检查 | 说明 |
|------|---------|------|
| 提交申请 | `申报库存 ≤ products.stock` | 申请时检查当前库存充足 |
| 审核通过 | `申报库存 ≤ products.stock` | 再次检查（防库存变化） |
| 查询可售库存 | `min(stock - sold_count, products.stock)` | 实时计算可售 |
| 下单 | 原子操作两表 | 同时扣减两个库存表 |

---

## 七、页面结构设计

### 管理端页面

```
├── 秒杀活动管理
│   ├── 活动列表
│   ├── 创建活动
│   │   ├── 选择小时（0-23，每个小时一场）
│   │   └── 选择日期
│   ├── 活动详情
│   └── 数据统计
├── 申请审核
│   ├── 申请列表
│   └── 审核详情
```

### 商家端页面

```
├── 秒杀活动
│   ├── 活动列表
│   ├── 活动详情
│   ├── 申请加入
│   ├── 我的申请
│   └── 活动数据
```

### 客户端页面

```
├── 秒杀专区
│   ├── 活动列表
│   ├── 活动详情
│   └── 商品列表
```

---

## 八、活动规则设计

### 8.1 活动状态规则

| 状态 | 说明 | 商家可操作 |
|------|------|-------------|
| 报名中 | 开放商家申请 | ✅ 申请加入 |
| 进行中 | 活动进行中 | ❌ |
| 已结束 | 活动结束 | ❌ |

### 8.2 审核状态规则（通过统一审核表）

审核状态由通用 `audit` 表管理，秒杀商品数据流转说明：

| 状态 | 说明 | seckill_goods 表 | 可操作 |
|------|------|-----------------|--------|
| 待审核 | 商家已提交，等待审核 | ❌ 无记录 | 修改申请、取消 |
| 已通过 | 审核通过，已生成秒杀商品记录 | ✅ 已插入 | ❌ |
| 已驳回 | 审核未通过，无商品记录 | ❌ 无记录 | 修改后重新提交 |

**关键点：** `seckill_goods` 表中的每条记录都代表已通过审核的秒杀商品

### 8.3 时间规则

| 规则 | 说明 |
|------|------|
| 📅 活动创建 | 需至少提前24小时 |
| ⏰ 活动时长 | 固定1小时（如10点场 = 10:00-11:00） |
| 🕐 时间选择 | 只能选择整点开始（0-23点） |
| ⏰ 申请时限 | 只能在"报名中"状态申请 |
| 🚫 防重复 | 同一日期同一小时只能创建一个活动 |

### 8.4 商品规则（方案3：实时库存）

| 规则 | 说明 | 检测时机 |
|------|------|----------|
| 🚫 唯一活动 | 一个商品在同一活动中只能申请一次（待审核或已通过） | 提交申请、审核通过 |
| 📦 库存限制 | **申报库存 ≤ 当前 products.stock** | 提交申请、审核通过 |
| 💰 价格限制 | 秒杀价格 < 商品原价 | 提交申请 |
| 🔄 驳回后释放 | 申请被驳回后，商品可申请该活动或其他活动 | 驳回操作 |
| ✅ 上线条件 | 只有通过审核的申请，商品才会在 seckill_goods 表中出现 | 审核通过时 |
| 📊 实时共享 | 秒杀销售直接扣减 products.stock | 用户购买时 |

### 8.5 客户端限购规则

| 规则 | 说明 |
|------|------|
| 👤 每人限购 | 1件或自定义 |
| 🚫 防刷单 | 同一IP/设备限购 |
| 📊 流量控制 | 秒杀接口限流 |
| ⏱️ 支付倒计时 | 下单后15分钟超时自动取消 |
| 🔄 库存恢复 | 取消订单时，同时恢复 seckill_goods.sold_count 和 products.stock |

---

## 九、异常处理

| 场景 | 处理方案 |
|------|----------|
| 申请库存超过现有库存 | 申请/审核时检查，提示库存不足 |
| 常规渠道销售导致秒杀库存不足 | 秒杀可售库存实时计算，用户看到实际可售数 |
| 库存不足 | 售罄提示，推荐相似商品 |
| 超时未支付 | 自动取消订单，同时恢复两个库存 |
| 活动违规 | 管理端强制结束，通知商家 |
| 系统故障 | 降级方案，暂停秒杀入口 |
| 价格误填 | 审核环节拦截，驳回修改 |

---

## 十、库存一致性保证

### 10.1 两阶段提交原理

```
场景：用户下单购买秒杀商品

执行流程：

1. 开始事务
   ├─ UPDATE seckill_goods SET sold_count = sold_count + 1 WHERE id = ? AND (stock - sold_count) > 0
   ├─ UPDATE products SET stock = stock - 1 WHERE id = ? AND stock > 0
   └─ 若任何一个 UPDATE 影响行数为 0，则回滚整个事务

2. 事务提交
   └─ 两个库存同时生效

3. 事务回滚（失败时）
   └─ 两个库存都恢复原值
```

### 10.2 防超卖检查

```sql
-- 方案3 的防超卖核心：原子性 + 双重检查

-- 第1层：秒杀库存检查
SELECT COUNT(*) FROM seckill_goods 
WHERE id = 123 AND (stock - sold_count) > 0;

-- 第2层：常规库存检查
SELECT COUNT(*) FROM products 
WHERE id = 456 AND stock > 0;

-- 两个检查都通过才能执行扣减
-- 扣减时使用 WHERE 条件防止超卖：
UPDATE seckill_goods SET sold_count = sold_count + 1 
WHERE id = 123 AND (stock - sold_count) > 0;

UPDATE products SET stock = stock - 1 
WHERE id = 456 AND stock > 0;
```

### 10.3 库存审计日志

为了追踪库存变动，建议添加审计表：

```sql
-- 库存变动审计表（可选）
CREATE TABLE inventory_audit_log (
    id              bigint auto_increment primary key,
    product_id      bigint not null comment '商品ID',
    seckill_goods_id bigint comment '秒杀商品ID（如果是秒杀订单）',
    operation_type  varchar(50) not null comment '操作类型：SECKILL_SOLD/REFUND/MANUAL_ADJUST',
    change_amount   int comment '变动数量',
    before_stock    int comment '变动前库存',
    after_stock     int comment '变动后库存',
    order_id        bigint comment '订单ID',
    operator_id     bigint comment '操作人ID',
    remarks         varchar(500) comment '备注',
    create_time     datetime default current_timestamp(),
    
    index idx_product (product_id),
    index idx_time (create_time)
) comment '库存变动审计日志';
```

---

## 十一、方案对比总结

### 11.1 三种方案对比

| 维度 | 方案1：预占型 | 方案2：分层型 | **方案3：实时型（采用）** |
|------|-------------|-------------|------------------------|
| **库存预占时机** | 申请时 | 审核时 | 不预占 |
| **常规库存影响** | 立即扣减 | 标记预留 | 不影响（销售时扣减） |
| **灵活性** | 低（库存长期冻结） | 中等 | **最高（零冻结）** |
| **管理复杂度** | 低 | 高（分级管理） | **中等（实时计算）** |
| **库存利用率** | 低（可能浪费） | 高 | **最高（完全共享）** |
| **防超卖能力** | 最强 | 最强 | **强（原子操作）** |
| **适用场景** | 库存紧张 | 大体量平台 | **中小型平台、库存充足** |

