# ES搜索条件构建重构说明

## 重构目标

重构 `GoodsEsService` 中的ES分页条件构建方法，支持以下搜索功能：
- ✅ 关键词搜索（商品名称 + 商品卖点）
- ✅ 价格区间搜索
- ✅ 按顶级分类ID搜索
- ✅ 按具体分类ID搜索

## 重构内容

### 1. 修改 `GoodsSearchDTO`

**新增字段：**
- `topCategoryId`: 顶级分类ID（用于按顶级分类筛选商品）

```java
/**
 * 顶级分类ID
 * 用于按顶级分类筛选商品
 * 如果为null则不按顶级分类筛选
 */
private Long topCategoryId;
```

### 2. 修改 `GoodsIndex`

**新增字段：**
- `topCategoryId`: 顶级分类ID（从 `categoryIdPath` 中提取）

**新增方法：**
- `extractTopCategoryId(String categoryIdPath)`: 从分类路径中提取顶级分类ID

分类路径格式为：`1,2,3`（从顶级到当前分类），该方法会自动提取第一个ID作为顶级分类ID。

### 3. 重构 `GoodsEsService.buildSearchCriteria()`

将单一的、长的方法拆分为多个职责明
