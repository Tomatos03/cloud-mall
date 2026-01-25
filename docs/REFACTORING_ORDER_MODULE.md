## Order 模块多规格商品支持重构总结

### 📋 修改清单

#### 1. **数据实体层 (Entity)**

**文件: `OrderItem.java`** ✅ 已修改
- 添加 `skuId` 字段 - 真实下单对象ID（支持多规格商品）
- 添加 `goodsPrice` 字段 - 从SKU获取实际价格
- 添加 `skuSpecs` 字段 - 规格快照（格式：颜色=黑色;尺码=L）
- 移除旧的 `specSnapshot` 字段（改为 `skuSpecs`）

对应的数据库字段已存在：
```sql
-- 已存在字段
sku_id bigint NOT NULL COMMENT 'SKU ID（真实下单对象）'
sku_specs varchar(255) NULL COMMENT 'SKU规格快照'
goods_price bigint NULL COMMENT '下单时商品单价（分）'
```

#### 2. **DTO 层**

**文件: `TradeShopItemDTO.java`** ✅ (已由用户修改)
```java
@Data
public class TradeShopItemDTO {
    private Long skuId;        // 改: goodsId → skuId（支持多规格）
    private Integer quantity;
}
```

#### 3. **VO 层**

**文件: `StoreOrderItemVO.java`** ✅ (已由用户修改)
```java
@Data
public class StoreOrderItemVO implements Serializable {
    // ... 其他字段
    private Map<String, String> selectedSpecs; // 购买时选择的规格
}
```

#### 4. **业务逻辑层 (Service)**

**文件: `OrderService.java`** ✅ 已修改
- 更新 `buildStoreOrderItemVO()` 方法：
  - 调用 `parseSkuSpecs()` 解析规格快照
  - 将 `skuSpecs` 字符串解析为 `Map<String, String>`
  - 映射到 `StoreOrderItemVO.selectedSpecs` 字段

新增方法：
```java
/**
 * 解析SKU规格快照字符串
 * 格式：颜色=黑色;尺码=L
 */
private Map<String, String> parseSkuSpecs(String skuSpecs) {
    // 解析逻辑：按;分隔，每项按=分隔成key-value
}
```

#### 5. **策略层 - 创建策略 (CreateStrategy)**

**文件: `AbstractOrderCreateStrategy.java`** ✅ 核心重构

添加新的依赖注入：
```java
protected final IGoodsSkuService goodsSkuService;
protected final IGoodsSkuSpecService goodsSkuSpecService;
protected final ISpecService specService;
protected final ISpecValueService specValueService;
```

修改主要方法：
- `buildOrderForShop()` - 从SKU获取信息，计算规格快照
- `buildOrderItem()` - 新增参数支持SKU和规格数据
- 新增 `buildSkuSpecsSnapshot()` - 构建规格快照
- 新增 `deductSkuInventory()` - 基于SKU扣减库存

关键变化：
```java
// 旧：基于商品
OrderItem buildOrderItem(Goods goods, TradeShopItemDTO itemDTO, long itemTotalPrice)

// 新：支持SKU和规格
OrderItem buildOrderItem(
    Goods goods, 
    TradeShopItemDTO itemDTO, 
    GoodsSku sku,           
    String skuSpecsSnapshot, 
    long itemTotalPrice
)
```

**文件: `NormalCartOrderCreateStrategy.java`** ✅ 已修改
- 更新构造函数以注入SKU相关服务

**文件: `InstantBuyOrderCreateStrategy.java`** ✅ 已修改
- 更新构造函数以注入SKU相关服务

#### 6. **策略层 - 验证策略 (ValidateStrategy)**

**文件: `AbstractOrderValidateStrategy.java`** ✅ 核心重构

添加新的依赖注入：
```java
protected final IGoodsSkuService goodsSkuService;
```

修改主要方法：
- `validateShop()` - 从SKU验证而非商品
- `validateItemData()` - 校验 `skuId` 而非 `goodsId`
- 新增 `validateSkuInventory()` - 基于SKU库存校验
- 移除 `validateInventory()` - 旧的商品级库存校验

关键变化：
```java
// 旧流程
if (!availableGoodsIds.contains(item.getGoodsId())) { ... }
validateInventory(goods, quantity);

// 新流程
GoodsSku sku = goodsSkuService.getById(item.getSkuId());
if (!availableGoodsIds.contains(sku.getGoodsId())) { ... }
validateSkuInventory(sku, quantity);
```

**文件: `NormalCartOrderValidateStrategy.java`** ✅ 已修改
- 更新构造函数注入 `IGoodsSkuService`
- 修改 `doAdditionalValidate()` - 校验购物车中的SKU而非商品
- 修改 `validateCartItem()` - 检查SKU存在性

**文件: `InstantBuyOrderValidateStrategy.java`** ✅ 已修改
- 更新构造函数注入 `IGoodsSkuService`
- 修改 `supportCartType()` 方法签名

### 🔄 工作流程变化

#### 旧流程（单规格）
```
TradeShopItemDTO {goodsId, quantity}
    ↓
获取商品信息 (Goods)
    ↓
使用商品默认价格计算订单价格
    ↓
基于商品扣减库存
    ↓
OrderItem {goodsId, goodsName, goodsPrice, ...}
```

#### 新流程（多规格）
```
TradeShopItemDTO {skuId, quantity}
    ↓
获取商品信息 (Goods) + SKU信息 (GoodsSku)
    ↓
查询SKU规格 (GoodsSkuSpec) 并构建规格快照
    ↓
使用SKU价格计算订单价格
    ↓
基于SKU扣减库存
    ↓
OrderItem {
  skuId, 
  goodsId, 
  goodsName, 
  goodsPrice,    // SKU的实际价格
  skuSpecs,      // "颜色=黑色;尺码=L"
  ...
}
```

### 📊 数据流示例

**下单请求:**
```json
{
  "storeId": 1,
  "tradeShopItemList": [
    {
      "skuId": 101,      // 黑色L号T恤 SKU
      "quantity": 2
    }
  ]
}
```

**生成的订单项（OrderItem）:**
```json
{
  "id": 1,
  "orderId": 1000,
  "skuId": 101,
  "goodsId": 50,
  "goodsName": "T恤",
  "goodsPrice": 9900,        // 这个SKU的价格（单位：分）
  "quantity": 2,
  "totalPrice": 19800,
  "skuSpecs": "颜色=黑色;尺码=L",  // 规格快照
  "createTime": "2025-01-02T10:30:00"
}
```

**查询订单展示（StoreOrderItemVO）:**
```java
StoreOrderItemVO {
  goodsId: 50,
  goodsName: "T恤",
  goodsMainImage: Image(...),
  goodsPrice: 9900,           // 单位：分
  goodsPriceText: "¥99.00",   // 格式化
  quantity: 2,
  totalPrice: 19800,
  totalPriceText: "¥198.00",
  selectedSpecs: {            // 解析后的规格Map
    "颜色": "黑色",
    "尺码": "L"
  },
  createTime: LocalDateTime(...)
}
```

### 🛠️ 涉及的核心服务

| 服务接口 | 用途 | 关键方法 |
|---------|------|--------|
| `IGoodsService` | 获取商品基本信息 | `getAvailableGoodsById()`, `lambdaQuery()` |
| `IGoodsSkuService` | 获取/管理SKU | `getById()`, `deductInventory()`, `increaseSales()` |
| `IGoodsSkuSpecService` | 查询SKU规格 | `listBySkuId()` |
| `ISpecService` | 获取规格定义 | `getById()` |
| `ISpecValueService` | 获取规格值定义 | `getById()` |
| `ICartService` | 购物车操作 | `existsInCart()` |

### ✨ 关键改进点

1. **真实多规格支持** 
   - 不同规格可以有不同的价格和库存
   - 同一商品的不同规格完全独立管理

2. **规格快照** 
   - 订单保存下单时选择的规格信息
   - 防止后续商品规格变更影响历史订单展示

3. **库存精确扣减** 
   - 从商品级扣减 → SKU级扣减
   - 支持同一商品不同规格的独立库存管理

4. **向后兼容** 
   - 单规格商品只有一个SKU
   - 现有逻辑无需特殊处理自动兼容

5. **架构清晰** 
   - DTO 层传输SKU标识
   - Entity 层存储规格快照
   - VO 层展示解析后的规格

### 🧪 测试场景

**场景1：多规格商品下单**
```
商品：T恤（ID=50）
SKU1：黑色M号，价格¥99，库存10
SKU2：黑色L号，价格¥109，库存5

操作：购买黑色L号2件
请求：TradeShopItemDTO {skuId=SKU2, quantity=2}

预期结果：
✓ 订单项价格 = ¥109 * 2 = ¥218
✓ SKU2库存 = 5 - 2 = 3
✓ 规格快照 = "颜色=黑色;尺码=L"
✓ 展示规格 = {颜色: 黑色, 尺码: L}
```

**场景2：库存不足**
```
SKU库存：3件
下单数量：5件

预期结果：
✗ 订单创建失败
✗ 错误信息：SKU库存不足
✓ 库存未扣减
```

**场景3：购物车中无该SKU**
```
购物车：空
下单SKU：101

预期结果：
✗ 订单创建失败
✗ 错误信息：购物车中不存在该SKU
```

### 📝 修改的文件清单

| 文件路径 | 修改类型 | 关键变化 |
|---------|---------|--------|
| `OrderItem.java` | Entity | 添加 skuId, skuSpecs, goodsPrice |
| `OrderService.java` | Service | 新增 parseSkuSpecs() |
| `AbstractOrderCreateStrategy.java` | Strategy | 添加SKU依赖，修改buildOrderItem() |
| `NormalCartOrderCreateStrategy.java` | Strategy | 更新构造函数 |
| `InstantBuyOrderCreateStrategy.java` | Strategy | 更新构造函数 |
| `AbstractOrderValidateStrategy.java` | Strategy | 从商品校验→SKU校验 |
| `NormalCartOrderValidateStrategy.java` | Strategy | 校验SKU而非商品 |
| `InstantBuyOrderValidateStrategy.java` | Strategy | 更新构造函数 |

**未修改但受影响的文件：**
| 文件路径 | 原因 |
|---------|------|
| `TradeShopItemDTO.java` | 已由用户修改（skuId替换goodsId） |
| `StoreOrderItemVO.java` | 已由用户修改（添加selectedSpecs） |

### 🚀 后续任务（可选）

1. **购物车模块更新**
   - `ICartService.existsInCart()` 需要支持SKU而非商品ID
   - 购物车存储结构需要考虑SKU关联

2. **数据库迁移**
   - 为现有订单历史补充SKU和规格数据（如果需要）
   - 创建迁移脚本处理旧数据

3. **订单查询优化**
   - 在 `beforeBuildOrderItems()` 中批量预加载SKU和规格
   - 减少N+1查询问题

4. **订单导出/报表**
   - 确保导出包含规格信息
   - 增强数据统计维度

5. **售后模块适配**
   - 退货、退款需要处理SKU级别操作
   - 售后数据关联到SKU而非商品

6. **购物车缓存更新**
   - 如果使用了缓存，需要更新缓存结构
   - 确保缓存包含SKU标识

### 💡 设计注意事项

1. **规格快照格式**
   - 使用简单的 `key=value;key2=value2` 格式
   - 便于存储和解析，避免过度复杂化

2. **向后兼容性**
   - 单规格商品的SKU仍然有效
   - 不需要特殊处理历史订单

3. **性能考虑**
   - SKU查询在 validateShop 中做一次
   - buildOrderForShop 中直接复用已验证的SKU
   - 避免重复查询数据库

4. **错误处理**
   - SKU不存在直接抛出异常
   - 规格解析失败使用空Map（不阻断流程）

### 🔗 相关代码引用

**规格解析示例：**
```java
// 输入: "颜色=黑色;尺码=L"
// 输出: {颜色: 黑色, 尺码: L}

String skuSpecs = "颜色=黑色;尺码=L";
String[] pairs = skuSpecs.split(";");
Map<String, String> specMap = new LinkedHashMap<>();
for (String pair : pairs) {
    String[] kv = pair.split("=");
    if (kv.length == 2) {
        specMap.put(kv[0].trim(), kv[1].trim());
    }
}
```

**SKU规格构建示例：**
```java
// 从数据库查询
List<GoodsSkuSpec> skuSpecs = goodsSkuSpecService.listBySkuId(skuId);

// 遍历构建快照
StringBuilder sb = new StringBuilder();
for (GoodsSkuSpec spec : skuSpecs) {
    Spec spec = specService.getById(spec.getSpecId());      // 获取规格名
    SpecValue value = specValueService.getById(spec.getSpecValueId()); // 获取规格值
    sb.append(spec.getName()).append("=").append(value.getValue()).append(";");
}
// 结果: "颜色=黑色;尺码=L;"（末尾分号需要处理）
```

---

**重构完成日期:** 2025-01-02  
**重构范围:** Order 模块核心策略和数据层  
**向后兼容:** ✅ 完全兼容  
**测试需求:** ⚠️ 需要完整的集成测试