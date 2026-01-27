# 订单超时取消与自动收货实现方案

## 概述

本方案实现了电商平台中订单超时取消和自动收货的完整功能，包括：

1. **消息补偿定时任务** - 兜底机制，确保超时订单最终一定会被关闭
2. **自动收货定时任务** - 到达T+N时间后自动确认收货
3. **日期时间工具类** - 日期时间操作的统一封装

## 核心设计原理

### 事务一致性保证

订单状态变更与消息发送采用以下策略确保一致性：

```
1. 订单状态更新与消息保存在同一事务中
2. 事务提交后异步发送消息（使用 TransactionSynchronization）
3. 消息发送失败时，定时任务进行补偿重试
```

### 兜底方案

定时任务作为兜底机制，确保：

- 即使消息队列发送失败，订单也能被关闭
- 每10分钟扫描一次过期未支付的订单
- 检查是否到达自动收货时间，逐个处理

## 文件说明

### 1. DateTimeUtil.java (工具类)

**路径**: `oneline-shop-framework/src/main/java/com/onlineshop/framework/utils/DateTimeUtil.java`

**主要方法**:

```java
// 判断是否已过期
boolean isExpired(LocalDateTime targetDateTime)

// 判断是否在时间范围内
boolean isInTimeRange(LocalDateTime start, LocalDateTime end)

// 计算时间差
long daysBetween(LocalDateTime start, LocalDateTime end)
long hoursBetween(LocalDateTime start, LocalDateTime end)
long minutesBetween(LocalDateTime start, LocalDateTime end)

// 判断是否在未来
boolean isInFuture(LocalDateTime targetDateTime)
```

### 2. Order.java (实体类修改)

**新增字段**:

```java
/**
 * 自动收货时间（到达此时间后自动确认收货）
 */
private LocalDateTime autoReceiveTime;
```

**使用场景**: 
- 商家发货后，系统自动计算 `autoReceiveTime = 发货时间 + N天`
- 定时任务检查该字段是否已过期

### 3. OrderCloseMessageCompensationTask.java (消息补偿定时任务)

**路径**: `oneline-shop-framework/src/main/java/com/onlineshop/framework/task/order/OrderCloseMessageCompensationTask.java`

**执行频率**: 每10分钟执行一次 (`0 */10 * * * ?`)

**工作流程**:

```
1. 查询所有过期未支付的订单 (status = CREATED && expireTime < now)
2. 重新发送这些订单的关闭消息到 RocketMQ
3. 消息发送失败时，下一个周期继续重试
```

**关键代码**:

```java
@Scheduled(cron = "0 */10 * * * ?")
public void compensateFailedCloseMessages() {
    List<Order> failedOrders = orderService.listFailedCloseOrders();
    if (!failedOrders.isEmpty()) {
        IProducer<Order> closeProducer = orderProducerFactory
            .getProducer(BizType.ORDER_TIMEOUT_CLOSE);
        closeProducer.sendAfterTransactionCommit(failedOrders);
    }
}
```

### 4. OrderAutoReceiveTask.java (自动收货定时任务)

**路径**: `oneline-shop-framework/src/main/java/com/onlineshop/framework/task/order/OrderAutoReceiveTask.java`

**执行频率**: 每天凌晨2点执行一次 (`0 0 2 * * ?`)

**工作流程**:

```
1. 查询所有待收货的订单 (status = SHIPPED)
2. 检查 autoReceiveTime 是否已过期
3. 调用 autoReceiveOrder() 自动确认收货
4. 失败的订单下一个周期继续处理
```

**关键代码**:

```java
@Scheduled(cron = "0 0 2 * * ?")
public void autoReceiveOrders() {
    List<Order> shippedOrders = orderService
        .lambdaQuery()
        .eq(Order::getStatus, "SHIPPED")
        .isNotNull(Order::getAutoReceiveTime)
        .list();
    
    for (Order order : shippedOrders) {
        if (DateTimeUtil.isExpired(order.getAutoReceiveTime())) {
            orderService.autoReceiveOrder(order);
        }
    }
}
```

### 5. IOrderService 接口修改

**新增方法**:

```java
/**
 * 查询关闭失败的订单
 * @return 失败订单列表
 */
List<Order> listFailedCloseOrders();

/**
 * 自动收货订单
 * @param order 订单对象
 * @return 是否成功
 */
boolean autoReceiveOrder(Order order);
```

### 6. OrderService 实现类修改

**新增实现**:

```java
@Override
public List<Order> listFailedCloseOrders() {
    return lambdaQuery()
        .eq(Order::getStatus, OrderStatus.CREATED.getCode())
        .lt(Order::getExpireTime, LocalDateTime.now())
        .list();
}

@Override
@Transactional(rollbackFor = Exception.class)
public boolean autoReceiveOrder(Order order) {
    // 1. 校验订单存在
    // 2. 校验订单状态为 SHIPPED
    // 3. 校验自动收货时间已过期
    // 4. 更新订单状态为 FINISHED
    return syncUpdateOrderStatus(currentOrder, OrderStatus.FINISHED);
}
```

## 使用场景说明

### 场景1: 订单超时取消

```
时间轴：
2026-01-05 21:00  → 用户下单，订单进入 CREATED 状态，expireTime = 21:30
2026-01-05 21:25  → 用户未支付
2026-01-05 21:35  → 定时任务执行，发现订单已过期
              ↓
         发送关闭消息到 MQ
              ↓
2026-01-05 21:36  → MQ 消费者接收到消息，关闭订单
              ↓
         订单状态变更为 CLOSED，库存释放
```

**如果消息发送失败**:

```
2026-01-05 21:45  → 下一个周期定时任务执行
              ↓
         扫描到该订单仍为 CREATED 且已过期
              ↓
         重新发送关闭消息 ← 兜底补偿
```

### 场景2: 自动收货

```
时间轴：
2026-01-05 22:00  → 商家发货，订单进入 SHIPPED 状态
                    autoReceiveTime = 2026-01-19 23:59 (14天后)
2026-01-19 02:00  → 每天凌晨2点定时任务执行
              ↓
         检查订单是否到达 autoReceiveTime
              ↓
2026-01-20 02:00  → 定时任务执行，发现 autoReceiveTime 已过期
              ↓
         更新订单状态为 FINISHED，不再需要用户确认
```

## 数据库字段要求

### orders 表需要包含:

```sql
-- 原有字段
ALTER TABLE orders ADD COLUMN expire_time DATETIME COMMENT '订单过期时间（待支付状态下）';

-- 新增字段（如果数据库中不存在）
ALTER TABLE orders ADD COLUMN auto_receive_time DATETIME COMMENT '自动收货时间（发货后N天）';
```

## 配置说明

### 启用定时任务

定时任务默认自动启用，需要在启动类上添加注解：

```java
@SpringBootApplication
@EnableScheduling  // 启用定时任务
public class OnlineShopManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineShopManagerApplication.class, args);
    }
}
```

### 调整执行频率

修改 `@Scheduled` 注解的 cron 表达式：

```java
// 每5分钟执行一次
@Scheduled(cron = "0 */5 * * * ?")

// 每小时执行一次
@Scheduled(cron = "0 0 * * * ?")

// 每天凌晨1点执行
@Scheduled(cron = "0 0 1 * * ?")
```

## 监控与告警

### 关键监控指标

1. **超时订单数量**: 定期监控过期未支付的订单数
2. **消息补偿次数**: 监控定时任务每次补偿的订单数
3. **自动收货数量**: 监控每天自动收货的订单数
4. **异常日志**: 监控定时任务执行中的异常

### 日志查看

```bash
# 查看消息补偿日志
grep "订单关闭消息补偿" application.log

# 查看自动收货日志
grep "订单自动收货" application.log

# 查看定时任务异常
grep "定时任务执行异常" application.log
```

## 性能优化建议

1. **批量处理**: 定时任务一次性处理多个订单，减少数据库查询次数
2. **索引优化**: 在 `status`, `expireTime`, `autoReceiveTime` 字段上建立索引
3. **分片处理**: 如果订单数量很大，可以对定时任务进行分片处理

```sql
CREATE INDEX idx_orders_status_expiretime 
ON orders(status, expire_time);

CREATE INDEX idx_orders_status_autoreceivetime 
ON orders(status, auto_receive_time);
```

## 常见问题

### Q1: 为什么要用定时任务兜底？

A: 消息队列虽然可靠性高，但在极端情况下可能丢失消息。定时任务作为兜底，确保即使消息丢失，订单也能最终被处理。

### Q2: autoReceiveTime 如何计算？

A: 商家发货时，系统自动计算：
```java
autoReceiveTime = shipTime + 14天  // 根据业务规则调整
```

### Q3: 能否手动取消自动收货？

A: 可以，用户在 autoReceiveTime 之前调用 `finishOrder()` 方法手动确认收货即可。自动收货只对未确认的订单生效。

### Q4: 定时任务冲突如何处理？

A: 单机部署无需处理。如果是分布式部署，建议使用分布式锁或引入 XXL-Job 框架进行协调。

## 扩展方向

### 未来升级建议

1. **分布式部署** → 使用 XXL-Job 框架避免重复执行
2. **多种规则** → 支持不同商品类型的不同自动收货时间
3. **用户提醒** → 收货前向用户发送提醒通知
4. **数据分析** → 统计自动收货率、超时订单率等指标

---

**最后修改日期**: 2026-01-27  
**作者**: Tomatos
