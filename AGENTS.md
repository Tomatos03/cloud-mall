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
- **Enums**: Use for status/state with standardized implementation (see Enum Standards below)

## Enum Standards

All status/state enums must follow a standardized pattern for consistency and database compatibility. Enums use `Integer` codes for database storage while maintaining type safety through enum utilities.

### Enum Implementation Pattern

**Use this pattern as the standard for all status enums:**

```java
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 订单状态枚举
 * 
 * 定义订单的各种状态和对应的整数代码
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    /**
     * 待支付
     */
    CREATED(0, "待支付"),

    /**
     * 待发货
     */
    PAID(1, "待发货"),

    /**
     * 已完成
     */
    FINISHED(2, "已完成"),

    /**
     * 已取消
     */
    CANCELED(3, "已取消");

    private final int code;      // 数据库存储的整数代码
    private final String desc;   // 状态描述

    /**
     * 根据代码获取对应的枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws BizException 如果代码无效
     */
    public static OrderStatus of(int code) {
        return Arrays.stream(values())
                     .filter(status -> status.code == code)
                     .findFirst()
                     .orElseThrow(() -> new BizException(BizErrorCode.INVALID_ORDER_STATUS));
    }
}
```

### Key Rules for Enum Implementation

1. **Field Naming**: 
   - Use `code` (not `value`) as the field name for the integer code
   - Use `desc` for the status description

2. **Constructor**:
   - Use `@AllArgsConstructor` from Lombok to auto-generate constructor
   - Parameter order: `code` first, then `desc`

3. **Static Method**:
   - Implement `of(int code)` static method to convert code to enum
   - Return type matches the enum class
   - Throw `BizException` with appropriate `BizErrorCode` if code is invalid

4. **Lombok Annotations**:
   - Use `@Getter` to auto-generate getters
   - Use `@AllArgsConstructor` to auto-generate constructor

5. **Imports**:
   - Always import `com.onlineshop.framework.exception.BizException`
   - Always import `com.onlineshop.framework.common.enums.BizErrorCode`
   - Use `java.util.Arrays` for stream operations

### Entity Field Type Convention

When using enums in entity classes, **store the code as Integer, not the enum**:

```java
// ✅ CORRECT - Store integer code in database
@Data
@TableName("orders")
public class Order {
    private Long id;
    
    /**
     * 订单状态
     * 0=待支付, 1=待发货, 2=已完成, 3=已取消
     * 
     * 注意：使用Integer类型而不是枚举，以便数据库直接存储和查询
     * 在需要比较时，使用 OrderStatus.CREATED.getCode() 等方式获取对应的int值
     */
    private Integer status;
}

// ❌ WRONG - Do NOT use enum as field type
public class Order {
    private OrderStatus status;  // Won't work with database!
}
```

### Service Usage Pattern

When working with status codes in services:

```java
@Service
@Slf4j
public class OrderServiceImpl {
    
    @Override
    public void createOrder(OrderDTO dto) {
        log.info("创建订单");
        
        Order order = new Order();
        BeanUtils.copyProperties(dto, order);
        // Set status to initial code value
        order.setStatus(OrderStatus.CREATED.getCode());
        save(order);
    }
    
    @Override
    public void completeOrder(Long orderId) {
        log.info("完成订单，ID: {}", orderId);
        
        Order order = getById(orderId);
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        
        // Check status by comparing code values
        AssertUtils.isTrue(order.getStatus() != null && order.getStatus().equals(OrderStatus.PAID.getCode()),
                          BizErrorCode.INVALID_ORDER_STATUS);
        
        order.setStatus(OrderStatus.FINISHED.getCode());
        updateById(order);
    }
}
```

### Query Pattern with Enum Codes

When querying by status, use the enum's `getCode()` method:

```java
// ✅ CORRECT - Using enum code for queries
LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
    .eq(Order::getStatus, OrderStatus.CREATED.getCode());

// ❌ WRONG - Using enum directly
LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
    .eq(Order::getStatus, OrderStatus.CREATED);  // Type mismatch!
```

## Pagination Standards

### Pagination Parameter Pattern
All paginated list queries must use **structured parameter DTOs** instead of individual `pageNum`/`pageSize` parameters. This ensures consistency and maintainability across all modules.

### Parameter DTO Implementation
Create a domain-specific parameter DTO extending `PageParamsDTO`:

```java
// Base class (already exists in framework)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParamsDTO {
    private Integer page = 1;      // Current page (1-indexed)
    private Integer pageSize = 10; // Records per page
}

// Domain-specific DTO example
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderParamsDTO extends PageParamsDTO {
    private String orderNo;        // Optional: filter by order number
    private String status;         // Optional: filter by status
    private String orderType;      // Optional: filter by type
    private Long parentId;         // Optional: filter by parent order
}
```

**Key Rules:**
- ✅ Extend `PageParamsDTO` (located in `com.onlineshop.framework.common.entity`)
- ✅ Add domain-specific filter fields as needed
- ✅ Use `@Data` and `@EqualsAndHashCode(callSuper = true)` from Lombok
- ✅ Do NOT use `@AllArgsConstructor` and `@NoArgsConstructor` together (conflicts with callSuper)
- ✅ Place DTOs in `com.onlineshop.framework.models.[domain].dto` package

### Service Interface Pattern
Update service methods to accept parameter DTOs instead of individual pagination parameters:

```java
// ✅ CORRECT - Using parameter DTO
public interface IOrderService extends IService<Order> {
    /**
     * 分页查询订单列表
     *
     * @param params 订单查询参数（包含page、pageSize及其他筛选条件）
     * @return 订单分页数据
     */
    IPage<OrderVO> listOrders(OrderParamsDTO params);
}

// ❌ WRONG - Individual pagination parameters
public interface IOrderService extends IService<Order> {
    IPage<OrderVO> listOrders(Integer pageNum, Integer pageSize, String status);
}
```

### Service Implementation Pattern
Extract pagination info from parameter DTO in service methods:

```java
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    
    // ==================== Query Methods ====================
    
    @Override
    public IPage<OrderVO> listOrders(OrderParamsDTO params) {
        log.info("查询订单列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
        
        Page<Order> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .like(StringUtils.isNotBlank(params.getOrderNo()), Order::getOrderNo, params.getOrderNo())
                .eq(StringUtils.isNotBlank(params.getStatus()), Order::getStatus, params.getStatus());
        
        IPage<Order> result = this.page(page, wrapper);
        return result.convert(this::convertToVO);
    }
}
```

### Controller Endpoint Pattern
Use **GET with automatic parameter mapping** for list endpoints to receive parameter DTO:

```java
@RestController
@RequestMapping("/admin/orders")
@Slf4j
public class OrderController {
    
    @Autowired
    private IOrderService orderService;
    
    /**
     * 查询订单列表
     * GET /admin/orders/list
     *
     * @param params 订单查询参数（包含分页和筛选条件）
     * @return 订单分页数据
     */
    @GetMapping("/list")
    public IPage<OrderVO> listOrders(OrderParamsDTO params) {
        log.info("查询订单列表，页码: {}, 每页数量: {}", params.getPage(), params.getPageSize());
        return orderService.listOrders(params);
    }
    
    // ❌ WRONG - Using POST with @RequestBody for list queries
    // @PostMapping("/list")
    // public IPage<OrderVO> listOrders(@RequestBody OrderParamsDTO params)
}
```

### HTTP Request Examples

**GET Request:**
```bash
GET /api/admin/orders/list?page=1&pageSize=20&orderNo=ORD&status=PENDING
Content-Type: application/json
```

**Response:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "ORD20260101001",
        "status": "PENDING"
      }
    ],
    "total": 100,
    "pages": 5,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### Pagination Standards Summary

| Aspect | Standard |
|--------|----------|
| **Parameters** | Use domain-specific DTO extending `PageParamsDTO` |
| **HTTP Method** | GET for list endpoints |
| **Parameter Location** | Query string (automatically mapped to DTO fields) |
| **Endpoint Path** | `/list` suffix (e.g., `/admin/orders/list`) |
| **Field Names** | `page` (not `pageNum`), `pageSize` (not `size`) |
| **Default Values** | `page=1`, `pageSize=10` (in PageParamsDTO) |
| **Service Signature** | Accept parameter DTO, not individual parameters |



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
