# Seckill Activity Audit System - Comprehensive Report

Generated: 2026-02-26

---

## 1. SECKILL ACTIVITY AUDIT REQUEST ENTITY

### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/domain/SeckillActivityAuditRequest.java`

### Class Structure
- **Package**: `com.onlineshop.framework.models.audit.domain`
- **Extends**: `AuditRequest` (base class)
- **Annotations**: `@Data`, `@EqualsAndHashCode(callSuper = true)`

### Required Fields for Submission

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `productId` | Long | Yes | Product ID (Foreign Key) |
| `startTime` | LocalDateTime | Yes | Seckill start time |
| `endTime` | LocalDateTime | Yes | Seckill end time |
| `seckillPrice` | BigDecimal | Yes | Seckill price |
| `stock` | Integer | Yes | Seckill inventory |
| `type` | String | Yes | Audit type (inherited from AuditRequest) - "SECKILL_ACTIVITY" |
| `applicantId` | Long | Yes | Applicant ID (inherited from AuditRequest) |
| `applicantName` | String | Yes | Applicant name (inherited from AuditRequest) |
| `targetId` | Long | Optional | Target ID (set during submission) |

### Validation Rules (from design document)
- **Time Rules**: Activity must be created at least 24 hours in advance
- **Price Rule**: Seckill price < Original product price
- **Stock Rule**: Seckill stock ≤ Product total stock
- **Uniqueness Rule**: One product can only participate in one activity (pending/approved)
- **Rejection Release Rule**: After rejection, product can apply for other activities

---

## 2. SECKILL ACTIVITY ENTITY

### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/entity/SeckillActivity.java`

### Class Structure
- **Package**: `com.onlineshop.framework.models.seckill.entity`
- **Database Table**: `seckill_activity`
- **Annotations**: `@Data`, `@TableName("seckill_activity")`
- **Serialization**: `implements Serializable` (serialVersionUID = 1L)

### Entity Fields

| Field | Type | DB Type | Nullable | Default | Notes |
|-------|------|---------|----------|---------|-------|
| `id` | Long | BIGINT | No | AUTO_INCREMENT | Primary key |
| `productId` | Long | BIGINT | No | - | Foreign key to product |
| `startTime` | LocalDateTime | DATETIME | No | - | Seckill start time |
| `endTime` | LocalDateTime | DATETIME | No | - | Seckill end time |
| `seckillPrice` | BigDecimal | DECIMAL(10,2) | No | - | Seckill price |
| `stock` | Integer | INT | No | - | Seckill inventory quantity |
| `createTime` | LocalDateTime | DATETIME | No | CURRENT_TIMESTAMP | Record creation time |
| `updateTime` | LocalDateTime | DATETIME | No | CURRENT_TIMESTAMP | Last update time |

### Key Characteristics
- **No Audit Status Field**: Audit status is maintained in the unified Audit table, NOT in SeckillActivity
- **Simple Structure**: Focuses on core seckill data only
- **Integration Point**: Can be linked via audit records using targetId

---

## 3. SECKILL GOODS ENTITY

### Current Status: **DESIGNED BUT NOT YET IMPLEMENTED**

### Design from seckill_design.md

**Table Structure** (from database design section):
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

### Expected Fields (when implemented)
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `activityId` | Long | Foreign key to seckill_activity |
| `auditId` | Long | Foreign key to audit (approval record) |
| `productId` | Long | Foreign key to product |
| `merchantId` | Long | Foreign key to merchant |
| `seckillPrice` | BigDecimal | Seckill price |
| `stock` | Integer | Seckill inventory |
| `soldCount` | Integer | Units sold (default 0) |
| `createTime` | LocalDateTime | Creation timestamp |
| `updateTime` | LocalDateTime | Update timestamp |

### Approval Process (per design doc)
- Created when audit is **APPROVED** 
- Links back to both `SeckillActivity` and `Audit` records
- Combines seckill activity with merchant and product context
- Unique constraint on (activityId, productId) ensures no duplicates

---

## 4. SERVICE INTERFACES AND IMPLEMENTATIONS

### A. ISeckillActivityService

#### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillActivityService.java`

#### Interface Definition
```java
public interface SeckillActivityService extends IService<SeckillActivity> {
}
```

#### Available Methods (Inherited from IService<SeckillActivity>)

**CRUD Operations**:
- `save(SeckillActivity entity)` - Create new seckill activity
- `saveOrUpdate(SeckillActivity entity)` - Insert or update
- `saveBatch(Collection<SeckillActivity> entityList)` - Batch insert
- `getById(Serializable id)` - Get by ID
- `getByIds(Collection<? extends Serializable> idList)` - Batch get by IDs
- `list()` - Get all records
- `list(Wrapper<SeckillActivity> queryWrapper)` - Query with conditions
- `page(IPage<SeckillActivity> page)` - Paginated query
- `page(IPage<SeckillActivity> page, Wrapper<SeckillActivity> queryWrapper)` - Paginated conditional query
- `updateById(SeckillActivity entity)` - Update by ID
- `update(SeckillActivity entity, Wrapper<SeckillActivity> updateWrapper)` - Conditional update
- `removeById(Serializable id)` - Delete by ID
- `removeByIds(Collection<? extends Serializable> idList)` - Batch delete by IDs
- `remove(Wrapper<SeckillActivity> queryWrapper)` - Delete with conditions
- `count()` - Count all records
- `count(Wrapper<SeckillActivity> queryWrapper)` - Count with conditions

### B. SeckillActivityServiceImpl

#### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillActivityServiceImpl.java`

#### Implementation Details
- **Extends**: `ServiceImpl<SeckillActivityMapper, SeckillActivity>`
- **Annotation**: `@Service`
- **Mapper**: Uses MyBatis Plus auto-mapped SeckillActivityMapper
- **Status**: Fully implemented with all IService methods available

### C. Related Service: IAuditService

#### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/service/IAuditService.java`

#### Custom Methods (Not inherited from IService)
- `submitAudit(AuditSubmit submitDTO)` - Submit audit request
- `pageQuery(AuditParamsDTO queryDTO)` - Paginated query with filters
- `getAuditById(Long auditId)` - Get audit details by ID
- `withdrawAudit(Long auditId)` - Withdraw pending audit (revoke submission)
- `queryLatestAudit(AuditType type, Long targetId)` - Get latest audit for target
- `queryLatestAuditByTypeBatch(AuditType type, Collection<? extends Serializable> targetIds)` - Batch latest audits
- `updateAudit(Audit audit)` - Update audit record
- `queryAuditStatus(AuditType type, Long targetId)` - Get current audit status

---

## 5. SECKILL ACTIVITY AUDITOR - IMPLEMENTATION STATUS

### File Location
`/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/impl/SeckillActivityAuditor.java`

### Status: **FULLY IMPLEMENTED**

### Class Structure
- **Package**: `com.onlineshop.framework.models.audit.application.impl`
- **Extends**: `AbstractAuditor<SeckillActivityAuditRequest>`
- **Annotation**: `@Component`
- **Spring Registration**: Yes, auto-registered as Spring component

### Constructor & Dependencies
```java
public SeckillActivityAuditor(
    IAuditService auditService,
    SeckillActivityService seckillActivityService,
    IGoodsService goodsService
)
```

### Implemented Methods

#### 1. `support(AuditType type) → boolean`
- **Purpose**: Check if this auditor handles the given audit type
- **Implementation**: `return AuditType.SECKILL_ACTIVITY == type;`
- **Status**: Correctly identifies SECKILL_ACTIVITY type

#### 2. `validateRequest(SeckillActivityAuditRequest request) → void`
- **Purpose**: Validate audit request before processing
- **Current Implementation**: Empty (no validation implemented)
- **TODO**: Should implement validation for:
  - Product exists and is valid
  - Start time < End time
  - Time rules (24 hours advance)
  - Price validation (seckill < original price)
  - Stock validation (seckill ≤ product stock)
  - Uniqueness check (product not already in active/pending audit)

#### 3. `createAuditTarget(SeckillActivityAuditRequest request) → Long`
- **Purpose**: Create tentative SeckillActivity during audit submission
- **Implementation**: 
  1. Create new `SeckillActivity` entity
  2. Map fields from request (productId, startTime, endTime, seckillPrice, stock)
  3. Set timestamps (createTime, updateTime = now)
  4. Save to database via `seckillActivityService.save()`
  5. Return the generated ID
- **Timing**: Executes during audit submission (BEFORE approval)
- **Status**: Fully implemented

#### 4. `generateSnapshot(SeckillActivityAuditRequest request) → String`
- **Purpose**: Create JSON snapshot of audit request for persistence
- **Implementation**: `return JSON.toJSONString(request);`
- **Format**: JSON string using FastJSON2 library
- **Usage**: Stored in Audit.snapshot field for future reference

#### 5. `afterApprove(Long targetId) → void`
- **Purpose**: Execute business logic when audit is approved
- **Current Implementation**: Empty (no action needed)
- **Comments**: Notes that SeckillActivity has no audit status field
- **Audit Status Location**: Maintained by Audit table, not SeckillActivity
- **Status**: Intentionally empty - audit status tracked separately

#### 6. `afterReject(Long targetId, String reason) → void`
- **Purpose**: Execute business logic when audit is rejected
- **Current Implementation**: Empty (optional implementation)
- **Comments**: Notes that rejection handling is optional
- **Possible Future Actions**: 
  - Delete created SeckillActivity
  - Mark as rejected
  - Send notifications
- **Status**: Intentionally empty - no mandatory cleanup

### Workflow Sequence

```
1. SUBMISSION PHASE (submitAudit called)
   ├─ validateRequest() - Validate input
   ├─ createAuditTarget() - Create SeckillActivity record
   ├─ generateSnapshot() - Serialize to JSON
   └─ saveAuditRecord() - Save Audit record (PENDING status)

2. REVIEW PHASE (admin reviews in audit system)
   └─ [Awaiting approval/rejection]

3. APPROVAL PHASE (handleDecision called with approved=true)
   ├─ Update Audit status to APPROVED
   └─ afterApprove() - Additional logic (if needed)
      └─ [SeckillActivity remains active]

4. REJECTION PHASE (handleDecision called with approved=false)
   ├─ Update Audit status to REJECTED
   └─ afterReject() - Cleanup logic (if needed)
      └─ [SeckillActivity may be deleted or marked]
```

### Key Design Decisions

1. **Early Creation**: SeckillActivity created during submission, not after approval
   - Enables validation against actual entity
   - Simplifies approval: just update Audit status
   - Decouples object lifecycle from approval status

2. **Separate Audit Status**: Audit status NOT stored in SeckillActivity
   - All audit info in unified Audit table
   - Clean separation of concerns
   - Audit state managed independently

3. **Template Method Pattern**: Extends AbstractAuditor
   - Provides framework for audit workflow
   - Ensures consistent transaction handling
   - Type-safe with generics

---

## 6. VALIDATION RULES AND BUSINESS REQUIREMENTS

### From `/docs/seckill_design.md`

#### 6.1 Time Rules (Section 7.3)

| Rule | Requirement | Enforcement Point |
|------|-------------|-------------------|
| Activity Creation | Minimum 24 hours advance | Submission validation |
| Activity Duration | Fixed 1 hour per slot | UI constraint (0-23) |
| Time Slot | Integer hours only (0-23) | UI dropdown selection |
| Application Window | Only during "报名中" status | Activity status check |
| No Duplication | One activity per date+hour | Unique constraint (DB) |

#### 6.2 Product/Goods Rules (Section 7.4)

| Rule | Description | Check When |
|------|-------------|-----------|
| Single Activity | Product can participate in max 1 activity | Submit & Approve |
| Stock Limitation | Seckill stock ≤ Product total stock | Submit |
| Price Limitation | Seckill price < Original product price | Submit |
| Rejection Release | Product freed after rejection | Rejection |
| Product Status | Product must be valid/active | Submit |

#### 6.3 Activity Status Rules (Section 7.1)

| Status | Duration | Merchant Can Apply | Notes |
|--------|----------|------------------|-------|
| 报名中 (Signup) | Before start | ✅ Yes | Open for applications |
| 进行中 (Ongoing) | During event | ❌ No | Activity running |
| 已结束 (Ended) | After completion | ❌ No | Activity finished |

#### 6.4 Audit Status Rules (Section 7.2)

| Status | Meaning | Can Modify | Can Resubmit |
|--------|---------|-----------|--------------|
| 待审核 (Pending) | Submitted, waiting | ✅ Yes | ✅ Yes |
| 已通过 (Approved) | Passed, active | ❌ No | ❌ No |
| 已驳回 (Rejected) | Failed review | ✅ Yes | ✅ Yes |

#### 6.5 Customer-Side Rules (Section 7.5)

| Rule | Details |
|------|---------|
| Per-Person Limit | 1 unit or configurable |
| Anti-Fraud Measures | Same IP/device limit |
| Rate Limiting | API throttling |
| Payment Timeout | 15 minutes to pay |

---

## 7. SUPPORTING ENTITIES AND ENUMS

### A. SeckillActivityDTO (Data Transfer Object)

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/dto/SeckillActivityDTO.java`

**Fields**: Same as SeckillActivity entity
- id, productId, startTime, endTime, seckillPrice, stock

### B. SeckillActivityVO (View Object)

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/vo/SeckillActivityVO.java`

**Fields**:
- id, productId, startTime, endTime, seckillPrice, stock
- `remainingStock` - Calculated remaining inventory
- `status` - Current seckill status (0=NotStarted, 1=Ongoing, 2=Ended)

### C. SeckillStatusEnum

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/enums/SeckillStatusEnum.java`

```java
NOT_STARTED(0, "未开始")    // Before startTime
ONGOING(1, "进行中")       // Between start and end time
ENDED(2, "已结束")         // After endTime
```

### D. AuditRequest (Base Class)

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/domain/AuditRequest.java`

**Common Fields**:
- `type` - "SECKILL_ACTIVITY" for seckill audits
- `applicantId` - Who submitted
- `applicantName` - Submitter's name
- `targetId` - Business object ID (set during submission)

**Jackson Polymorphism**: Uses @JsonTypeInfo and @JsonSubTypes for automatic type detection

### E. Audit Entity

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/entity/Audit.java`

**Key Fields**:
- `id` - Audit record ID
- `targetType` - "SECKILL_ACTIVITY" (from AuditRequest.type)
- `targetId` - ID of SeckillActivity
- `status` - PENDING, APPROVED, REJECTED, REVOKED, REAUDIT
- `snapshot` - JSON of original SeckillActivityAuditRequest
- `applicantId`, `applicantName` - Who applied
- `auditorId`, `auditorName` - Who reviewed
- `createTime` - Application timestamp
- `auditTime` - Review timestamp

### F. AuditStatus Enum

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/enums/AuditStatus.java`

```java
PENDING("PENDING", "待审核")           // Initial state
APPROVED("APPROVED", "通过")           // Approved
REJECTED("REJECTED", "拒绝")           // Rejected
REVOKED("REVOKED", "已撤销")           // Withdrawn
REAUDIT("REAUDIT", "需重新审核")       // Needs re-review
```

### G. AuditType Enum

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/enums/AuditType.java`

```java
GOODS("GOODS", "商品")
STORE_REGISTER("STORE_REGISTER", "店铺注册")
SECKILL_ACTIVITY("SECKILL_ACTIVITY", "秒杀活动")  // Our target
```

---

## 8. INTEGRATION POINTS

### Manager Class: SeckillManager

**File**: `/home/Tomatos/Projects/design/online-mall/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/manager/SeckillManager.java`

**Responsibilities**:
- Stock management (Redis + Database sync)
- Rate limiting (per-user per-minute)
- Seckill status checking
- Order generation
- Cache initialization and cleanup

**Key Methods**:
- `checkSeckillStatus(Long seckillId)` - Get current state
- `participateSeckill(Long seckillId, Long userId, Integer quantity)` - Place order
- `getRemainingStock(Long seckillId)` - Check inventory
- `syncStockToDatabase(Long seckillId)` - Finalize inventory
- `initializeStock(Long seckillId)` - Load to Redis
- `clearSeckillCache(Long seckillId)` - Cleanup

---

## 9. MISSING IMPLEMENTATIONS & TODOs

### High Priority

1. **SeckillGoods Entity** - Needs to be created
   - Should be generated when audit is APPROVED
   - Bridges SeckillActivity + Merchant + Product
   - Database table needs migration

2. **SeckillActivityAuditor.validateRequest()** - Empty implementation
   - Add validation for all business rules
   - Check product existence
   - Validate time, price, stock constraints
   - Check uniqueness rules

3. **ISeckillGoodsService** - Need to create
   - CRUD for SeckillGoods
   - Batch operations for approval

4. **IGoodsService** - Referenced but interface structure unknown
   - Used to validate product info
   - Need to check implementation

### Medium Priority

1. **afterApprove/afterReject callbacks** - Currently empty
   - May need to implement once SeckillGoods entity exists
   - After approve: create SeckillGoods record
   - After reject: cleanup logic

2. **Error Codes** - SeckillActivityAuditor references various error codes
   - Ensure all codes defined in BizErrorCode enum

---

## 10. FILE LOCATIONS SUMMARY

### Core Entities & Requests
- `SeckillActivityAuditRequest` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/domain/SeckillActivityAuditRequest.java`
- `SeckillActivity` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/entity/SeckillActivity.java`
- `SeckillActivityDTO` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/dto/SeckillActivityDTO.java`
- `SeckillActivityVO` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/vo/SeckillActivityVO.java`

### Services & Mappers
- `SeckillActivityService` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillActivityService.java`
- `SeckillActivityServiceImpl` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/service/SeckillActivityServiceImpl.java`
- `SeckillActivityMapper` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/mapper/SeckillActivityMapper.java`
- `IAuditService` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/service/IAuditService.java`

### Auditor & Abstract Base
- `SeckillActivityAuditor` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/impl/SeckillActivityAuditor.java`
- `AbstractAuditor` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/application/AbstractAuditor.java`
- `AuditRequest` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/domain/AuditRequest.java`
- `Audit` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/entity/Audit.java`

### Enums
- `SeckillStatusEnum` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/enums/SeckillStatusEnum.java`
- `AuditType` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/enums/AuditType.java`
- `AuditStatus` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/audit/enums/AuditStatus.java`

### Managers
- `SeckillManager` → `/online-shop-framework/src/main/java/com/onlineshop/framework/models/seckill/manager/SeckillManager.java`

### Documentation
- `seckill_design.md` → `/docs/seckill_design.md`

---

## 11. QUICK REFERENCE DIAGRAMS

### Data Flow: Audit Submission

```
User Request
    ↓
SeckillActivityAuditRequest (contains product, time, price, stock)
    ↓
SeckillActivityAuditor.submitAudit()
    ├─ validateRequest() [EMPTY - needs implementation]
    ├─ createAuditTarget() [CREATES SeckillActivity]
    │  └─ Save to DB
    └─ saveAuditRecord() [CREATES Audit record, PENDING status]
        └─ Store in audit table with JSON snapshot
```

### Audit Status Lifecycle

```
┌─────────────────┐
│    PENDING      │  (Waiting for admin review)
└────────┬────────┘
         │
    ┌────┴─────┐
    ↓          ↓
┌─────────┐ ┌───────────┐
│APPROVED │ │ REJECTED  │  (Admin decision)
└─────────┘ └───────────┘
    │            │
afterApprove() afterReject()
    │            │
Activity OK   Cleanup/Notify
```

### Service Method Availability

```
SeckillActivityService (extends IService<SeckillActivity>)
├─ Standard CRUD: save, update, remove, get, list
├─ Batch: saveBatch, removeByIds, getByIds
├─ Query: list (conditional), page (paginated)
├─ Count: count (all/conditional)
└─ Update: update (conditional), removeById
```

---

## 12. NEXT STEPS FOR COMPLETE IMPLEMENTATION

1. **Implement validateRequest()** in SeckillActivityAuditor
   - Add all business rule validations
   - Check product exists and is valid
   - Validate time constraints (24hr advance)
   - Validate price < original price
   - Validate stock ≤ product stock
   - Check uniqueness (no other pending/approved for product)

2. **Create SeckillGoods entity and service**
   - Entity matching table design from seckill_design.md
   - SeckillGoodsService interface and implementation
   - SeckillGoodsMapper for database access

3. **Implement afterApprove() callback**
   - When audit is APPROVED, create SeckillGoods record
   - Link audit record to seckill goods
   - Populate merchant context

4. **Add SeckillGoodsService to SeckillActivityAuditor**
   - Inject into constructor
   - Use in afterApprove() to create goods record

5. **Create database migration**
   - Add seckill_goods table
   - Add audit_id column to link records
   - Add foreign key constraints

6. **Implement afterReject() callback**
   - Optional: Delete created SeckillActivity
   - Optional: Send rejection notification to merchant

