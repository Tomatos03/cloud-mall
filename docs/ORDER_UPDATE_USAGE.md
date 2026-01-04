# 订单状态更新使用文档

## 概述

本文档说明如何使用重构后的订单状态更新方法，特别是在支付成功后更新订单状态的场景。

## 订单类型说明

系统支持三种订单类型：

- **PARENT（父订单）**：多店铺场景下的父订单，用于统一管理多个子订单
- **SUB（子订单）**：多店铺场景下的子订单，归属于某个父订单
- **NORMAL（普通订单）**：单店铺场景下的普通订单

## 方法说明

### 1. updateOrderStatus (根据订单ID更新)

用于前台用户操作，需要验证订单归属权限。

**方法签名：**
```java
boolean updateOrderStatus(UpdateOrderDTO updateOrderDTO)
```

**参数：**
```java
UpdateOrderDTO {
    Long orderId;    // 订单ID
    String status;   // 目标状态
}
```

**更新逻辑：**

| 订单类型 | 更新行为 |
|---------|---------|
| PARENT（父订单） | 更新父订单 + 级联更新所有子订单 |
| SUB（子订单） | 更新子订单 + 检查所有兄弟订单状态是否一致，如果一致则同步更新父订单 |
| NORMAL（普通订单） | 直接更新订单 |

**使用示例：**
```java
@Autowired
private IOrderService orderService;

// 用户取消订单
UpdateOrderDTO updateDTO = new UpdateOrderDTO();
updateDTO.setOrderId(123456L);
updateDTO.setStatus(OrderStatus.CANCELLED.getCode());

boolean success = orderService.updateOrderStatus(updateDTO);
```

---

### 2. updateOrderStatusByOrderNo (根据订单号更新) ⭐️ 推荐支付场景使用

用于支付成功后更新订单状态，**无需用户权限验证**，支持父子订单级联更新。

**方法签名：**
```java
boolean updateOrderStatusByOrderNo(String orderNo, OrderStatus status)
```

**参数：**
- `orderNo`: 订单号（父订单号或普通订单号）
- `status`: 目标状态枚举

**更新逻辑：**

| 订单号类型 | 更新行为 |
|-----------|---------|
| 父订单号（多店铺） | 更新父订单 + 级联更新所有子订单 |
| 普通订单号（单店铺） | 直接更新订单 |

**使用示例：**

#### 场景1：支付成功回调

```java
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {
    
    private final IOrderService orderService;
    
    /**
     * 处理支付成功回调
     */
    public void handlePaymentSuccess(String orderNo, String transactionId) {
        try {
            // 更新订单状态为已支付
            boolean success = orderService.updateOrderStatusByOrderNo(
                orderNo, 
                OrderStatus.PENDING_SHIPMENT  // 待发货
            );
            
            if (success) {
                log.info("订单支付成功，状态已更新, orderNo: {}, transactionId: {}", 
                         orderNo, transactionId);
                
                // TODO: 其他业务逻辑
                // - 发送支付成功通知
                // - 触发发货流程
                // - 记录支付日志
            } else {
                log.error("订单状态更新失败, orderNo: {}", orderNo);
            }
            
        } catch (BusinessException e) {
            log.error("支付回调处理失败, orderNo: {}, error: {}", orderNo, e.getMessage());
            throw e;
        }
    }
}
```

#### 场景2：订单超时取消

```java
@Service
@RequiredArgsConstructor
public class OrderTimeoutService {
    
    private final IOrderService orderService;
    
    /**
     * 取消超时未支付订单
     */
    public void cancelTimeoutOrders(String orderNo) {
        boolean success = orderService.updateOrderStatusByOrderNo(
            orderNo,
            OrderStatus.CANCELLED
        );
        
        if (success) {
            log.info("超时订单已取消, orderNo: {}", orderNo);
            // TODO: 恢复库存
        }
    }
}
```

#### 场景3：订单发货

```java
@Service
@RequiredArgsConstructor
public class ShipmentService {
    
    private final IOrderService orderService;
    
    /**
     * 订单发货
     */
    public void shipOrder(String orderNo, String trackingNumber) {
        boolean success = orderService.updateOrderStatusByOrderNo(
            orderNo,
            OrderStatus.SHIPPED
        );
        
        if (success) {
            log.info("订单已发货, orderNo: {}, trackingNumber: {}", 
                     orderNo, trackingNumber);
            // TODO: 发送发货通知
        }
    }
}
```

## 父子订单级联更新示例

### 多店铺场景（父子订单）

假设用户下单购买了来自2个店铺的商品：

```
父订单 P1234567890 (PARENT)
├── 子订单 1234567891 (SUB) - 店铺A
└── 子订单 1234567892 (SUB) - 店铺B
```

#### 支付成功后调用：

```java
// 使用父订单号更新
orderService.updateOrderStatusByOrderNo("P1234567890", OrderStatus.PENDING_SHIPMENT);
```

**执行结果：**
1. 父订单 `P1234567890` 状态更新为 `PENDING_SHIPMENT`（待发货）
2. 子订单 `1234567891` 状态更新为 `PENDING_SHIPMENT`（待发货）
3. 子订单 `1234567892` 状态更新为 `PENDING_SHIPMENT`（待发货）

#### 店铺A单独发货：

```java
// 商家操作：更新子订单状态
UpdateOrderDTO dto = new UpdateOrderDTO();
dto.setOrderId(1234567891L);  // 店铺A的子订单ID
dto.setStatus(OrderStatus.SHIPPED.getCode());

orderService.updateOrderStatus(dto);
```

**执行结果：**
1. 子订单 `1234567891` 状态更新为 `SHIPPED`（已发货）
2. 系统检测到另一个子订单 `1234567892` 状态仍为 `PENDING_SHIPMENT`
3. 父订单状态**不变**（仍为 `PENDING_SHIPMENT`）

#### 店铺B也完成发货：

```java
UpdateOrderDTO dto = new UpdateOrderDTO();
dto.setOrderId(1234567892L);  // 店铺B的子订单ID
dto.setStatus(OrderStatus.SHIPPED.getCode());

orderService.updateOrderStatus(dto);
```

**执行结果：**
1. 子订单 `1234567892` 状态更新为 `SHIPPED`（已发货）
2. 系统检测到**所有子订单状态一致**（都是 `SHIPPED`）
3. **自动同步更新**父订单 `P1234567890` 状态为 `SHIPPED`

---

### 单店铺场景（普通订单）

```
普通订单 1234567890 (NORMAL) - 店铺A
```

#### 支付成功后调用：

```java
// 使用普通订单号更新
orderService.updateOrderStatusByOrderNo("1234567890", OrderStatus.PENDING_SHIPMENT);
```

**执行结果：**
1. 普通订单 `1234567890` 状态更新为 `PENDING_SHIPMENT`（待发货）

## 订单状态流转

```
待支付 (PENDING_PAYMENT)
    ↓ [支付成功]
待发货 (PENDING_SHIPMENT)
    ↓ [商家发货]
已发货 (SHIPPED)
    ↓ [用户确认收货]
已完成 (COMPLETED)
    ↓ [用户评价]
已评价 (RATED)

待支付 (PENDING_PAYMENT)
    ↓ [超时/用户取消]
已取消 (CANCELLED)
```

## 注意事项

1. **权限验证**
   - `updateOrderStatus`: 需要验证订单归属，仅订单所有者可操作
   - `updateOrderStatusByOrderNo`: 无权限验证，适用于系统内部调用（如支付回调）

2. **事务处理**
   - 两个方法都添加了 `@Transactional` 注解，保证数据一致性
   - 父子订单的级联更新在同一事务中完成

3. **幂等性**
   - 重复调用相同状态不会报错
   - 建议在支付回调中添加幂等性控制

4. **日志记录**
   - 所有更新操作都有详细的日志记录
   - 便于问题排查和数据追踪

## 最佳实践

### ✅ 推荐做法

```java
// 支付成功回调：使用订单号更新
orderService.updateOrderStatusByOrderNo(orderNo, OrderStatus.PENDING_SHIPMENT);

// 用户取消订单：使用订单ID更新（需验证权限）
UpdateOrderDTO dto = new UpdateOrderDTO();
dto.setOrderId(orderId);
dto.setStatus(OrderStatus.CANCELLED.getCode());
orderService.updateOrderStatus(dto);
```

### ❌ 不推荐做法

```java
// 错误：支付回调中使用需要权限验证的方法
// 支付回调是系统级操作，无用户上下文
orderService.updateOrderStatus(dto);  // 可能因为用户上下文为空而失败

// 错误：手动查询并更新父子订单
// 应该使用封装好的方法，避免遗漏逻辑
Order order = orderService.getById(orderId);
order.setStatus(status);
orderService.updateById(order);  // 缺少级联更新逻辑
```

## 常见问题

### Q1: 支付成功后应该调用哪个方法？
**A:** 使用 `updateOrderStatusByOrderNo(orderNo, status)`，因为支付回调通常只有订单号，且不需要用户权限验证。

### Q2: 如何处理部分子订单发货的情况？
**A:** 使用 `updateOrderStatus` 更新单个子订单状态，系统会自动检测所有子订单状态，当全部一致时自动同步更新父订单。

### Q3: 订单状态更新失败怎么办？
**A:** 方法会抛出 `BusinessException`，调用方应该捕获异常并进行重试或记录日志。支付回调场景建议配置重试机制。

### Q4: 能否跳过某些状态直接更新？
**A:** 可以，但建议遵循状态流转规则。如果业务需要，可以在调用前添加状态验证逻辑。