# AGENTS.md - Coding Guidelines for AI Agents

This document provides essential information for AI coding agents (like OpenCode) working in the **online-mall** e-commerce platform repository. It covers build commands, code style, and architectural patterns.

## Build & Test Commands

### Full Build (All Modules)
```bash
mvn clean compile -DskipTests
mvn clean package
```

### Build Single Module
```bash
mvn clean compile -DskipTests -pl online-shop-framework
mvn clean compile -DskipTests -pl online-shop-manager
mvn clean compile -DskipTests -pl online-shop-web
mvn clean compile -DskipTests -pl online-shop-merchant
```

### Run Tests
```bash
# All tests
mvn test

# Single module tests
mvn test -pl online-shop-framework

# Single test class
mvn test -Dtest=SeckillActivityServiceImplTest

# Single test method
mvn test -Dtest=SeckillActivityServiceImplTest#testCreateActivity
```

### Verify Build Success
```bash
mvn clean compile -DskipTests 2>&1 | tail -20
```

## Project Structure

- **online-shop-framework**: Shared library with domain models, services, utilities, and configuration
- **online-shop-manager**: Admin platform for managing activities and approvals
- **online-shop-web**: User-facing e-commerce platform
- **online-shop-merchant**: Merchant platform for sellers
- **im**: Instant messaging module

## Import Organization

Follow this order (with blank lines between groups):
```java
// Standard Java imports
import java.time.LocalDateTime;
import java.util.ArrayList;

// Third-party libraries (alphabetically)
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

// Local imports (alphabetically by package)
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.utils.AssertUtils;
```

**No wildcard imports** (`import com.example.*`). Always use explicit imports.

## Code Style & Conventions

### Naming Conventions
- **Classes**: `PascalCase` (e.g., `SeckillActivityService`, `SeckillOrder`)
- **Methods**: `camelCase`, start with action verb (e.g., `createActivity()`, `cancelOrder()`)
- **Constants**: `UPPER_SNAKE_CASE`
- **Variables**: `camelCase`
- **Packages**: `com.onlineshop.framework.models.[domain].service`

### Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: Practical limit ~120 characters
- **Braces**: Opening brace on same line (K&R style)
- **Blank lines**: Separate logical sections with blank lines or comments

### Class Structure Order
1. Package declaration
2. Imports
3. Javadoc
4. Class declaration with annotations (`@Service`, `@Component`, etc.)
5. Class-level fields with `@Autowired` or `@Value`
6. Constructors
7. Public methods (grouped logically with comments like `// ==================== Section ====================`)
8. Private helper methods
9. Nested classes (if any)

### Annotations
- Use `@Slf4j` for logging (from Lombok)
- Use `@Service` for service classes
- Use `@RestController` and `@RequestMapping` for controllers
- Use `@Autowired` or constructor injection (prefer `@Autowired` for consistency)
- Use `@RequiredArgsConstructor` from Lombok for constructor injection when needed

### Logging
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeckillActivityServiceImpl {
    public void createActivity(SeckillActivityDTO dto) {
        log.info("创建秒杀活动，产品ID: {}", dto.getProductId());
        // ... business logic
    }
}
```

## Error Handling

### Use AssertUtils for Validations
Always use `AssertUtils` utility class instead of inline if-throw patterns:

```java
// ✅ CORRECT - Using AssertUtils
public SeckillOrderVO getSeckillOrder(Long orderId) {
    SeckillOrder order = getById(orderId);
    AssertUtils.notNull(order, BizErrorCode.SECKILL_ORDER_NOT_EXIST);
    return convertToVO(order);
}

// ❌ WRONG - Inline validation
public SeckillOrderVO getSeckillOrder(Long orderId) {
    SeckillOrder order = getById(orderId);
    if (order == null) {
        throw new BizException(BizErrorCode.SECKILL_ORDER_NOT_EXIST);
    }
    return convertToVO(order);
}
```

### AssertUtils Methods
- `AssertUtils.notNull(obj, errorCode)` - Verify object is not null
- `AssertUtils.isNull(obj, errorCode)` - Verify object is null
- `AssertUtils.isEqual(obj1, obj2, errorCode)` - Verify two objects are equal
- `AssertUtils.isTrue(condition, errorCode)` - Verify boolean expression is true
- `AssertUtils.isFalse(condition, errorCode)` - Verify boolean expression is false
- `AssertUtils.assertNotBlank(str, errorCode)` - Verify string is not empty

### Exception Handling
All business exceptions use `BizException` with `BizErrorCode` enums:

```java
public class BizException extends RuntimeException {
    private BizErrorCode bizErrorCode;
    
    public BizException(BizErrorCode bizErrorCode) {
        super(bizErrorCode.getErrorMessage());
        this.bizErrorCode = bizErrorCode;
    }
}
```

Never use generic `Exception` or `RuntimeException`. Always use `BizException` with proper error codes.

## Service & Controller Patterns

### Service Implementation Pattern
```java
@Service
@Slf4j
public class DomainServiceImpl extends ServiceImpl<DomainMapper, Domain> 
        implements IDomainService {
    
    @Autowired
    private OtherService otherService;
    
    // ==================== Query Methods ====================
    
    public DomainVO getById(Long id) {
        log.info("查询...");
        Domain entity = super.getById(id);
        AssertUtils.notNull(entity, BizErrorCode.NOT_EXIST);
        return convertToVO(entity);
    }
    
    // ==================== Mutation Methods ====================
    
    public boolean create(DomainDTO dto) {
        log.info("创建...");
        Domain entity = new Domain();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreateTime(LocalDateTime.now());
        return save(entity);
    }
    
    // ==================== Helper Methods ====================
    
    private DomainVO convertToVO(Domain entity) {
        DomainVO vo = new DomainVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

### Controller Pattern (Thin HTTP Adapter)
```java
@RestController
@RequestMapping("/admin/domain")
@RequiredArgsConstructor
@Slf4j
public class DomainController {
    private final IDomainService domainService;
    
    @GetMapping("/{id}")
    public DomainVO getById(@PathVariable Long id) {
        log.info("查询..., ID: {}", id);
        return domainService.getById(id);
    }
    
    @PostMapping
    public DomainVO create(@RequestBody DomainDTO dto) {
        log.info("创建...");
        return domainService.create(dto);
    }
}
```

## Type Conventions

- **Nullable references**: Always check with `AssertUtils.notNull()` before use
- **Collections**: Use `List<T>`, `IPage<T>` from MyBatis-Plus for pagination
- **Dates**: Use `java.time.LocalDateTime`, never `java.util.Date`
- **Decimals**: Use `java.math.BigDecimal` for monetary values
- **Enums**: Use for status/state (e.g., `SeckillOrderStatusEnum.PENDING_PAYMENT`)

## Documentation

- **Javadoc on public methods**: Brief description of what the method does
- **Inline comments**: Explain complex business logic or non-obvious decisions
- **Section comments**: Use `// ==================== Section Name ====================` to group related methods
- **Log messages**: Use Chinese for consistency with existing codebase; format: `verb + noun + context`

## Clean Architecture Rules

1. **Controllers are HTTP adapters**: Only handle routing and delegation to services
2. **Business logic in Services**: All validation, state management, database operations
3. **Centralized validation**: Use AssertUtils for all validations
4. **VO conversion in services**: Not in controllers
5. **One service per domain**: Keep services focused and cohesive
6. **Dependency injection via constructor or @Autowired**: Never instantiate dependencies manually

## Critical Development Constraints

### 1. Business Exception Handling
Always throw `BizException` paired with `BizErrorCode` enums. Never use generic `Exception` or `RuntimeException`:

```java
// ✅ CORRECT
AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);

// ❌ WRONG
if (order == null) {
    throw new Exception("Order not found");
}
```

### 2. Getting Current User Information
Use `AuthUserUtils` utility class to get current login user info. Never access SecurityContextHolder directly in business code:

```java
// ✅ CORRECT - Use AuthUserUtils
Long userId = AuthUserUtils.getUserId();
String username = AuthUserUtils.getUsername();
AuthUser authUser = AuthUserUtils.getAuthUser();
Long storeId = AuthUserUtils.getStoreId();

// ❌ WRONG - Direct SecurityContextHolder access
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

**Available AuthUserUtils Methods:**
- `AuthUserUtils.getAuthUser()` - Get current authenticated user as AuthUser object
- `AuthUserUtils.getUserId()` - Get current user ID
- `AuthUserUtils.getUsername()` - Get current username
- `AuthUserUtils.getStoreId()` - Get current user's store ID (for merchants)
- `AuthUserUtils.getRoles()` - Get current user's role list
- `AuthUserUtils.getAuthentication()` - Get raw authentication object (rarely needed)

### 3. Common Fields Auto-Fill Handler
Do NOT manually set common fields like `createTime`, `updateTime`, `createUser`, `updateUser`. These are automatically filled by `MyMetaObjectHandler`:

```java
// ✅ CORRECT - Fields auto-filled
SeckillOrder order = new SeckillOrder();
order.setProductId(100L);
order.setQuantity(5);
// createTime, updateTime, createUser, updateUser are auto-filled!
save(order);

// ❌ WRONG - Manually setting auto-filled fields
SeckillOrder order = new SeckillOrder();
order.setProductId(100L);
order.setQuantity(5);
order.setCreateTime(LocalDateTime.now());      // DON'T DO THIS
order.setCreateUser(AuthUserUtils.getUsername()); // DON'T DO THIS
save(order);
```

**Auto-Filled Fields** (defined in `CommonDO`):
- `createTime` - Filled on INSERT with current timestamp
- `updateTime` - Filled on INSERT and UPDATE with current timestamp
- `createUser` - Filled on INSERT with current username or "system"
- `updateUser` - Filled on INSERT and UPDATE with current username or "system"

Note: If no user is authenticated, "system" is used as the default user.

## Compilation & Debug

- **No compilation errors**: Always verify `mvn clean compile -DskipTests` succeeds
- **Warnings acceptable**: Lombok warnings about Builder patterns are normal
- **Target Java version**: Java 17 (set in pom.xml: `<maven.compiler.target>17</maven.compiler.target>`)
