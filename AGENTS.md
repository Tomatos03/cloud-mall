# AGENTS.md

本文件用于指导 AI Agent 在 **online-mall** 仓库内进行一致、可维护的开发。
目标：**规则清晰、可执行、低冗余**。

## 1. 常用命令

### 全量构建
```bash
mvn clean compile -DskipTests
mvn clean package
```

### 单模块构建
```bash
mvn clean compile -DskipTests -pl online-shop-framework
mvn clean compile -DskipTests -pl online-shop-manager
mvn clean compile -DskipTests -pl online-shop-web
mvn clean compile -DskipTests -pl online-shop-merchant
```

### 测试
```bash
mvn test
mvn test -pl online-shop-framework
mvn test -Dtest=SeckillActivityServiceImplTest
mvn test -Dtest=SeckillActivityServiceImplTest#testCreateActivity
```

### 快速确认编译
```bash
mvn clean compile -DskipTests 2>&1 | tail -20
```

## 2. 模块结构

- `online-shop-framework`：核心领域模型、服务、通用能力
- `online-shop-manager`：管理端
- `online-shop-web`：用户端
- `online-shop-merchant`：商家端
- `im`：即时通讯模块

## 3. 代码风格（必须遵守）

### 命名
- 类：`PascalCase`
- 方法/变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 接口：`I` 前缀（如 `IOrderService`）
- 实现类：去掉 `I`（如 `OrderService`）

### Import 顺序
1. JDK
2. 第三方（按字母序）
3. 项目内包（按字母序）

要求：
- 分组之间空行
- 禁止 `*` 通配符导入
- 禁止在业务代码中使用全限定类名（FQCN）直接引用类型，必须先 `import` 再使用简类名

### 格式
- 4 空格缩进
- 行宽建议 <= 120
- 大括号使用 K&R 风格
- 方法/构造器参数列表一行放不下时，参数必须换行且“一行一个参数”，右括号与左括号对齐并与 `{` 同行
- 复杂逻辑可加简短注释，避免“翻译式注释”

### 集合判空
- 集合判空统一使用 Hutool：`CollUtil.isEmpty(...)` / `CollUtil.isNotEmpty(...)`
- 禁止使用 `collection == null || collection.isEmpty()`、`collection != null && !collection.isEmpty()` 这类手写判空

### MyBatis-Plus 查询风格
- Service 层查询统一使用 `service.lambdaQuery()` 链式写法
- 禁止使用 `service.list(new LambdaQueryWrapper<>())` 这类包装器直接传入写法

## 4. 分层与职责

- Controller 只做参数接收、鉴权上下文读取、调用 Service
- 业务规则、状态流转、数据一致性放在 Service / Application 层
- VO/DTO 转换放在 Service，不放 Controller
- 依赖注入使用 `@Autowired` 或构造器注入，不手动 `new`

## 5. 校验与异常

- 统一使用 `AssertUtils` 做业务校验
- 业务异常统一 `BizException + BizErrorCode`
- 禁止直接抛出通用 `Exception/RuntimeException`

示例（推荐）：
```java
AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
```

## 6. 用户上下文与公共字段

### 当前用户信息
必须使用 `AuthUserUtils`：
- `getUserId()`
- `getUsername()`
- `getStoreId()`
- `getAuthUser()`

不要在业务代码里直接访问 `SecurityContextHolder`。

### 公共字段自动填充
`createTime/updateTime/createUser/updateUser` 由 `MyMetaObjectHandler` 自动填充。
不要手动赋值这些字段。

## 7. 枚举与数据库字段

### 枚举标准
- 字段名：`code`、`desc`
- `code` 使用 `int`/`Integer`
- 提供 `of(int code)` 方法，非法值抛 `BizException`

### 实体字段
- 数据库状态字段使用 `Integer` 存储枚举 `code`
- 查询条件用 `Enum.getCode()`，不要直接传枚举对象

## 8. 分页接口标准

- 分页参数 DTO 必须继承 `PageParamsDTO`
- 字段统一：`page`、`pageSize`
- 列表查询接口使用 `GET`
- 参数通过 query string 自动绑定 DTO
- 路径建议以 `/list` 结尾

## 9. 审核体系约定

- 统一基于 `AbstractAuditor<T>`
- `submitAudit` 内先校验再落库
- 子类校验入口：
  - `validateAndFill(AuditSubmitDTO<T> submitDTO)`
  - 可同时校验 `submitDTO.bizPid` 与 `submitDTO.items`

### 秒杀商品审核请求约定
- 商家申请接口：`POST /seckill/activities/{id}/goods`
- 请求体：`List<SeckillGoodsAuditItemDTO>`（批量）
- `SeckillGoodsAuditItemDTO` 表示单商品：`goodsId/seckillPrice/stock`
- 活动 ID 从路径进入并写入 `AuditSubmitDTO.bizPid`

## 10. 提交前检查清单

- 编译通过：`mvn clean compile -DskipTests`
- 无通配符 import
- 无手动填充公共字段
- 使用 `AssertUtils` 和 `BizErrorCode`
- 新增分页接口符合第 8 节规范

## 11. 全局返回与异常处理约定

项目已注册全局返回处理器和异常处理器：
- `GlobalResponseHandler`（`ResponseBodyAdvice`）
- `GlobalExceptionHandler`（`@RestControllerAdvice`）

开发约定：
- Controller/Service 正常返回业务对象、集合、分页对象或 `void`，由全局处理器统一包装为 `Result.success(...)`
- 若返回类型本身是 `Result`，不会再次包装（避免双层 Result）
- 返回值为 `Boolean false` 时，会被统一转换为 `Result.error("操作失败")`
- 业务错误必须抛 `BizException`，由全局异常处理器转换为带业务码的错误响应
- 非业务异常由全局异常处理器统一返回通用错误信息，并记录错误日志

---

如需扩展规范，优先在本文件追加“明确可执行”的规则，避免大段示例堆积。
