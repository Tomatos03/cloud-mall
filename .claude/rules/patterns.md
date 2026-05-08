# Key Architectural Patterns

- **Global response wrapping**: `GlobalResponseHandler` (ResponseBodyAdvice) auto-wraps returns in `Result.success(data)`. Do not return `Result` manually -- return the raw business object.
- **Global exception handling**: `GlobalExceptionHandler` converts `BizException` to error responses. Throw `BizException + BizErrorCode`, never raw `Exception/RuntimeException`.
- **Business validation**: Use `AssertUtils` for all preconditions (e.g., `AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST)`).
- **User context**: Always use `AuthUserUtils` (`getUserId()`, `getUsername()`, `getStoreId()`). Never access `SecurityContextHolder` directly.
- **Audit fields**: `createTime/updateTime/createUser/updateUser` are auto-filled by `MyMetaObjectHandler`. Never set manually.
- **Pagination**: DTOs extend `PageParamsDTO` with `page`/`pageSize` fields. List endpoints use `GET` with query string binding.
- **Security**: Stateless JWT via Spring Security. Each app module has its own `SecurityFilterChain` scoped to its URL prefix (`/web/**`, `/manager/**`, `/merchant/**`). Configurable whitelist per module.
