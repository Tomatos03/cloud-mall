# Pre-Commit Checklist

- 编译通过：`mvn clean compile -DskipTests`
- 无通配符 import
- 无手动填充公共字段
- 使用 `AssertUtils` 和 `BizErrorCode`
- 新增分页接口符合领域约定
- 新领域模块遵循标准包结构
