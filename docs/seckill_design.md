# 秒杀活动设计文档

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
| 3.4 | 数据统计 | 活动整体数据、商家排名 |

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

#### 3. 活动数据查看

| 步骤 | 操作 | 说明 |
|------|------|------|
| 3.1 | 我的参与 | 查看已参与的活动 |
| 3.2 | 商品数据 | 查看各商品在活动中的表现 |
| 3.3 | 数据报表 | 浏览量、下单量、转化率等 |

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

## 四、数据库表结构设计

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

-- 秒杀商品表（关联统一审核表）
CREATE TABLE seckill_goods (
    id              bigint auto_increment primary key,
    activity_id     bigint not null comment '活动ID（外键）',
    audit_id        bigint not null comment '审核记录ID（外键，关联审核表）',
    product_id      bigint not null comment '商品ID（外键）',
    merchant_id     bigint not null comment '商家ID（外键）',
    seckill_price   decimal(10, 2) not null comment '秒杀价格',
    stock           int not null comment '秒杀库存',
    sold_count      int default 0 comment '已售数量',
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    unique key uk_activity_product (activity_id, product_id),
    index idx_merchant (merchant_id),
    foreign key (activity_id) references seckill_activity(id)
) comment '秒杀商品表';
```

---

## 五、API接口设计

### 管理端接口

```
POST   /admin/seckill/activities           创建秒杀活动
GET    /admin/seckill/activities           活动列表
GET    /admin/seckill/activities/:id      活动详情
PUT    /admin/seckill/activities/:id      更新活动
DELETE /admin/seckill/activities/:id      删除活动
POST   /admin/seckill/activities/:id/start 开始活动

GET    /admin/seckill/applies              申请列表
POST   /admin/seckill/applies/:id/approve 通过申请
POST   /admin/seckill/applies/:id/reject  驳回申请
```

### 商家端接口

```
GET    /merchant/seckill/activities       可报名活动列表
GET    /merchant/seckill/activities/:id   活动详情
GET    /merchant/seckill/applies           我的申请列表
POST   /merchant/seckill/applies           提交申请
GET    /merchant/seckill/applies/:id       申请详情
PUT    /merchant/seckill/applies/:id       修改申请（仅待审核/已驳回）
DELETE /merchant/seckill/applies/:id       取消申请
GET    /merchant/seckill/my-products       活动中我的商品
GET    /merchant/seckill/stats/:activityId 活动数据统计
```

### 客户端接口

```
GET    /client/seckill/activities          秒杀活动列表
GET    /client/seckill/activities/:id      活动详情
GET    /client/seckill/products/:activityId 活动商品列表
GET    /client/seckill/product/:id         秒杀商品详情
```

---

## 六、页面结构设计

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

## 七、活动规则设计

### 7.1 活动状态规则

| 状态 | 说明 | 商家可操作 |
|------|------|-------------|
| 报名中 | 开放商家申请 | ✅ 申请加入 |
| 进行中 | 活动进行中 | ❌ |
| 已结束 | 活动结束 | ❌ |

### 7.2 审核状态规则（通过统一审核表）

| 状态 | 说明 | 可操作 |
|------|------|---------|
| 待审核 | 商家已提交，等待审核 | 修改、取消 |
| 已通过 | 审核通过，已生成秒杀商品记录 | ❌ |
| 已驳回 | 审核未通过 | 修改后重新提交 |

### 7.3 时间规则

| 规则 | 说明 |
|------|------|
| 📅 活动创建 | 需至少提前24小时 |
| ⏰ 活动时长 | 固定1小时（如10点场 = 10:00-11:00） |
| 🕐 时间选择 | 只能选择整点开始（0-23点） |
| ⏰ 申请时限 | 只能在"报名中"状态申请 |
| 🚫 防重复 | 同一日期同一小时只能创建一个活动 |

### 7.4 商品规则

| 规则 | 说明 | 检测时机 |
|------|------|----------|
| 🚫 唯一活动 | 一个商品只能参与一个活动（待审核/已通过） | 提交申请、审核通过 |
| 📦 库存限制 | 秒杀库存 ≤ 商品总库存 | 提交申请 |
| 💰 价格限制 | 秒杀价格 < 商品原价 | 提交申请 |
| 🔄 驳回后释放 | 申请被驳回后，商品可申请其他活动 | 驳回操作 |

### 7.5 客户端限购规则

| 规则 | 说明 |
|------|------|
| 👤 每人限购 | 1件或自定义 |
| 🚫 防刷单 | 同一IP/设备限购 |
| 📊 流量控制 | 秒杀接口限流 |
| ⏱️ 支付倒计时 | 下单后15分钟超时自动取消 |

---

## 八、异常处理

| 场景 | 处理方案 |
|------|----------|
| 库存不足 | 售罄提示，推荐相似商品 |
| 超时未支付 | 自动取消订单，释放库存 |
| 活动违规 | 管理端强制结束，通知商家 |
| 系统故障 | 降级方案，暂停秒杀入口 |
| 价格误填 | 审核环节拦截，驳回修改 |

---

## 九、数据统计指标

### 商家维度

| 指标 | 说明 |
|------|------|
| 浏览量 | PV/UV |
| 下单量 | 活动期间下单数 |
| 支付单量 | 成功支付订单数 |
| 转化率 | 下单转化率/支付转化率 |
| 客单价 | 平均订单金额 |

### 平台维度

| 指标 | 说明 |
|------|------|
| 总活动数 | 累计活动数量 |
| 总参与人数 | 活动参与用户数 |
| 总GMV | 活动成交总额 |
| 活动热力排行 | 按热度/销量排序 |

---
