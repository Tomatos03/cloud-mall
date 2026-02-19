# Online Mall - 电商平台系统

一个功能完整、架构清晰的电商平台系统，采用 SpringBoot + Mybatis-Plus + Redis 技术栈。

## 📑 项目概览

### 项目结构

```
online-mall/
├── online-shop-framework/          # 框架层（共享库）
│   ├── src/main/java/com/onlineshop/framework/
│   │   ├── common/                 # 通用模块
│   │   ├── exception/              # 异常处理
│   │   ├── global/                 # 全局配置
│   │   ├── models/                 # 业务模型
│   │   │   ├── address/            # 地址管理
│   │   │   ├── auth/               # 认证授权
│   │   │   ├── banner/             # 轮播图
│   │   │   ├── cart/               # 购物车
│   │   │   ├── category/           # 商品分类
│   │   │   ├── comment/            # 商品评论
│   │   │   ├── goods/              # 商品管理
│   │   │   ├── order/              # 订单管理
│   │   │   ├── seckill/            # 秒杀模块 ⭐ NEW
│   │   │   └── ...
│   │   └── utils/                  # 工具类
│   └── src/main/resources/
│       └── db/                     # 数据库脚本
├── online-shop-web/                # Web 层（用户端）
├── online-shop-manager/            # 管理平台
├── online-shop-merchant/           # 商家平台
└── im/                             # 即时通讯模块
```

---

## 🎯 秒杀功能模块 - 完整实现

### 模块介绍

秒杀功能是一个高并发、高性能的限时抢购模块，采用 **Redis 库存缓存 + 数据库双重验证** 的架构，支持：

- ✅ 原子性库存扣减
- ✅ 用户限流防刷
- ✅ 实时库存查询
- ✅ 订单追踪管理

### 核心架构

```
┌─────────────────────────────────────────────────┐
│           用户端接口 (SeckillWebController)     │
│  - 获取秒杀活动列表                              │
│  - 参与秒杀（核心）                              │
│  - 查询秒杀订单                                  │
└──────────────────┬──────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────┐
│      秒杀管理器 (SeckillManager)                │
│  - 限流检查（防刷）                              │
│  - 活动状态检查                                  │
│  - 原子性库存扣减                                │
│  - 秒杀订单生成                                  │
└──────────────────┬──────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ↓                   ↓
    ┌─────────────┐   ┌──────────────┐
    │  Redis      │   │  MySQL       │
    │  库存缓存    │   │  数据持久化   │
    └─────────────┘   └──────────────┘
```

### 📁 模块文件结构

```
online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/
├── entity/                          # 数据实体
│   ├── SeckillActivity.java         # 秒杀活动实体
│   └── SeckillOrder.java            # 秒杀订单实体
├── service/                         # 业务服务
│   ├── SeckillActivityService.java  # 活动服务接口
│   ├── SeckillActivityServiceImpl.java
│   ├── SeckillOrderService.java     # 订单服务接口
│   └── SeckillOrderServiceImpl.java
├── mapper/                          # 数据访问
│   ├── SeckillActivityMapper.java
│   └── SeckillOrderMapper.java
├── manager/                         # 核心业务逻辑
│   └── SeckillManager.java          # ⭐ 秒杀业务管理器
├── dto/                             # 数据传输对象
│   ├── SeckillActivityDTO.java
│   └── SeckillOrderDTO.java
├── vo/                              # 视图对象
│   ├── SeckillActivityVO.java
│   └── SeckillOrderVO.java
└── enums/                           # 枚举类
    ├── SeckillStatusEnum.java       # 秒杀活动状态
    └── SeckillOrderStatusEnum.java  # 秒杀订单状态
```

### 🔧 关键类说明

#### 1. **SeckillManager.java** - 核心业务管理器

负责秒杀的所有核心业务逻辑：

**主要方法**：

- `participateSeckill()` - 用户参与秒杀
    - 限流检查
    - 活动状态校验
    - Redis 原子性库存扣减
    - 秒杀订单生成
- `checkSeckillStatus()` - 检查活动状态
- `initializeStock()` - 初始化库存到 Redis
- `syncStockToDatabase()` - 库存同步回数据库
- `getRemainingStock()` - 获取剩余库存

#### 2. **SeckillWebController.java** - 用户端接口

用户参与秒杀的所有接口：

| 接口                                 | 方法 | 功能                    |
| ------------------------------------ | ---- | ----------------------- |
| `/web/seckill/activities/ongoing`    | GET  | 获取进行中的秒杀活动    |
| `/web/seckill/activities/upcoming`   | GET  | 获取即将开始的活动      |
| `/web/seckill/activities/{id}`       | GET  | 获取活动详情            |
| `/web/seckill/participate/{id}`      | POST | **参与秒杀（核心）** ⭐ |
| `/web/seckill/orders/{id}`           | GET  | 获取秒杀订单详情        |
| `/web/seckill/orders/user/{userId}`  | GET  | 查询用户秒杀订单        |
| `/web/seckill/orders/{id}/cancel`    | POST | 取消秒杀订单            |
| `/web/seckill/activities/{id}/stock` | GET  | 获取剩余库存            |

#### 3. **SeckillManagerController.java** - 管理端接口

秒杀活动管理接口：

| 接口                                        | 方法   | 功能             |
| ------------------------------------------- | ------ | ---------------- |
| `/manager/seckill/activity/create`          | POST   | 创建秒杀活动     |
| `/manager/seckill/activity/{id}`            | PUT    | 编辑秒杀活动     |
| `/manager/seckill/activity/{id}`            | GET    | 获取活动详情     |
| `/manager/seckill/activity/page`            | GET    | 分页查询活动     |
| `/manager/seckill/activity/{id}`            | DELETE | 删除活动         |
| `/manager/seckill/activity/{id}/init-stock` | POST   | 初始化库存缓存   |
| `/manager/seckill/activity/{id}/sync-stock` | POST   | 同步库存到数据库 |
| `/manager/seckill/activity/{id}/stock`      | GET    | 查询当前库存     |

### 🗄️ 数据库表结构

#### 秒杀活动表 (seckill_activity)

```sql
CREATE TABLE seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,          -- 主键
    product_id BIGINT NOT NULL,                    -- 商品ID
    start_time DATETIME NOT NULL,                  -- 秒杀开始时间
    end_time DATETIME NOT NULL,                    -- 秒杀结束时间
    seckill_price DECIMAL(10,2) NOT NULL,          -- 秒杀价格
    stock INT NOT NULL,                            -- 秒杀库存
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 秒杀订单表 (seckill_order)

```sql
CREATE TABLE seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,          -- 主键
    seckill_id BIGINT NOT NULL,                    -- 秒杀活动ID
    product_id BIGINT NOT NULL,                    -- 商品ID
    user_id BIGINT NOT NULL,                       -- 用户ID
    order_id BIGINT,                               -- 关联的普通订单ID
    seckill_price DECIMAL(10,2) NOT NULL,          -- 秒杀价格
    quantity INT NOT NULL DEFAULT 1,               -- 购买数量
    status INT NOT NULL DEFAULT 0,                 -- 订单状态
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🛠️ 技术栈

- **后端框架**: Spring Boot 2.x
- **数据库**: MySQL 5.7+
- **缓存**: Redis 5.0+
- **ORM**: Mybatis-Plus 3.x
- **权限**: Spring Security
- **实时通讯**: WebSocket
- **日志**: SLF4J + Logback

---
