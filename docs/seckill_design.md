# 秒杀活动设计文档

## 各角色工作流

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
| 3.2 | 活动详情 | 查看活动信息返回已加入秒杀活动的商品列表 |
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

## 库存管理设计

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

## 数据库表结构设计

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
) comment '秒杀商品表 - 只包含审核通过的秒杀商品';
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

---

## API接口设计

### 管理端接口

#### 基础响应格式
```json
{
  "code": 0,           // 0:成功, 其他:错误码
  "msg": "success",    // 成功消息或错误信息
  "data": {}           // 数据内容
}
```

#### 错误码说明
| 错误码 | 说明 |
|-------|------|
| 1001 | 活动不存在 |
| 1002 | 活动状态不允许操作 |
| 1003 | 参数错误 |
| 1004 | 无权限操作 |
| 1005 | 活动已结束 |

#### 1. 分页查询秒杀活动
```http
GET /admin/seckill/activities
```

**请求参数：**
- `page`: 当前页码（默认1）
- `pageSize`: 每页数量（默认10）
- `status`: 活动状态（0-报名中，1-进行中，2-已结束，可选）
- `name`: 活动名称搜索（可选）
- `date`: 活动日期（可选）

**响应数据：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total": 100,  // 总记录数
    "list": [
      {
        "id": 1,
        "name": "618大促",
        "startHour": 10,
        "activityDate": "2024-06-18",
        "status": 1,
        "maxItems": 100,
        "createTime": "2024-06-01 10:00:00",
        "updateTime": "2024-06-18 09:30:00"
      }
    ]
  }
}
```

#### 2. 停止秒杀活动
```http
POST /admin/seckill/activities/{id}/stop
```

**请求参数：**
- `id`: 活动ID

**响应数据：**
```json
{
  "code": 0,
  "msg": "活动已停止",
  "data": null
}
```

#### 3. 查询秒杀活动信息
```http
GET /admin/seckill/activities/{id}
```

**请求参数：**
- `id`: 活动ID

**响应数据：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "618大促",
    "startHour": 10,
    "activityDate": "2024-06-18",
    "status": 1,
    "maxItems": 100,
    "createTime": "2024-06-01 10:00:00",
  }
}
```

#### 4. 分页查询某个秒杀活动中的商品
```http
GET /admin/seckill/activities/{id}/goods
```

**请求参数：**
- `id`: 活动ID
- `page`: 当前页码（默认1）
- `pageSize`: 每页数量（默认10）
- `status`: 商品状态（可选：0-待审核，1-已通过，2-已驳回）

**响应数据：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total": 45,
    "list": [
      {
        "id": 101,
        "goodsName": "iPhone 15 Pro",
        "merchantName": "苹果官方旗舰店",
        "seckillPrice": 6999.00,
        "stock": 50,
        "status": 1,
      }
    ]
  }
}
```

#### 5. 创建秒杀活动
```http
POST /admin/seckill/activities
```

**请求参数：**
```json
{
  "name": "618大促",
  "startHour": 10,
  "activityDate": "2024-06-18",
  "maxItems": 100
}
```

**响应数据：**

```json
{
  "code": 0,
  "msg": "活动创建成功",
  "data": null
}
```

#### 6. 更新秒杀活动
```http
PUT /admin/seckill/activities/{id}
```

**请求参数：**
```json
{
  "name": "618大促（更新）",
  "maxItems": 150
}
```

**响应数据：**
```json
{
  "code": 0,
  "msg": "活动更新成功",
  "data": {
    "id": 1,
    "name": "618大促（更新）",
    "maxItems": 150
  }
}
```

#### 8. 开始秒杀活动
```http
POST /admin/seckill/activities/{id}/start
```

**响应数据：**
```json
{
  "code": 0,
  "msg": "活动已开始",
  "data": {
    "id": 1,
    "status": 1  // 1-进行中
  }
}
```

### 商家端接口

#### 基础响应格式
```json
{
  "code": 0,           // 0:成功, 其他:错误码
  "msg": "success",    // 成功消息或错误信息
  "data": {}           // 数据内容
}
```

#### 错误码说明
| 错误码 | 说明 |
|-------|------|
| 2001 | 活动不存在 |
| 2002 | 活动状态不允许操作 |
| 2003 | 参数错误 |
| 2004 | 无权限操作（非该商家活动） |
| 2005 | 商品不存在 |
| 2006 | 库存不足 |

#### 1. 分页查询活动列表
```http
GET /merchant/seckill/activities
```

**请求参数：**
- `page`: 当前页码（默认1）
- `pageSize`: 每页数量（默认10）
- `status`: 活动状态（0-报名中，1-进行中，2-已结束，可选）
- `name`: 活动名称搜索（可选）

**响应数据：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "name": "618大促",
        "startHour": 10,
        "activityDate": "2024-06-18",
        "status": 0,
        "maxItems": 100,
        "createTime": "2024-06-01 10:00:00",
        "applyCount": 23,        // 申请数量
        "approvedCount": 15      // 已通过数量
      }
    ]
  }
}
```

#### 2. 分页查询某个秒杀活动中的商品
```http
GET /merchant/seckill/activities/{id}/goods
```

**请求参数：**

- `id`: 活动ID
- `page`: 当前页码（默认1）
- `pageSize`: 每页数量（默认10）
- `status`: 商品状态（可选：0-待审核，1-已通过，2-已驳回，不传则返回全部）

**响应数据：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total": 45,
    "list": [
      {
        "id": 101,
        "goodsName": "iPhone 15 Pro",
        "goodsId": 1001,
        "seckillPrice": 6999.00,
        "originalPrice": 7999.00,
        "stock": 50,
        "soldCount": 32,
        "status": 1,            // 0-待审核，1-已通过，2-已驳回
        "applyTime": "2024-06-10 14:30:00",
        "auditTime": "2024-06-11 09:15:00",
        "rejectReason": null
      }
    ]
  }
}
```

#### 3. 批量将商品加入到秒杀活动
```http
POST /merchant/seckill/activities/{id}/batch-add
```

**请求参数：**
```json
{
  "goodsList": [
    {
      "goodsId": 1001,
      "seckillPrice": 6999.00,
      "stock": 50
    },
    {
      "goodsId": 1002,
      "seckillPrice": 2999.00,
      "stock": 100
    }
  ]
}
```

**响应数据：**
```json
{
  "code": 0,
  "msg": "批量添加成功",
  "data": {
    "successCount": 2,        // 成功数量
    "failCount": 0,          // 失败数量
    "failDetails": []        // 失败详情（如果有）
  }
}
```

#### 4. 从秒杀活动中批量删除商品
```http
POST /merchant/seckill/activities/{id}/batch-remove
```

**请求参数：**
```json
{
  "goodsIds": [1001, 1002, 1003]  // 商品ID列表
}
```

**响应数据：**
```json
{
  "code": 0,
  "msg": "批量删除成功",
  "data": {
    "successCount": 3,        // 成功删除数量
    "failCount": 0,          // 失败数量
    "failDetails": []        // 失败详情（如果有）
  }
}
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

## 页面原型设计

### 管理端页面



### 商家端页面



### 客户端页面



---

## 活动规则设计

### 8.1 活动状态规则

| 状态 | 说明 | 商家可操作 |
|------|------|-------------|
| 报名中 | 开放商家申请 | ✅ 申请加入 |
| 进行中 | 活动进行中 | ❌ |
| 已结束 | 活动结束 | ❌ |

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
