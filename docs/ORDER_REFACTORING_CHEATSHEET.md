# Order 模块多规格重构 - 快速参考卡片

## 🎯 核心变化一览

| 层级 | 旧流程 | 新流程 |
|------|-------|-------|
| **DTO** | `goodsId` | ✅ `skuId` |
| **Entity** | 无规格存储 | ✅ `skuSpecs` (快照) |
| **Entity** | 商品价格 | ✅ `goodsPrice` (SKU价格) |
| **验证** | 商品库存校验 | ✅ SKU库存校验 |
| **创建** | 商品库存扣减 | ✅ SKU库存扣减 |
| **VO** | 无规格展示 | ✅ `selectedSpecs` (Map) |

## 📊 数据流速写

```
请求: {skuId: 101, quantity: 2}
       ↓
验证: SKU存在 → 商品已上架 → 库存充足 ✓
       ↓
查询: Goods + GoodsSku + GoodsSkuSpec
       ↓
规格快照: "颜色=黑色;尺码=L"
       ↓
价格计算: sku.price × 2
       ↓
OrderItem: {skuId, goodsId, goodsPrice, skuSpecs, ...}
       ↓
库存扣减: SKU库存 -2
```

## 🔧 关键方法变更

### 1️⃣ AbstractOrderCreateStrategy

**添加依赖：**
```java
protected final IGoodsSkuService goodsSkuService;
protected final IGoodsSkuSpecService goodsSkuSpecService;
protected final ISpecService specService;
protected final ISpecValueService specValueService;
```

**修改方法签名：**
```java
// 旧
protected OrderItem buildOrderItem(Goods goods, TradeShopItemDTO itemDTO, long itemTotalPrice)

// 新
protected OrderItem buildOrderItem(
    Goods goods, 
    TradeShopItemDTO itemDTO, 
    GoodsSku sku,           // ← 新增
    String skuSpecsSnapshot, // ← 新增
    long itemTotalPrice
)
```

**新增方法：**
```java
private String buildSkuSpecsSnapshot(Long skuId)
private void deductSkuInventory(Long skuId, Integer quantity)
```

### 2️⃣ AbstractOrderValidateStrategy

**添加依赖：**
```java
protected final IGoodsSkuService goodsSkuService;
```

**修改校验逻辑：**
```java
// 旧
Long goodsId = item.getGoodsId();
if (!availableGoodsIds.contains(goodsId)) { ... }
validateInventory(goods, quantity);

// 新
Long skuId = item.getSkuId();
GoodsSku sku = goodsSkuService.getById(skuId);
if (sku == null) { ... }
Long goodsId = sku.getGoodsId();
if (!availableGoodsIds.contains(goodsId)) { ... }
validateSkuInventory(sku, quantity);
```

**新增方法：**
```java
protected void validateSkuInventory(GoodsSku sku, Integer quantity)
```

### 3️⃣ OrderService

**新增方法：**
```java
private Map<String, String> parseSkuSpecs(String skuSpecs) {
    Map<String, String> specMap = new LinkedHashMap<>();
    String[] pairs = skuSpecs.split(";");
    for (String pair : pairs) {
        String[] kv = pair.split("=");
        if (kv.length == 2) {
            specMap.put(kv[0].trim(), kv[1].trim());
        }
    }
    return specMap;
}
```

**修改方法：**
```java
private StoreOrderItemVO buildStoreOrderItemVO(OrderItem item) {
    Map<String, String> selectedSpecs = parseSkuSpecs(item.getSkuSpecs());
    return StoreOrderItemVO.builder()
            // ... 其他字段
            .selectedSpecs(selectedSpecs)  // ← 新增
            .build();
}
```

## 📋 实现检查清单

### 修改OrderItem实体
- [ ] 添加 `skuId` 字段
- [ ] 添加 `skuSpecs` 字段
- [ ] `goodsPrice` 字段已存在

### 修改DTO层
- [ ] `TradeShopItemDTO` 已改为 `skuId`

### 修改VO层
- [ ] `StoreOrderItemVO` 已添加 `selectedSpecs`

### 修改CreateStrategy
- [ ] `AbstractOrderCreateStrategy` 添加SKU依赖
- [ ] 修改 `buildOrderForShop()` 方法
- [ ] 修改 `buildOrderItem()` 方法签名
- [ ] 新增 `buildSkuSpecsSnapshot()` 方法
- [ ] 新增 `deductSkuInventory()` 方法
- [ ] 更新 `NormalCartOrderCreateStrategy` 构造函数
- [ ] 更新 `InstantBuyOrderCreateStrategy` 构造函数

### 修改ValidateStrategy
- [ ] `AbstractOrderValidateStrategy` 添加SKU依赖
- [ ] 修改 `validateShop()` 方法
- [ ] 修改 `validateItemData()` 方法
- [ ] 新增 `validateSkuInventory()` 方法
- [ ] 移除 `validateInventory()` 方法
- [ ] 更新 `NormalCartOrderValidateStrategy` 构造函数
- [ ] 更新 `InstantBuyOrderValidateStrategy` 构造函数

### 修改Service
- [ ] `OrderService` 新增 `parseSkuSpecs()` 方法
- [ ] 修改 `buildStoreOrderItemVO()` 方法

## 🧪 测试用例

### TC1: 多规格商品正常下单
```
Given: SKU库存充足，规格存在
When: 下单1件黑色L号T恤
Then: 
  ✓ OrderItem.skuId = 101
  ✓ OrderItem.skuSpecs = "颜色=黑色;尺码=L"
  ✓ StoreOrderItemVO.selectedSpecs = {颜色: 黑色, 尺码: L}
```

### TC2: SKU不存在
```
Given: SKU ID 999不存在
When: 尝试下单
Then: ✓ 抛出 GOODS_NOT_EXIST 异常
```

### TC3: SKU库存不足
```
Given: SKU库存 = 2
When: 下单3件
Then: ✓ 抛出 GOODS_INVENTORY_NOT_ENOUGH 异常
```

### TC4: 购物车缺少该SKU
```
Given: 购物车中无SKU 101
When: 普通购物车下单SKU 101
Then: ✓ 抛出 GOODS_NOT_IN_CART 异常
```

## 🔍 常见问题

**Q1: 为什么要添加skuSpecs而不用JSON？**
```
A: 简化数据格式，避免额外的JSON解析依赖
  - 存储: "颜色=黑色;尺码=L"
  - 解析: split(";") → split("=")
  - 易读、易维护
```

**Q2: 旧系统中没有SKU怎么办？**
```
A: 单规格商品也需要一个SKU
  - 商品1 → SKU1（唯一的规格组合）
  - 多规格商品 → SKU1, SKU2, SKU3 ...
  - 向后完全兼容
```

**Q3: 规格快照为什么要存储？**
```
A: 防止历史订单显示问题
  - 场景: 商品删除了某个规格后
  - 旧订单仍能正确展示购买时的规格
  - 订单数据完整独立
```

**Q4: 为什么不用Map直接存储？**
```
A: 数据库字符串存储更简洁
  - 数据库类型: VARCHAR(255)
  - 序列化: 简单字符串连接
  - 反序列化: 简单字符串分割
  - JSON会增加存储和解析成本
```

## 📍 文件位置快速查询

```
order/
├── entity/
│   └── OrderItem.java                              ← 添加 skuId, skuSpecs
├── dto/
│   └── TradeShopItemDTO.java                       ← goodsId → skuId
├── vo/
│   └── StoreOrderItemVO.java                       ← 添加 selectedSpecs
├── service/impl/
│   └── OrderService.java                           ← 新增 parseSkuSpecs()
└── strategy/impl/
    ├── AbstractOrderCreateStrategy.java            ← 核心重构 ⭐
    ├── AbstractOrderValidateStrategy.java          ← 核心重构 ⭐
    ├── NormalCartOrderCreateStrategy.java          ← 更新构造函数
    ├── NormalCartOrderValidateStrategy.java        ← 更新构造函数
    ├── InstantBuyOrderCreateStrategy.java          ← 更新构造函数
    └── InstantBuyOrderValidateStrategy.java        ← 更新构造函数
```

## 🎨 代码片段备用

### 规格快照构建
```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < skuSpecs.size(); i++) {
    GoodsSkuSpec spec = skuSpecs.get(i);
    Spec s = specService.getById(spec.getSpecId());
    SpecValue v = specValueService.getById(spec.getSpecValueId());
    
    sb.append(s.getName()).append("=").append(v.getValue());
    if (i < skuSpecs.size() - 1) sb.append(";");
}
return sb.toString();
```

### 规格快照解析
```java
Map<String, String> map = new LinkedHashMap<>();
if (skuSpecs == null || skuSpecs.isEmpty()) {
    return Collections.emptyMap();
}
String[] pairs = skuSpecs.split(";");
for (String pair : pairs) {
    String[] kv = pair.split("=");
    if (kv.length == 2) {
        map.put(kv[0].trim(), kv[1].trim());
    }
}
return map;
```

## ⚠️ 常见陷阱

1. **别忘记更新构造函数**
   - ❌ `super(goodsService);`
   - ✅ `super(goodsService, goodsSkuService, ...);`

2. **别混淆supportCartType()返回值**
   - ❌ `return CartType.NORMAL;`
   - ✅ `return CartType.NORMAL.name();` 或 `"NORMAL"`

3. **别忘记处理空规格快照**
   - ❌ 直接 split()
   - ✅ 先检查 null/empty，返回 emptyMap()

4. **别在循环中逐个查询SKU信息**
   - ❌ 每次循环都查数据库
   - ✅ 在 beforeBuildOrderItems() 中批量加载

5. **别混淆规格快照格式**
   - ❌ JSON: `{"颜色":"黑色"}`
   - ✅ 简单: `颜色=黑色;尺码=L`

## 📞 快速联系

- **核心文件**: AbstractOrderCreateStrategy.java, AbstractOrderValidateStrategy.java
- **数据文件**: OrderItem.java
- **显示文件**: StoreOrderItemVO.java, OrderService.java
- **测试重点**: SKU验证、规格快照、库存扣减

---

**版本**: 1.0  
**更新时间**: 2025-01-02  
**状态**: ✅ 完成  
