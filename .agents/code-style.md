# Code Style 

## 命名
- 类：`PascalCase`
- 方法/变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 接口：`I` 前缀（如 `IOrderService`）
- 实现类：去掉 `I`（如 `OrderService`）
- 查询方法：统一以 `query` 开头（如 `queryOrderList`、`queryOrderDetail`）

## Import 顺序
1. JDK
2. 第三方（按字母序）
3. 项目内包（按字母序）

要求：
- 分组之间空行
- 禁止 `*` 通配符导入
- 禁止在业务代码中使用全限定类名（FQCN）直接引用类型

## 格式
- 4 空格缩进
- 行宽建议 <= 120
- 大括号使用 K&R 风格
- 方法/构造器参数列表一行放不下时，参数必须换行且"一行一个参数"，右括号与左括号对齐并与 `{` 同行
- 复杂逻辑可加简短注释，避免"翻译式注释"

## 集合判空
- 统一使用 Hutool：`CollUtil.isEmpty(...)` / `CollUtil.isNotEmpty(...)`
- 禁止手写 `collection == null || collection.isEmpty()`

## MyBatis-Plus 查询风格
- Service 层查询统一使用 `service.lambdaQuery()` 链式写法
- 禁止 `service.list(new LambdaQueryWrapper<>())`
