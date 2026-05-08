# Enum Conventions

- Fields: `code` (int), `desc` (String). Provide `of(int code)` method throwing `BizException` on invalid value.
- DB columns store enum as `Integer` (the `code`). Use `Enum.getCode()` in queries, never pass enum objects directly.
