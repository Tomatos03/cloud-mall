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
│   │   │   ├── seckill/            # 秒杀模块 
│   │   │   └── ...
│   │   └── utils/                  # 工具类
│   └── src/main/resources/
│       └── db/                     # 数据库脚本
├── online-shop-web/                # Web 层（用户端）
├── online-shop-manager/            # 管理平台
├── online-shop-merchant/           # 商家平台
└── im/                             # 即时通讯模块
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
