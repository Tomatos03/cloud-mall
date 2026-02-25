# Seckill Activity Audit System - Quick Reference Card

**Last Updated**: 2026-02-26

---

## Critical Fields & Validation Rules

### SeckillActivityAuditRequest (What merchant submits)
```
productId:    Long      ✓ Required - Product being promoted
startTime:    DateTime  ✓ Required - Must be 24+ hours from now
endTime:      DateTime  ✓ Required - Must be exactly 1 hour after start
seckillPrice: BigDecimal ✓ Required - Must be < original price
stock:        Integer   ✓ Required - Must be ≤ product stock
type:         String    ✓ "SECKILL_ACTIVITY"
applicantId:  Long      ✓ Merchant ID
```

### SeckillActivity (Database entity)
```
Automatically created when audit is SUBMITTED
Contains: id, productId, startTime, endTime, seckillPrice, stock
Audit status: Stored in Audit table, NOT here
```

### SeckillGoods (To be created on APPROVAL)
```
Links: SeckillActivity + Audit + Product + Merchant
Fields: id, activityId, auditId, productId, merchantId, seckillPrice, stock, soldCount
```

---

## Validation Checklist (validateRequest Implementation)

- [ ] productId is not null
- [ ] startTime is not null
- [ ] endTime is not null  
- [ ] seckillPrice is not null
- [ ] stock is not null
- [ ] startTime < endTime
- [ ] startTime is 24+ hours from now
- [ ] Duration (endTime - startTime) = exactly 1 hour
- [ ] Product exists (goodsService.getById)
- [ ] Product is active (product.isActive)
- [ ] seckillPrice < product.originalPrice
- [ ] stock ≤ product.totalStock
- [ ] Product not already in pending/approved seckill

---

## Key Services & Methods

### SeckillActivityService (extends IService<SeckillActivity>)
```java
save(entity)              // Create
getById(id)              // Get by ID
updateById(entity)       // Update
removeById(id)           // Delete
list()                   // Get all
list(wrapper)            // Query with conditions
page(page, wrapper)      // Paginated query
count()                  // Count records
```

### IAuditService (audit tracking)
```java
submitAudit(DTO)         // Submit audit
pageQuery(params)        // Search audits
getAuditById(id)         // Get details
queryAuditStatus(type, targetId)      // Get current status
queryLatestAudit(type, targetId)      // Get newest record
withdrawAudit(id)        // Cancel submission
```

### SeckillGoodsService (TO CREATE)
```java
save(goods)                             // Create
getByActivityAndProduct(actId, prodId) // Find goods
removeByActivityId(activityId)         // Delete all in activity
```

---

## Audit Status Lifecycle

```
USER SUBMITS
    ↓
validateRequest() checks business rules
    ↓
createAuditTarget() creates SeckillActivity in DB
    ↓
saveAuditRecord() creates Audit record (PENDING status)
    ↓
[Admin reviews in audit system]
    ↓
┌─────────────────────┬──────────────────────┐
APPROVED              REJECTED
    ↓                     ↓
afterApprove()        afterReject()
Create SeckillGoods   Optional cleanup
    ↓                     ↓
Activity ACTIVE       Activity INACTIVE
Can participate       Can be resubmitted
```

---

## Common Error Scenarios

| Scenario | Error Code | Fix |
|----------|-----------|-----|
| Product not found | PRODUCT_NOT_FOUND | Check productId exists |
| Price too high | SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL | Lower seckill price |
| Stock too much | SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK | Reduce stock quantity |
| Not 24 hours advance | ACTIVITY_MUST_ADVANCE_24_HOURS | Choose later start time |
| Duration not 1 hour | ACTIVITY_DURATION_MUST_BE_ONE_HOUR | Fix end time |
| Product already in seckill | PRODUCT_ALREADY_IN_SECKILL | Choose different product |

---

## File Locations (Quick Copy-Paste)

**Create these files:**
```
/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/entity/SeckillGoods.java
/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillGoodsService.java
/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillGoodsServiceImpl.java
/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/mapper/SeckillGoodsMapper.java
```

**Modify these files:**
```
/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/impl/SeckillActivityAuditor.java
  - Add SeckillGoodsService injection
  - Implement validateRequest()
  - Implement afterApprove()
```

**Database migration:**
```sql
CREATE TABLE seckill_goods (
    id              bigint auto_increment primary key,
    activity_id     bigint not null,
    audit_id        bigint not null,
    product_id      bigint not null,
    merchant_id     bigint not null,
    seckill_price   decimal(10, 2) not null,
    stock           int not null,
    sold_count      int default 0,
    create_time     datetime default current_timestamp(),
    update_time     datetime default current_timestamp() on update current_timestamp(),
    unique key uk_activity_product (activity_id, product_id),
    index idx_merchant (merchant_id),
    foreign key (activity_id) references seckill_activity(id)
) comment '秒杀商品表';
```

---

## Code Template: validateRequest()

```java
@Override
protected void validateRequest(SeckillActivityAuditRequest request) {
    // Field validation
    AssertUtils.notNull(request.getProductId(), BizErrorCode.PRODUCT_ID_REQUIRED);
    AssertUtils.notNull(request.getStartTime(), BizErrorCode.START_TIME_REQUIRED);
    AssertUtils.notNull(request.getEndTime(), BizErrorCode.END_TIME_REQUIRED);
    AssertUtils.notNull(request.getSeckillPrice(), BizErrorCode.PRICE_REQUIRED);
    AssertUtils.notNull(request.getStock(), BizErrorCode.STOCK_REQUIRED);
    
    // Time validation
    AssertUtils.isTrue(request.getStartTime().isBefore(request.getEndTime()), 
        BizErrorCode.INVALID_TIME_RANGE);
    LocalDateTime now = LocalDateTime.now();
    AssertUtils.isTrue(request.getStartTime().isAfter(now.plusHours(24)), 
        BizErrorCode.ACTIVITY_MUST_ADVANCE_24_HOURS);
    Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
    AssertUtils.isTrue(duration.equals(Duration.ofHours(1)), 
        BizErrorCode.ACTIVITY_DURATION_MUST_BE_ONE_HOUR);
    
    // Product validation
    Goods product = goodsService.getById(request.getProductId());
    AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);
    AssertUtils.isTrue(product.isActive(), BizErrorCode.PRODUCT_NOT_ACTIVE);
    
    // Price & Stock validation
    AssertUtils.isTrue(request.getSeckillPrice().compareTo(product.getPrice()) < 0, 
        BizErrorCode.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL);
    AssertUtils.isTrue(request.getStock() <= product.getStock(), 
        BizErrorCode.SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK);
    
    // Uniqueness check
    AuditStatus status = auditService.queryAuditStatus(AuditType.SECKILL_ACTIVITY, 
        request.getProductId());
    if (status != null && (status == AuditStatus.PENDING || status == AuditStatus.APPROVED)) {
        throw new BizException(BizErrorCode.PRODUCT_ALREADY_IN_SECKILL);
    }
}
```

---

## Code Template: afterApprove()

```java
@Override
protected void afterApprove(Long targetId) {
    // Get the seckill activity that was created during submission
    SeckillActivity activity = seckillActivityService.getById(targetId);
    AssertUtils.notNull(activity, BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
    
    // Get the audit record (just approved)
    Audit audit = auditService.queryLatestAudit(AuditType.SECKILL_ACTIVITY, targetId);
    AssertUtils.notNull(audit, BizErrorCode.AUDIT_LOG_NOT_EXISTS);
    
    // Get product info (for merchant ID)
    Goods product = goodsService.getById(activity.getProductId());
    AssertUtils.notNull(product, BizErrorCode.PRODUCT_NOT_FOUND);
    
    // Create seckill goods record linking everything together
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

---

## Remember

1. **Audit status is NOT in SeckillActivity**
   - Always check Audit table for status
   - Use: `auditService.queryAuditStatus(AuditType.SECKILL_ACTIVITY, activityId)`

2. **SeckillActivity created on SUBMIT, not APPROVE**
   - Enables validation against real data
   - Approval just updates Audit status

3. **SeckillGoods created on APPROVE**
   - When audit transitions to APPROVED status
   - In afterApprove() callback
   - Links activity + audit + product + merchant

4. **All times use LocalDateTime**
   - No java.util.Date
   - Use LocalDateTime.now()
   - Duration for time calculations

5. **Transactions handled by AbstractAuditor**
   - submitAudit() wrapped in transaction
   - handleDecision() wrapped in transaction
   - Don't add @Transactional yourself

---

## Testing Checklist

- [ ] validateRequest rejects null fields
- [ ] validateRequest rejects non-24hr advance
- [ ] validateRequest rejects non-1hr duration
- [ ] validateRequest rejects invalid price (>=original)
- [ ] validateRequest rejects invalid stock (>product stock)
- [ ] validateRequest rejects product not found
- [ ] validateRequest rejects product already in audit
- [ ] createAuditTarget saves SeckillActivity
- [ ] afterApprove creates SeckillGoods
- [ ] afterApprove sets all fields correctly
- [ ] Database migration creates table
- [ ] Unique constraint enforced
- [ ] Foreign keys valid

---

## Implementation Time Estimate

| Task | Hours |
|------|-------|
| Entity creation | 1 |
| Service creation | 1 |
| validateRequest() | 2-3 |
| afterApprove() | 1.5 |
| Database migration | 0.5 |
| Error codes | 0.5 |
| **Total** | **6.5-7.5** |

---

**For detailed information, see:**
- SECKILL_AUDIT_REPORT.md - Complete analysis
- IMPLEMENTATION_CHECKLIST.md - Step-by-step guide
- /docs/seckill_design.md - Original specifications
