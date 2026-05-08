# Code Style (Required)

## Naming
- Interfaces: `I` prefix (e.g., `IOrderService`), implementations: drop `I` (e.g., `OrderService`)
- Query methods: always start with `query` (e.g., `queryOrderList`, `queryOrderDetail`)

## Imports
- Order: JDK -> third-party (alphabetical) -> project (alphabetical)
- Blank line between groups. No wildcard (`*`) imports. No FQCN in business code.

## Collection null checks
- Use `CollUtil.isEmpty()` / `CollUtil.isNotEmpty()` from Hutool. No manual `collection == null || collection.isEmpty()`.

## MyBatis-Plus queries
- Use `service.lambdaQuery()` chain style. Never `service.list(new LambdaQueryWrapper<>())`.
