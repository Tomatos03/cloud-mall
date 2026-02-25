# Seckill Activity Audit System - Implementation Checklist

**Date**: 2026-02-26  
**Status**: Partially Implemented (Core framework ready, Business logic pending)

---

## Current Implementation Status

### ✅ COMPLETED
- [x] `SeckillActivityAuditRequest` entity - Base audit request with required fields
- [x] `SeckillActivity` entity - Core seckill activity model
- [x] `SeckillActivityService` & `SeckillActivityServiceImpl` - Service layer with CRUD
- [x] `SeckillActivityMapper` - MyBatis Plus mapper
- [x] `SeckillActivityAuditor` class - Template method implementation
- [x] `AbstractAuditor` - Audit workflow framework
- [x] Audit infrastructure - `IAuditService`, `Audit` entity, `AuditRequest` base class
- [x] Enums - `AuditType`, `AuditStatus`, `SeckillStatusEnum`
- [x] Manager class - `SeckillManager` for stock and order handling
- [x] Design documentation - `seckill_design.md` with complete specifications

### ⚠️ PARTIAL IMPLEMENTATION
- [ ] `SeckillActivityAuditor.validateRequest()` - Empty, needs business logic
- [ ] `SeckillActivityAuditor.afterApprove()` - Empty, needs SeckillGoods creation
- [ ] `SeckillActivityAuditor.afterReject()` - Empty, optional cleanup logic

### ❌ NOT IMPLEMENTED
- [ ] `SeckillGoods` entity - Designed but not coded
- [ ] `SeckillGoodsService` interface - Not created
- [ ] `SeckillGoodsServiceImpl` - Not created
- [ ] `SeckillGoodsMapper` - Not created
- [ ] Database migration for `seckill_goods` table

---

## High Priority TODOs

### 1. Implement validateRequest() in SeckillActivityAuditor

**File**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/impl/SeckillActivityAuditor.java`

**Current Code**:
```java
@Override
protected void validateRequest(SeckillActivityAuditRequest request) {
}
```

**Required Implementation**:

```java
@Override
protected void validateRequest(SeckillActivityAuditRequest request) {
    // 1. Validate basic fields exist
    AssertUtils.notNull(request.getProductId(), BizErrorCode.PRODUCT_ID_REQUIRED);
    AssertUtils.notNull(request.getStartTime(), BizErrorCode.START_TIME_REQUIRED);
    AssertUtils.notNull(request.getEndTime(), BizErrorCode.END_TIME_REQUIRED);
    AssertUtils.notNull(request.getSeckillPrice(), BizErrorCode.PRICE_REQUIRED);
    AssertUtils.notNull(request.getStock(), BizErrorCode.STOCK_REQUIRED);
    
    // 2. Validate time constraints
    AssertUtils.isTrue(request.getStartTime().isBefore(request.getEndTime()), 
        BizErrorCode.INVALID_TIME_RANGE); // Start must be before end
    
    LocalDateTime now = LocalDateTime.now();
    AssertUtils.isTrue(request.getStartTime().isAfter(now.plusHours(24)), 
        BizErrorCode.ACTIVITY_MUST_ADVANCE_24_HOURS); // At least 24 hours advance
    
    // 3. Validate activity duration (fixed 1 hour per design)
    Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
    AssertUtils.isTrue(duration.equals(Duration.ofHours(1)), 
        BizErrorCode.ACTIVITY_DURATION_MUST_BE_ONE_HOUR);
    
    // 4. Validate product exists and get its details
    Goods product = goodsService.getById(request.getProductId());
    AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);
    AssertUtils.isTrue(product.isActive(), BizErrorCode.PRODUCT_NOT_ACTIVE);
    
    // 5. Validate price constraint: seckillPrice < originalPrice
    AssertUtils.isTrue(request.getSeckillPrice().compareTo(product.getPrice()) < 0, 
        BizErrorCode.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL);
    
    // 6. Validate stock constraint: seckillStock <= productStock
    AssertUtils.isTrue(request.getStock() <= product.getStock(), 
        BizErrorCode.SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK);
    
    // 7. Check uniqueness: Product can only have one active/pending seckill activity
    AuditStatus existingStatus = auditService.queryAuditStatus(AuditType.SECKILL_ACTIVITY, request.getProductId());
    if (existingStatus != null && 
        (existingStatus == AuditStatus.PENDING || existingStatus == AuditStatus.APPROVED)) {
        throw new BizException(BizErrorCode.PRODUCT_ALREADY_IN_SECKILL);
    }
}
```

**Estimated Time**: 2-3 hours (need to verify all error codes exist)

---

### 2. Create SeckillGoods Entity

**Directory**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/entity/`

**File**: `SeckillGoods.java`

**Template**:

```java
package com.onlineshop.framework.models.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品表
 * 在审核通过时创建，链接秒杀活动、商家、商品
 */
@Data
@TableName("seckill_goods")
public class SeckillGoods implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID（外键）
     */
    private Long activityId;

    /**
     * 审核记录ID（外键，关联审核表）
     */
    private Long auditId;

    /**
     * 商品ID（外键）
     */
    private Long productId;

    /**
     * 商家ID（外键）
     */
    private Long merchantId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 已售数量
     */
    private Integer soldCount = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
```

**Estimated Time**: 1 hour

---

### 3. Create SeckillGoodsService Interface

**File**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillGoodsService.java`

**Template**:

```java
package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;

/**
 * 秒杀商品服务接口
 */
public interface SeckillGoodsService extends IService<SeckillGoods> {
    
    /**
     * 根据活动ID和商品ID查询秒杀商品
     * @param activityId 活动ID
     * @param productId 商品ID
     * @return 秒杀商品
     */
    SeckillGoods getByActivityAndProduct(Long activityId, Long productId);
    
    /**
     * 删除活动的所有秒杀商品
     * @param activityId 活动ID
     */
    void removeByActivityId(Long activityId);
}
```

**Estimated Time**: 30 minutes

---

### 4. Create SeckillGoodsServiceImpl

**File**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillGoodsServiceImpl.java`

**Template**:

```java
package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import com.onlineshop.framework.models.seckill.mapper.SeckillGoodsMapper;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品服务实现
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {
    
    @Override
    public SeckillGoods getByActivityAndProduct(Long activityId, Long productId) {
        QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId)
               .eq("product_id", productId);
        return getOne(wrapper);
    }
    
    @Override
    public void removeByActivityId(Long activityId) {
        QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        remove(wrapper);
    }
}
```

**Estimated Time**: 30 minutes

---

### 5. Create SeckillGoodsMapper

**File**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/mapper/SeckillGoodsMapper.java`

**Template**:

```java
package com.onlineshop.framework.models.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.seckill.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀商品 Mapper
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
}
```

**Estimated Time**: 15 minutes

---

### 6. Update SeckillActivityAuditor

**File**: `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/impl/SeckillActivityAuditor.java`

**Changes**:

1. Add `SeckillGoodsService` to constructor injection
2. Implement `afterApprove()` method:

```java
private final SeckillGoodsService seckillGoodsService;

public SeckillActivityAuditor(
        IAuditService auditService,
        SeckillActivityService seckillActivityService,
        IGoodsService goodsService,
        SeckillGoodsService seckillGoodsService  // NEW
) {
    this.auditService = auditService;
    this.seckillActivityService = seckillActivityService;
    this.goodsService = goodsService;
    this.seckillGoodsService = seckillGoodsService;  // NEW
}

@Override
protected void afterApprove(Long targetId) {
    // 审核通过，创建秒杀商品记录，建立SeckillActivity与Audit的关联
    SeckillActivity activity = seckillActivityService.getById(targetId);
    AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
    
    // 获取最新的审核记录
    Audit audit = auditService.queryLatestAudit(AuditType.SECKILL_ACTIVITY, targetId);
    AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);
    
    // 获取商品信息（包括商家ID）
    Goods product = goodsService.getById(activity.getProductId());
    AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);
    
    // 创建秒杀商品记录
    SeckillGoods goods = new SeckillGoods();
    goods.setActivityId(activity.getId());
    goods.setAuditId(audit.getId());
    goods.setProductId(activity.getProductId());
    goods.setMerchantId(product.getMerchantId());
    goods.setSeckillPrice(activity.getSeckillPrice());
    goods.setStock(activity.getStock());
    goods.setSoldCount(0);
    goods.setCreateTime(LocalDateTime.now());
    goods.setUpdateTime(LocalDateTime.now());
    
    seckillGoodsService.save(goods);
}
```

**Estimated Time**: 1.5 hours (including testing)

---

## Medium Priority TODOs

### 7. Implement afterReject() (Optional)

**Current Status**: Empty  
**Purpose**: Handle cleanup when audit is rejected

**Possible Implementations**:
1. Delete the created `SeckillActivity` record
2. Send notification to merchant
3. Free up product for other activities

**Example**:

```java
@Override
protected void afterReject(Long targetId, String reason) {
    // Option 1: Delete the SeckillActivity if not needed
    // seckillActivityService.removeById(targetId);
    
    // Option 2: Send notification to merchant (if implemented)
    // notificationService.sendRejectionNotice(targetId, reason);
}
```

**Estimated Time**: 1-2 hours (depends on notification implementation)

---

### 8. Database Migration for seckill_goods Table

**File**: Create migration file in your DB migration folder

**SQL** (from design doc):

```sql
CREATE TABLE seckill_goods (
    id              bigint auto_increment primary key,
    activity_id     bigint not null comment '活动ID（外键）',
    audit_id        bigint not null comment '审核记录ID（外键，关联审核表）',
    product_id      bigint not null comment '商品ID（外键）',
    merchant_id     bigint not null comment '商家ID（外键）',
    seckill_price   decimal(10, 2) not null comment '秒杀价格',
    stock           int not null comment '秒杀库存',
    sold_count      int default 0 comment '已售数量',
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    unique key uk_activity_product (activity_id, product_id),
    index idx_merchant (merchant_id),
    foreign key (activity_id) references seckill_activity(id)
) comment '秒杀商品表';
```

**Estimated Time**: 30 minutes

---

### 9. Verify/Create Missing Error Codes

**File**: Check if `BizErrorCode` enum has all required codes

**Required Codes**:
- `PRODUCT_ID_REQUIRED`
- `START_TIME_REQUIRED`
- `END_TIME_REQUIRED`
- `PRICE_REQUIRED`
- `STOCK_REQUIRED`
- `INVALID_TIME_RANGE`
- `ACTIVITY_MUST_ADVANCE_24_HOURS`
- `ACTIVITY_DURATION_MUST_BE_ONE_HOUR`
- `PRODUCT_NOT_FOUND`
- `PRODUCT_NOT_ACTIVE`
- `SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL`
- `SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK`
- `PRODUCT_ALREADY_IN_SECKILL`
- `SECKILL_ACTIVITY_NOT_EXIST`

**Estimated Time**: 30 minutes - 1 hour

---

## Low Priority TODOs

### 10. Add DTO/VO for SeckillGoods

Similar to `SeckillActivityDTO` and `SeckillActivityVO`, create:
- `SeckillGoodsDTO` - For API input/output
- `SeckillGoodsVO` - For view presentation

**Estimated Time**: 1 hour

---

### 11. Add Custom Query Methods

Enhance services with business-specific queries:
- `getAllSeckillGoodsByActivity()` - Get all goods in an activity
- `getSeckillGoodsByMerchant()` - Get merchant's seckill goods
- `queryByStatus()` - Query by seckill status
- `calculateStats()` - Get sales statistics

**Estimated Time**: 2-3 hours

---

### 12. Add Logging & Monitoring

- Add detailed logging in all validation methods
- Add metrics collection for audit submissions/approvals
- Add alerting for validation failures

**Estimated Time**: 2-3 hours

---

## Testing TODOs

### 13. Unit Tests

Create test classes for:
- `SeckillActivityAuditorTest` - Test validation logic
- `SeckillGoodsServiceTest` - Test CRUD operations
- `SeckillActivityValidationTest` - Test business rules

**Estimated Time**: 4-5 hours

---

### 14. Integration Tests

- End-to-end audit submission flow
- Approval flow with SeckillGoods creation
- Rejection flow with cleanup
- Validation rule enforcement

**Estimated Time**: 4-5 hours

---

### 15. Database Migration Testing

- Verify seckill_goods table creation
- Test constraints (unique, foreign keys)
- Test indexes

**Estimated Time**: 1-2 hours

---

## Summary Timeline

| Phase | Tasks | Est. Hours | Priority |
|-------|-------|-----------|----------|
| Core Implementation | 1-6 | 6-7 | HIGH |
| Database & Config | 8-9 | 1.5 | HIGH |
| Business Logic | 7, 10-12 | 6-8 | MEDIUM |
| Testing | 13-15 | 10-12 | MEDIUM |
| **Total** | | **23.5-27.5** | - |

---

## Implementation Order (Recommended)

1. **Phase 1 (Day 1)**: Core entities
   - Create `SeckillGoods` entity
   - Create `SeckillGoodsService` interface & implementation
   - Create `SeckillGoodsMapper`
   - Database migration

2. **Phase 2 (Day 1-2)**: Integration
   - Update `SeckillActivityAuditor` with new service injection
   - Implement `validateRequest()` with all business rules
   - Implement `afterApprove()` with SeckillGoods creation
   - Verify error codes

3. **Phase 3 (Day 2)**: Polish
   - Add DTOs/VOs
   - Add custom query methods
   - Add logging

4. **Phase 4 (Day 3)**: Testing
   - Unit tests
   - Integration tests
   - Database migration testing

---

## Verification Checklist

After implementation, verify:

- [ ] All entities compile without errors
- [ ] All services are properly injected
- [ ] All validation rules from design doc are implemented
- [ ] Database migration runs successfully
- [ ] SeckillGoods table created with all constraints
- [ ] Audit submission creates SeckillActivity
- [ ] Audit approval creates SeckillGoods
- [ ] All foreign key relationships work
- [ ] Error codes match implementation
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] No null pointer exceptions
- [ ] Transaction handling is correct
- [ ] Logging is comprehensive

---

## Notes

1. **Audit Status Management**: Remember that audit status is stored in the `Audit` table, NOT in `SeckillActivity` or `SeckillGoods`. Use `IAuditService.queryAuditStatus()` to check status.

2. **Transaction Handling**: All database operations are wrapped in transactions by `AbstractAuditor.submitAudit()` and `handleDecision()`. Ensure no additional transaction boundaries are added.

3. **Product Reference**: `IGoodsService` is used to validate product info. Ensure it has methods like:
   - `getById(Long productId)`
   - `isActive()` check on product
   - Access to product's merchant ID, original price, and stock

4. **Error Handling**: Use `AssertUtils` for validation and throw `BizException` with appropriate `BizErrorCode` values.

5. **Timestamps**: Use `LocalDateTime.now()` for all timestamp assignments.

---

**Report Generated**: 2026-02-26  
**Scope**: Seckill Activity Audit System - Complete Implementation Roadmap
