# Domain Package Convention

Each domain under `com.cloudmall.framework.models.<domain>/` follows:
- `entity/` -- MyBatis-Plus entity extending `CommonDO`
- `mapper/` -- Mapper interfaces
- `service/` + `service/impl/` -- Service interfaces (`I`-prefix) and implementations
- `dto/` -- Input DTOs
- `vo/` -- Output view objects
- `enums/` -- Domain enums
- `application/` -- Complex workflow orchestration (order, seckill, audit)
