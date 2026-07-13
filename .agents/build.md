# Build & Dev Commands

## 模块结构

| 模块 | 说明 | 端口 | 启动类 |
|------|------|------|--------|
| `cloud-mall-framework` | 核心领域模型、服务、通用能力 | - | - |
| `cloud-mall-web` | 用户端 API | 7001 | `CloudMallWebApplication` |
| `cloud-mall-manager` | 管理端 API | 7000 | `CloudMallManageApplication` |
| `cloud-mall-merchant` | 商家端 API | 7002 | `CloudMallMerchantApplication` |
| `cloud-mall-aggregation` | 聚合启动模块 | 7777 | `CloudMallAggregationApplication` |
| `im` | 即时通讯模块 | 7010 | `IMApplication` |

## 构建

```bash
# 全量
mvn clean compile -DskipTests
mvn clean package

# 单模块
mvn clean compile -DskipTests -pl cloud-mall-framework
mvn clean compile -DskipTests -pl cloud-mall-manager
mvn clean compile -DskipTests -pl cloud-mall-web
mvn clean compile -DskipTests -pl cloud-mall-merchant

# 快速确认编译
mvn clean compile -DskipTests 2>&1 | tail -20
```

## 测试

```bash
mvn test
mvn test -pl cloud-mall-framework
mvn test -Dtest=SeckillActivityServiceImplTest
mvn test -Dtest=SeckillActivityServiceImplTest#testCreateActivity
```

## 本地开发

### 启动依赖服务
```bash
docker compose -f docs/docker/docker-compose.yml up -d
```

### 启动应用
```bash
# 推荐：聚合模块（一次启动全端）
mvn -pl cloud-mall-aggregation spring-boot:run

# 单模块启动（需指定 profile）
mvn -pl cloud-mall-manager spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl cloud-mall-web spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl cloud-mall-merchant spring-boot:run -Dspring-boot.run.profiles=local

# im 模块单独启动，无需 profile
mvn -pl im spring-boot:run
```

### Profile 说明
- `dev` — 共享框架配置
- `env` — 环境特定值
- `local` — 本地开发环境值
- 聚合模块默认使用 `dev,env` 双 profile
- 单模块启动务必传入 `-Dspring-boot.run.profiles=local`
