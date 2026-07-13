# Domain Conventions

## 领域包结构
新增领域模块时遵循以下目录布局（位于 `com.cloudmall.framework.models.<domain>/`）：
- `entity/` — MyBatis-Plus 实体（继承 `CommonDO`）
- `mapper/` — Mapper 接口
- `service/` + `service/impl/` — 接口（`I` 前缀）和实现
- `dto/` — 入参 DTO
- `vo/` — 出参 VO
- `enums/` — 枚举
- `application/` — 复杂业务流程编排（如订单、秒杀、审核）

## 分层与职责
- Controller 只做参数接收、鉴权上下文读取、调用 Service
- 业务规则、状态流转、数据一致性放在 Service / Application 层
- VO/DTO 转换放在 Service，不放 Controller

## 安全与认证
- 采用无状态 JWT + Spring Security，每个接入端有自己的 `SecurityFilterChain`，按 URL 前缀隔离（`/web/**`、`/manager/**`、`/merchant/**`）
- 白名单路径在 `application.yml` 的 `auth.white-list` 中配置
- 获取用户信息使用 `AuthUserUtils`（`getUserId()`、`getUsername()`、`getStoreId()`、`getAuthUser()`），不直接访问 `SecurityContextHolder`

## 公共字段自动填充
`createTime/updateTime/createUser/updateUser` 由 `MyMetaObjectHandler` 自动填充，不要手动赋值。

## 校验与异常
- 统一使用 `AssertUtils` 做业务校验
- 业务异常统一 `BizException + BizErrorCode`
- 禁止直接抛出通用 `Exception/RuntimeException`
```java
AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
```

## 枚举标准
- 字段名：`code`（`int`/`Integer`）、`desc`
- 提供 `of(int code)` 方法，非法值抛 `BizException`
- 数据库状态字段使用 `Integer` 存储枚举 `code`
- 查询条件用 `Enum.getCode()`，不要直接传枚举对象

## 分页接口标准
- 分页参数 DTO 必须继承 `PageParamsDTO`
- 字段统一：`page`、`pageSize`
- 列表查询接口使用 `GET`
- 参数通过 query string 自动绑定 DTO
- 路径建议以 `/list` 结尾

## 全局返回与异常处理
项目已注册全局处理器：
- `GlobalResponseHandler`（`ResponseBodyAdvice`）
- `GlobalExceptionHandler`（`@RestControllerAdvice`）

约定：
- Controller/Service 正常返回业务对象、集合、分页对象或 `void`，由全局处理器统一包装为 `Result.success(...)`
- 若返回类型本身是 `Result`，不会再次包装
- 返回值为 `Boolean false` 时，会被统一转换为 `Result.error("操作失败")`
- 业务错误必须抛 `BizException`，由全局异常处理器转换为带业务码的错误响应
- 非业务异常由全局异常处理器统一返回通用错误信息，并记录错误日志

## 审核体系
- 统一基于 `AbstractAuditor<T>`
- `submitAudit` 内先校验再落库
- 子类校验入口：`validateAndFill(AuditSubmitDTO<T> submitDTO)`

### 秒杀商品审核请求约定
- 商家申请接口：`POST /seckill/activities/{id}/goods`
- 请求体：`List<SeckillGoodsAuditItemDTO>`（批量）
- `SeckillGoodsAuditItemDTO`：`goodsId/seckillPrice/stock`
- 活动 ID 从路径进入并写入 `AuditSubmitDTO.bizPid`
