1. 项目采用功能分包，所有业务模块统一放在 oneline-shop-framework/models 目录下。
2. 某类（如 service、mapper、dto、vo 等）如果数量大于1，才为其创建目录。  
   - service 接口放在 service 目录，具体实现放在 service/impl 子目录。
   - mapper、dto、vo 也按此规则分目录。
3. Service 接口命名以 I 开头（如 IGoodsService），实现类去掉 I 前缀（如 GoodsService）。
4. ORM 框架统一使用 MyBatis-Plus。
5. 已注册全局结果处理器，controller 返回结果无需额外包装。
6. 统一使用 Lombok 注解简化 getter/setter 等方法。
7. 业务异常统一抛出 BusinessException，必须配合 BizErrorCode 枚举类。
8. 获取当前登录用户信息统一通过 UserContextHolder 工具类。
12. 已注册公共字段自动填充处理器，无需重复处理。
9. AppService（应用服务）如有多个 public 方法，需定义接口（如 IGoodsAppService），实现类实现该接口，接口只声明非私有方法。
10. 禁止创建总结文档，直接说明改动或重构内容即可。
11. 禁止使用全类名，必须 import 后再用类名。