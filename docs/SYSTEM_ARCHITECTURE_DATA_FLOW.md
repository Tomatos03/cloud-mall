# 系统架构设计文档

## 3.2.2 数据流分析

### 3.2.2.1 系统概述

本项目采用**分布式微服务架构**，基于 **Spring Boot 3.5.7** 框架构建，整合了多种企业级中间件和数据库技术，实现了高效的数据流通和处理能力。

#### 核心技术栈
- **后端框架**: Spring Boot 3.5.7 + Spring Web MVC
- **数据库**: MariaDB（关系型数据库）
- **缓存层**: Redis（分布式缓存）
- **ORM框架**: MyBatis Plus 3.5.14（数据持久层）
- **认证授权**: Spring Security + JWT（JJWT 0.13.0）
- **工具库**: Hutool 5.8.42（通用工具集）
- **数据验证**: Spring Validation（参数校验）

---

### 3.2.2.2 数据存储架构

#### 1. 数据库设计

本系统采用**单一 MariaDB 数据库**存储所有业务数据，主要包含以下核心数据表：

| 表名 | 说明 | 主要字段 | 关键特性 |
|------|------|---------|---------|
| **user** | 用户表 | id, username, password, nickname, phone, email, avatarUrl, role | 存储用户基本信息和角色 |
| **store** | 店铺表 | id, name, description, avatarUrl, owner_id, status | 支持多店铺业务模式 |
| **goods** | 商品表 | id, name, categoryId, description, img, imgList, inventory, price, storeId | 商品库存和价格管理 |
| **category** | 分类表 | id, name, parentId, level, sort, status | 商品分类树形结构 |
| **orders** | 订单表 | id, parentId, userId, storeId, orderNo, status, totalPrice, createdAt | 支持父子订单结构 |
| **order_item** | 订单项表 | id, orderId, goodsId, quantity, unitPrice, totalPrice | 订单与商品的关联 |
| **cart** | 购物车表 | id, userId, goodsId, num, unitId | 临时购物数据 |
| **address** | 地址表 | id, userId, receiver, detail, phone, isDefault | 用户配送地址 |
| **favorite** | 收藏表 | id, userId, goodsId, addedAt | 用户商品收藏 |
| **goods_comment** | 评论表 | id, orderItemId, userId, rating, content | 商品评价 |
| **banner** | 轮播表 | id, title, imageUrl, goodsId, isRecommend | 首页轮播配置 |
| **notice** | 通知表 | id, content | 系统通知 |

#### 2. 缓存设计

使用 **Redis** 作为分布式缓存层，主要缓存场景：

- **会话缓存**: 用户登录令牌 (JWT Token)
- **商品缓存**: 热门商品信息、商品分类数据
- **用户缓存**: 用户信息、权限数据
- **列表缓存**: 订单列表、商品列表等频繁查询数据
- **计数器**: 浏览量、销量统计

---

### 3.2.2.3 系统数据流图

```
╔════════════════════════════════════════════════════════════════════════════════════════╗
║                              在线商城系统数据流架构图                                    ║
╚════════════════════════════════════════════════════════════════════════════════════════╝

┌─────────────────┐          ┌──────────────────┐           ┌──────────────────┐
│   用户端应用     │          │   管理端应用     │           │   商家端应用     │
│ (Web/Mobile)   │          │ (Admin Dashboard) │          │ (Merchant)       │
└────────┬────────┘          └────────┬─────────┘           └────────┬─────────┘
         │                            │                               │
         │ HTTP/JSON                  │ HTTP/JSON                    │ HTTP/JSON
         │ (REST API)                 │ (REST API)                   │ (REST API)
         │                            │                               │
         ▼                            ▼                               ▼
╔════════════════════════════════════════════════════════════════════════════════════╗
║                         Spring Boot 微服务应用层                                    ║
├─────────────────────────────┬──────────────────────────┬──────────────────────────┤
│   online-shop-web (用户端)  │  online-shop-manager    │   oneline-shop-framework │
│                              │   (管理端/商家端)        │   (共享框架)              │
├─────────────────────────────┼──────────────────────────┼──────────────────────────┤
│ ┌─ Controllers:            │ ┌─ Controllers:          │ ┌─ Controllers:         │
│ │  • GoodsController       │ │  • OrderController     │ │  • AuthController    │
│ │  • UserController        │ │  • UserController      │ │  • SystemController │
│ │  • CartController        │ │  • StoreController     │ │                      │
│ │  • OrderController       │ │  • AnalyticsCtrl       │ └─ Exception Handler  │
│ │  • CommentController     │ │                        │ ┌─ Interceptors:      │
│ └─ Services:              │ └─ Services:            │ │  • JWT认证拦截器    │
│   • GoodsService          │   • OrderService        │ │  • 参数校验拦截器   │
│   • CartService           │   • UserService         │ │  • 业务日志拦截器   │
│   • UserService           │   • StoreService        │ └─                     │
│   • OrderService          │   • AnalyticsService    │ ┌─ Models/Entities:  │
│   • CommentService        │                         │ │  • Account (user)   │
│                           │ └─ Repositories:        │ │  • Store            │
│ └─ Repositories:          │   (MyBatis Mappers)     │ │  • Goods            │
│   (MyBatis Mappers)       │   • OrderMapper         │ │  • Order/OrderItem  │
│   • GoodsMapper           │   • UserMapper          │ │  • Category         │
│   • UserMapper            │   • StoreMapper         │ │  • Cart/Address     │
│   • OrderMapper           │   • AnalyticsMapper     │ │  • Favorite/Comment │
│   • CartMapper            │                         │ │  • Banner/Notice    │
└─────────────────────────────┴──────────────────────────┴──────────────────────────┘
         │                            │                               │
         │ SQL CRUD操作               │ SQL查询/统计                   │ SQL操作
         │                            │                               │
         ▼                            ▼                               ▼
╔════════════════════════════════════════════════════════════════════════════════════╗
║                            ORM持久层 (MyBatis Plus)                                 ║
├────────────────────────────────────────────────────────────────────────────────────┤
│ • 动态SQL生成与执行                                                                  │
│ • 自动分页处理 (mybatis-plus-jsqlparser)                                           │
│ • 关联查询与结果映射                                                                 │
│ • 批量操作优化                                                                       │
└────────────────┬──────────────────────────────────────────────────────────────────┘
                 │ 数据库连接 (MariaDB JDBC)
                 ▼
╔════════════════════════════════════════════════════════════════════════════════════╗
║                          数据库层 (MariaDB)                                         ║
├─────────────────┬──────────────────┬──────────────┬──────────────┬────────────────┤
│  用户数据        │   商品数据        │   订单数据    │   购物车数据   │   其他业务数据  │
├─────────────────┼──────────────────┼──────────────┼──────────────┼────────────────┤
│ • user          │ • goods          │ • orders     │ • cart       │ • address      │
│ • store         │ • category       │ • order_item │              │ • favorite     │
│                 │ • banner         │              │              │ • goods_comment│
│                 │                  │              │              │ • notice       │
└─────────────────┴──────────────────┴──────────────┴──────────────┴────────────────┘

                 ┌──────────────────────────────────────────────────────┐
                 │         缓存层 (Redis)                                 │
                 ├──────────────────────────────────────────────────────┤
                 │ • 用户会话缓存 (JWT Token验证)                       │
                 │ • 商品热点数据缓存 (查询优化)                         │
                 │ • 用户权限缓存                                        │
                 │ • 订单状态缓存                                        │
                 │ • 计数统计 (浏览量、销量)                            │
                 └──────────────────────────────────────────────────────┘

```

---

### 3.2.2.4 数据流转流程说明

#### **场景 1: 用户购物流程**

```
1. 浏览商品阶段
   用户端 → [GET /api/goods/list] → GoodsController → GoodsService
   ↓
   GoodsService 首先查询Redis缓存中的商品列表
   ├─ 缓存命中 → 直接返回缓存数据 ✓
   └─ 缓存未命中 → 查询数据库 → MyBatis Plus → MariaDB (goods表)
                  ↓ 数据库返回结果 → 更新Redis缓存
                  → 响应给客户端

2. 加入购物车阶段
   用户端 → [POST /api/cart/add] → CartController → CartService
   ↓
   CartService → CartMapper → MyBatis Plus → MariaDB (cart表)
   ↓ 数据库写入成功
   → Redis 更新用户购物车缓存 (可选)
   → 响应给客户端

3. 生成订单阶段
   用户端 → [POST /api/order/create] → OrderController → OrderService
   ↓
   OrderService 分布式事务处理:
   ├─ 清空购物车 (DELETE cart WHERE userId=?)
   ├─ 创建父订单 (INSERT orders WHERE parentId IS NULL)
   ├─ 按店铺分组创建子订单 (INSERT orders WHERE parentId=?)
   ├─ 创建订单项 (INSERT order_item FOR EACH item)
   ├─ 更新商品库存 (UPDATE goods SET inventory=inventory-?)
   └─ Redis 清除相关缓存
   ↓
   MariaDB 持久化订单数据
   → OrderService 返回订单号
   → 响应给用户端

```

#### **场景 2: 商家管理订单**

```
商家登录
  ↓
[GET /manager/order/page] → OrderController → JWT拦截器验证
  ↓ (验证通过，获取商家ID)
  ↓
OrderService.getOrdersByStore(storeId, pageNum, pageSize)
  ↓
OrderMapper 动态SQL查询:
  SELECT * FROM orders WHERE store_id=? LIMIT ?, ?
  ↓
MyBatis Plus 自动分页处理
  ↓
MariaDB 执行SQL → 返回结果集
  ↓
MyBatis 自动映射到Order对象列表
  ↓
Redis 缓存此查询结果 (可选)
  ↓
响应给管理端: List<Order> + 总数 + 当前页

```

#### **场景 3: 用户认证流程**

```
用户登录
  ↓
[POST /api/user/login] → AuthController → UserService
  ↓
UserService.authenticate(username, password)
  ├─ 查询数据库: SELECT * FROM user WHERE username=?
  ├─ Spring Security 密码加密验证
  └─ 匹配成功 ✓
  ↓
JWT Token生成:
  Token = JJWT.builder()
    .setSubject(userId)
    .setRole(userRole)
    .setIssuedAt(now)
    .setExpiration(now+24h)
    .signWith(secretKey)
    .compact()
  ↓
Redis 缓存Token + 用户权限信息 (key: token, ttl: 24h)
  ↓
响应给客户端: {token, user_info, expire_time}

后续请求流程:
  客户端 → [Header: Authorization: Bearer {token}] → 所有API
    ↓
  JWT拦截器 → 从Header提取token
    ↓
  验证Token签名 & 过期时间 (JJWT.parser())
    ├─ 验证通过 → 从Redis缓存获取权限信息 → 放行 ✓
    └─ 验证失败 → 401 Unauthorized ✗

```

---

### 3.2.2.5 核心数据流特性

#### **1. 关系型数据结构**

```
用户中心                         订单管理
┌─────────┐                    ┌────────────┐
│   user  │─────────┐          │   orders   │
└─────────┘         │          └─────┬──────┘
    ▲               │                │
    │ 1:1           │ 1:N            │ 1:N
    │               │                │
    │               ▼                ▼
    │           ┌─────────┐      ┌──────────┐
    │           │  store  │      │order_item│
    │           └─────────┘      └────┬─────┘
    │               │                  │
    │               │ 1:N               │ N:1
    │               ▼                  ▼
    └───────────┌────────┐        ┌────────┐
                │ goods  │        │ goods  │
                └────────┘        └────────┘
  
用户与购物功能
┌─────────┐
│  user   │
└────┬────┘
     │ 1:N
     ▼
  ┌──────────┐      ┌─────────┐      ┌──────────┐
  │  cart    │      │ favorite│      │ address  │
  └──────────┘      └─────────┘      └──────────┘
     │                  │                 │
     │ N:1              │ N:1