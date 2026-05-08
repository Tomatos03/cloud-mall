# 在线商城-用户端接口文档

## 公共约束

### 响应结构

所有接口响应均遵循以下统一格式：

**响应头参数**：

| 参数名 | 参数值 |
|--------|------|
| Content-Type | application/json |

**响应数据结构**：

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 业务状态码，200 为成功 |
| message | String | 提示信息 |
| data | Object | 存放响应数据体 |

**分页查询响应结构**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| records | Array | 当前页数据列表，具体字段见各接口的「records 字段说明」 |
| total | Long | 总记录数 |
| size | Long | 每页数量 |
| current | Long | 当前页码 |
| pages | Long | 总页数 |

### 认证方式

除特别说明外，所有接口均需要在请求头参数中携带 JWT Token 进行认证:

```
Authorization: Bearer <token>
```

---

## 用户模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/user`

### 1. 用户登录

#### 基本信息

**接口描述**：用户通过用户名和密码登录系统

**请求方式**：`POST`

**请求路径**：`/login`

**鉴权要求**：无需鉴权

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（不能为空） |
| password | String | 是 | 密码（不能为空） |

**请求数据示例**：

```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | JWT 访问令牌 |

---

### 2. 用户注册

#### 基本信息

**接口描述**：用户注册新账号

**请求方式**：`POST`

**请求路径**：`/register`

**鉴权要求**：无需鉴权

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（不能为空） |
| password | String | 是 | 密码（不能为空） |

**请求数据示例**：

```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

---

### 3. 修改密码

#### 基本信息

**接口描述**：修改当前登录用户的密码

**请求方式**：`POST`

**请求路径**：`/changePassword`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| (body) | String | 是 | 新密码字符串（直接传递字符串，非 JSON 对象） |

**请求数据示例**：

```json
"newPassword123"
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

---

### 4. 获取用户信息

#### 基本信息

**接口描述**：获取当前登录用户的详细信息

**请求方式**：`GET`

**请求路径**：`/info`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/user/info
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "uid": "U20250101000001",
        "username": "zhangsan",
        "nickname": "张三",
        "avatarUrl": "https://example.com/avatar.jpg",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "bio": "热爱生活",
        "resourceCodes": []
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| uid | String | 用户唯一标识 |
| username | String | 用户名 |
| nickname | String | 昵称 |
| avatarUrl | String | 头像 URL |
| phone | String | 手机号 |
| email | String | 邮箱 |
| bio | String | 个人简介 |
| resourceCodes | Array | 资源权限码列表 |

---

### 5. 更新用户信息

#### 基本信息

**接口描述**：更新当前登录用户的个人信息

**请求方式**：`PUT`

**请求路径**：`/info`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称 |
| avatarUrl | String | 否 | 头像 URL |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| bio | String | 否 | 个人简介 |

**请求数据示例**：

```json
{
    "nickname": "新昵称",
    "avatarUrl": "https://example.com/new-avatar.jpg",
    "phone": "13900139000"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

---

## 地址模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/address`

### 1. 添加地址

#### 基本信息

**接口描述**：为当前用户添加收货地址

**请求方式**：`POST`

**请求路径**：`/add`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiver | String | 是 | 收件人姓名 |
| regionCode | Integer | 是 | 区域编码 |
| fullAddress | String | 是 | 完整地址（省市区） |
| detail | String | 是 | 详细地址 |
| zipCode | String | 否 | 邮编 |
| phone | String | 是 | 联系电话 |
| isDefault | Boolean | 否 | 是否默认地址 |

**请求数据示例**：

```json
{
    "receiver": "张三",
    "regionCode": 440305,
    "fullAddress": "广东省深圳市南山区",
    "detail": "科技中路1号大厦A座1001",
    "zipCode": "518000",
    "phone": "13800138000",
    "isDefault": true
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 2. 删除地址

#### 基本信息

**接口描述**：删除指定的收货地址

**请求方式**：`DELETE`

**请求路径**：`/{id}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 3. 更新地址

#### 基本信息

**接口描述**：修改指定的收货地址信息

**请求方式**：`PUT`

**请求路径**：`/{id}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址 ID |

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiver | String | 否 | 收件人姓名 |
| regionCode | Integer | 否 | 区域编码 |
| fullAddress | String | 否 | 完整地址（省市区） |
| detail | String | 否 | 详细地址 |
| zipCode | String | 否 | 邮编 |
| phone | String | 否 | 联系电话 |
| isDefault | Boolean | 否 | 是否默认地址 |

**请求数据示例**：

```json
{
    "receiver": "张三",
    "fullAddress": "广东省深圳市南山区",
    "detail": "科技中路2号大厦B座2002",
    "phone": "13800138001"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 4. 查询用户地址列表

#### 基本信息

**接口描述**：获取当前登录用户的所有收货地址

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数**：无

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "receiver": "张三",
            "regionCode": 440305,
            "fullAddress": "广东省深圳市南山区",
            "detail": "科技中路1号大厦A座1001",
            "zipCode": "518000",
            "phone": "13800138000",
            "isDefault": true
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 地址 ID |
| receiver | String | 收件人姓名 |
| regionCode | Integer | 区域编码 |
| fullAddress | String | 完整地址（省市区） |
| detail | String | 详细地址 |
| zipCode | String | 邮编 |
| phone | String | 联系电话 |
| isDefault | Boolean | 是否默认地址 |

---

### 5. 获取默认地址

#### 基本信息

**接口描述**：获取当前用户的默认收货地址

**请求方式**：`GET`

**请求路径**：`/default`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/address/default
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "userId": 100,
        "receiver": "张三",
        "regionCode": 440305,
        "fullAddress": "广东省深圳市南山区",
        "detail": "科技中路1号大厦A座1001",
        "zipCode": "518000",
        "phone": "13800138000",
        "isDefault": true,
        "createdAt": "2025-12-20 10:30:00",
        "updatedAt": "2025-12-20 10:30:00"
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 地址 ID |
| userId | Long | 用户 ID |
| receiver | String | 收件人姓名 |
| regionCode | Integer | 区域编码 |
| fullAddress | String | 完整地址 |
| detail | String | 详细地址 |
| zipCode | String | 邮编 |
| phone | String | 联系电话 |
| isDefault | Boolean | 是否默认地址 |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

---

### 6. 设置默认地址

#### 基本信息

**接口描述**：将指定地址设为默认收货地址

**请求方式**：`PUT`

**请求路径**：`/setDefault/{id}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 轮播图模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/banner`

### 1. 获取推荐轮播图

#### 基本信息

**接口描述**：获取首页推荐轮播图列表

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/banner/
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "imageUrl": "https://example.com/banner1.jpg",
            "goodsId": 100
        },
        {
            "imageUrl": "https://example.com/banner2.jpg",
            "goodsId": 200
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| imageUrl | String | 轮播图图片 URL |
| goodsId | Long | 关联商品 ID |

---

## 购物车模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/cart`

### 1. 添加商品到购物车

#### 基本信息

**接口描述**：将指定 SKU 商品添加到购物车

**请求方式**：`POST`

**请求路径**：`/`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuId | Long | 是 | SKU ID（不能为空） |
| quantity | Long | 是 | 数量（不能为空，最小值为 1） |

**请求数据示例**：

```json
{
    "skuId": 1001,
    "quantity": 2
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "storeId": 10,
        "storeName": "华为官方旗舰店",
        "goodsId": 100,
        "goodsName": "华为 Mate 70",
        "skuId": 1001,
        "skuSpecs": {
            "颜色": "曜石黑",
            "内存": "256GB"
        },
        "price": 599900,
        "selected": true,
        "quantity": 2,
        "mainImage": "https://example.com/goods1.jpg",
        "unit": "件"
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| storeId | Long | 店铺 ID |
| storeName | String | 店铺名称 |
| goodsId | Long | 商品 ID |
| goodsName | String | 商品名称 |
| skuId | Long | SKU ID |
| skuSpecs | Map | 规格键值对（key: 规格名，value: 规格值） |
| price | Long | 单价（单位：分） |
| selected | Boolean | 是否选中 |
| quantity | Long | 数量 |
| mainImage | String | 商品主图 URL |
| unit | String | 单位 |

---

### 2. 获取购物车

#### 基本信息

**接口描述**：获取当前用户的购物车列表，按店铺分组

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/cart/
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "storeList": [
            {
                "storeId": 10,
                "storeName": "华为官方旗舰店",
                "items": [
                    {
                        "storeId": 10,
                        "storeName": "华为官方旗舰店",
                        "goodsId": 100,
                        "goodsName": "华为 Mate 70",
                        "skuId": 1001,
                        "skuSpecs": {
                            "颜色": "曜石黑",
                            "内存": "256GB"
                        },
                        "price": 599900,
                        "selected": true,
                        "quantity": 2,
                        "mainImage": "https://example.com/goods1.jpg",
                        "unit": "件"
                    }
                ]
            }
        ]
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| storeList | Array | 店铺分组列表 |
| storeList[].storeId | Long | 店铺 ID |
| storeList[].storeName | String | 店铺名称 |
| storeList[].items | Array | 该店铺下的购物车商品列表 |

---

### 3. 更新购物车项

#### 基本信息

**接口描述**：更新购物车中商品的数量

**请求方式**：`PUT`

**请求路径**：`/`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuId | Long | 是 | SKU ID（不能为空） |
| quantity | Long | 是 | 新数量（不能为空，最小值为 1） |

**请求数据示例**：

```json
{
    "skuId": 1001,
    "quantity": 5
}
```

#### 响应说明

**响应数据示例**：同「添加商品到购物车」响应结构

---

### 4. 删除购物车项

#### 基本信息

**接口描述**：根据 SKU ID 删除购物车中的单个商品

**请求方式**：`DELETE`

**请求路径**：`/{skuId}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuId | Long | 是 | SKU ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 5. 批量删除购物车项

#### 基本信息

**接口描述**：根据 SKU ID 列表批量删除购物车商品

**请求方式**：`DELETE`

**请求路径**：`/batch`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuIds | Array | 是 | SKU ID 列表 |

**请求数据示例**：

```json
{
    "skuIds": [1001, 1002, 1003]
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 6. 清空购物车

#### 基本信息

**接口描述**：清空当前用户的购物车

**请求方式**：`DELETE`

**请求路径**：`/clear`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
DELETE /web/cart/clear
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 商品模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/goods`

### 1. 根据分类获取商品列表

#### 基本信息

**接口描述**：根据分类 ID 获取商品列表

**请求方式**：`GET`

**请求路径**：`/listByCategory`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Long | 是 | 分类 ID |
| limit | Integer | 否 | 返回数量限制，默认 10 |

**请求数据示例**：

```
/web/goods/listByCategory?categoryId=1&limit=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "name": "华为 Mate 70",
            "categoryId": 10,
            "unitName": "件",
            "sellPoint": "旗舰芯片，超强影像",
            "storeId": 10,
            "storeName": "华为官方旗舰店",
            "sales": 5000,
            "minPrice": 599900,
            "maxPrice": 799900,
            "status": true
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商品 ID |
| name | String | 商品名称 |
| categoryId | Long | 分类 ID |
| unitName | String | 单位名称 |
| sellPoint | String | 卖点 |
| storeId | Long | 店铺 ID |
| storeName | String | 店铺名称 |
| sales | Integer | 销量 |
| minPrice | Long | 最低价（单位：分） |
| maxPrice | Long | 最高价（单位：分） |
| status | Boolean | 上架状态 |

---

### 2. 商品搜索

#### 基本信息

**接口描述**：根据关键词、分类、价格等条件搜索商品

**请求方式**：`GET`

**请求路径**：`/search`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词 |
| categoryId | Long | 否 | 分类 ID |
| minPrice | String | 否 | 最低价格 |
| maxPrice | String | 否 | 最高价格 |
| sortType | String | 否 | 排序方式，默认 `comprehensive`（综合排序） |
| isDesc | Boolean | 否 | 是否降序 |

**请求数据示例**：

```
/web/goods/search?keyword=手机&categoryId=1&minPrice=1000&maxPrice=5000&page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "name": "华为 Mate 70",
                "sellPoint": "旗舰芯片，超强影像",
                "mainImageUrl": "https://example.com/goods1.jpg",
                "minPrice": "5999.00",
                "sale": 5000
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    }
}
```

**响应字段说明（records）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商品 ID |
| name | String | 商品名称 |
| sellPoint | String | 卖点 |
| mainImageUrl | String | 商品主图 URL |
| minPrice | String | 最低价（单位：元） |
| sale | Integer | 销量 |

---

### 3. 店铺内商品搜索

#### 基本信息

**接口描述**：在指定店铺内搜索商品

**请求方式**：`GET`

**请求路径**：`/store/{storeId}/search`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| storeId | Long | 是 | 店铺 ID |

**请求参数（Query String）**：同「商品搜索」

**请求数据示例**：

```
/web/goods/store/10/search?keyword=手机&page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：同「商品搜索」响应结构

---

### 4. 获取商品详情

#### 基本信息

**接口描述**：获取商品的完整详情，包含 SPU 信息、店铺信息、规格列表、SKU 列表

**请求方式**：`GET`

**请求路径**：`/detail/{id}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "spu": {
            "id": 1,
            "goodsName": "华为 Mate 70",
            "sellPoint": "旗舰芯片，超强影像",
            "displayImageUrls": [
                "https://example.com/display1.jpg",
                "https://example.com/display2.jpg"
            ],
            "descriptionImageUrls": [
                "https://example.com/desc1.jpg"
            ],
            "createTime": "2025-12-01T10:00:00",
            "sale": 5000,
            "positiveRate": "98.5%"
        },
        "storeInfo": {
            "storeId": 10,
            "storeName": "华为官方旗舰店",
            "storeAvatarUrl": "https://example.com/store-avatar.jpg"
        },
        "specifications": [
            {
                "name": "颜色",
                "values": [
                    { "id": 1, "name": "曜石黑" },
                    { "id": 2, "name": "星河银" }
                ]
            },
            {
                "name": "内存",
                "values": [
                    { "id": 3, "name": "256GB" },
                    { "id": 4, "name": "512GB" }
                ]
            }
        ],
        "skus": [
            {
                "id": 1001,
                "specValueIds": [1, 3],
                "price": "5999.00",
                "inventory": 100,
                "status": true
            },
            {
                "id": 1002,
                "specValueIds": [1, 4],
                "price": "6999.00",
                "inventory": 50,
                "status": true
            }
        ]
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| spu | Object | SPU 基本信息 |
| spu.id | Long | 商品 ID |
| spu.goodsName | String | 商品名称 |
| spu.sellPoint | String | 卖点 |
| spu.displayImageUrls | Array | 展示图片 URL 列表 |
| spu.descriptionImageUrls | Array | 详情描述图片 URL 列表 |
| spu.createTime | DateTime | 上架时间 |
| spu.sale | Integer | 总销量 |
| spu.positiveRate | String | 好评率 |
| storeInfo | Object | 店铺信息 |
| storeInfo.storeId | Long | 店铺 ID |
| storeInfo.storeName | String | 店铺名称 |
| storeInfo.storeAvatarUrl | String | 店铺头像 URL |
| specifications | Array | 规格列表 |
| specifications[].name | String | 规格名称 |
| specifications[].values | Array | 规格值列表 |
| skus | Array | SKU 列表 |
| skus[].id | Long | SKU ID |
| skus[].specValueIds | Array | 规格值 ID 组合 |
| skus[].price | String | 价格（单位：元） |
| skus[].inventory | Long | 库存 |
| skus[].status | Boolean | 是否可用 |

---

## 分类模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/category`

### 1. 获取分类列表

#### 基本信息

**接口描述**：获取所有启用的分类列表

**请求方式**：`GET`

**请求路径**：`/list`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/category/list
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "name": "电子产品",
            "parentId": 0,
            "level": 1,
            "sort": 1,
            "status": true
        },
        {
            "id": 10,
            "name": "手机",
            "parentId": 1,
            "level": 2,
            "sort": 1,
            "status": true
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 分类 ID |
| name | String | 分类名称 |
| parentId | Long | 父分类 ID |
| level | Integer | 层级 |
| sort | Integer | 排序 |
| status | Boolean | 状态 |

---

### 2. 获取分类商品板块

#### 基本信息

**接口描述**：获取首页分类商品板块数据，按分类分组返回商品列表

**请求方式**：`GET`

**请求路径**：`/category-goods`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/category/category-goods
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "category": {
                "id": 1,
                "name": "电子产品"
            },
            "tabs": [
                { "id": 10, "name": "手机" },
                { "id": 11, "name": "平板" }
            ],
            "goodsMap": {
                "10": [
                    {
                        "id": 1,
                        "name": "华为 Mate 70",
                        "sellPoint": "旗舰芯片，超强影像",
                        "mainImageUrl": "https://example.com/goods1.jpg",
                        "minPrice": "5999.00",
                        "sale": 5000
                    }
                ],
                "11": []
            }
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| category | Object | 当前分类信息 |
| tabs | Array | 子分类标签列表 |
| goodsMap | Object | 商品映射（key 为子分类 ID，value 为商品卡片列表） |

---

## 评论模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/comments`

### 1. 创建商品评论

#### 基本信息

**接口描述**：对已购买的商品进行评论

**请求方式**：`POST`

**请求路径**：`/add`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单号（不能为空） |
| orderItemId | Long | 是 | 订单明细 ID（不能为空） |
| goodsId | Long | 是 | 商品 ID（不能为空） |
| rating | Integer | 是 | 评分（不能为空，范围 1-5） |
| content | String | 是 | 评论内容（不能为空） |
| imageUrls | Array | 否 | 评论图片 URL 列表 |
| isAnonymous | Boolean | 否 | 是否匿名，默认 false |
| specs | String | 是 | 规格快照（不能为空） |

**请求数据示例**：

```json
{
    "orderNo": "20250101000001",
    "orderItemId": 100,
    "goodsId": 1,
    "rating": 5,
    "content": "商品质量很好，非常满意！",
    "imageUrls": [
        "https://example.com/comment1.jpg"
    ],
    "isAnonymous": false,
    "specs": "颜色:曜石黑 内存:256GB"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

---

### 2. 分页查询商品评论

#### 基本信息

**接口描述**：根据商品 ID 分页查询评论列表

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| goodsId | Long | 是 | 商品 ID |
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |

**请求数据示例**：

```
/web/comments?goodsId=1&page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "orderItemId": 100,
                "orderId": 50,
                "goodsId": 1,
                "goodsName": "华为 Mate 70",
                "goodsImage": "https://example.com/goods1.jpg",
                "userId": 1000,
                "userNickname": "张三",
                "userAvatar": "https://example.com/avatar.jpg",
                "rating": 5,
                "content": "商品质量很好，非常满意！",
                "reply": null,
                "imageUrls": [
                    "https://example.com/comment1.jpg"
                ],
                "isAnonymous": false,
                "createTime": "2025-12-25 14:30:00",
                "specs": "颜色:曜石黑 内存:256GB"
            }
        ],
        "total": 200,
        "size": 10,
        "current": 1,
        "pages": 20
    }
}
```

**响应字段说明（records）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 评论 ID |
| orderItemId | Long | 订单明细 ID |
| orderId | Long | 订单 ID |
| goodsId | Long | 商品 ID |
| goodsName | String | 商品名称 |
| goodsImage | String | 商品图片 |
| userId | Long | 评论用户 ID |
| userNickname | String | 用户昵称 |
| userAvatar | String | 用户头像 |
| rating | Integer | 评分（1-5） |
| content | String | 评论内容 |
| reply | String | 商家回复 |
| imageUrls | Array | 评论图片列表 |
| isAnonymous | Boolean | 是否匿名 |
| createTime | DateTime | 评论时间 |
| specs | String | 规格快照 |

---

## 收藏模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/favorites`

### 1. 分页获取收藏列表

#### 基本信息

**接口描述**：分页获取当前用户的所有收藏商品

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |

**请求数据示例**：

```
GET /web/favorites/?page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "favoriteId": 1,
                "goodsId": 100,
                "goodsName": "华为 Mate 70",
                "goodsMainImageUrl": "https://example.com/goods1.jpg",
                "goodsPrice": "5999.00",
                "goodsSellPoint": "旗舰芯片，超强影像"
            }
        ],
        "total": 20,
        "size": 10,
        "current": 1,
        "pages": 2
    }
}
```

**响应字段说明（records）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| favoriteId | Long | 收藏记录 ID |
| goodsId | Long | 商品 ID |
| goodsName | String | 商品名称 |
| goodsMainImageUrl | String | 商品主图 URL |
| goodsPrice | String | 商品价格（单位：元） |
| goodsSellPoint | String | 商品卖点 |

---

### 2. 添加收藏

#### 基本信息

**接口描述**：收藏指定商品

**请求方式**：`POST`

**请求路径**：`/{goodsId}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| goodsId | Long | 是 | 商品 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 3. 取消收藏

#### 基本信息

**接口描述**：取消收藏指定商品

**请求方式**：`DELETE`

**请求路径**：`/{goodsId}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| goodsId | Long | 是 | 商品 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 4. 检查收藏状态

#### 基本信息

**接口描述**：检查当前用户是否已收藏指定商品

**请求方式**：`GET`

**请求路径**：`/status/{goodsId}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| goodsId | Long | 是 | 商品 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "isFavorite": true
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| isFavorite | boolean | 是否已收藏 |

---

## 文件模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/files`

### 1. 上传文件

#### 基本信息

**接口描述**：上传文件到服务器（如头像、评论图片等）

**请求方式**：`POST`

**请求路径**：`/upload`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | multipart/form-data |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 要上传的文件 |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "url": "https://minio.example.com/bucket/abc123.jpg"
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| url | String | 文件访问 URL |

---

## 公告模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/notice`

### 1. 获取公告列表

#### 基本信息

**接口描述**：获取所有公告列表

**请求方式**：`GET`

**请求路径**：`/`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/notice/
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "content": "系统将于今晚凌晨2点进行维护升级"
        },
        {
            "id": 2,
            "content": "新年大促活动即将开始，敬请期待！"
        }
    ]
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 公告 ID |
| content | String | 公告内容 |

---

## 订单模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/order`

### 1. 创建订单

#### 基本信息

**接口描述**：创建订单，支持购物车购买和立即购买两种模式

**请求方式**：`POST`

**请求路径**：`/create/{cartType}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cartType | String | 是 | 购买方式：`cart`（购物车购买）或 `direct`（立即购买） |

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| addressId | Long | 是 | 收货地址 ID（不能为空） |
| tradeItems | Array | 是 | 交易商品列表（不能为空） |
| tradeItems[].storeId | Long | 是 | 店铺 ID |
| tradeItems[].tradeShopItemList | Array | 是 | 店铺内商品列表 |
| tradeItems[].tradeShopItemList[].skuId | Long | 是 | SKU ID |
| tradeItems[].tradeShopItemList[].quantity | Integer | 是 | 购买数量 |

**请求数据示例**：

```json
{
    "addressId": 1,
    "tradeItems": [
        {
            "storeId": 10,
            "tradeShopItemList": [
                {
                    "skuId": 1001,
                    "quantity": 2
                },
                {
                    "skuId": 1002,
                    "quantity": 1
                }
            ]
        }
    ]
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "orderNo": "20250101000001",
        "expireTime": "2025-01-01T10:30:00",
        "payQrCode": "data:image/png;base64,..."
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| orderNo | String | 订单号 |
| expireTime | DateTime | 支付截止时间 |
| payQrCode | String | 支付二维码（Base64 编码图片） |

---

### 2. 分页查询订单

#### 基本信息

**接口描述**：分页查询当前用户的订单列表（聚合视图，包含子订单和商品明细）

**请求方式**：`GET`

**请求路径**：`/page`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| orderNo | String | 否 | 订单号（精确匹配） |
| status | String | 否 | 订单状态（见附录 OrderStatus 枚举） |
| orderType | String | 否 | 订单类型（见附录 OrderType 枚举） |
| parentId | Long | 否 | 父订单 ID |

**请求数据示例**：

```
GET /web/order/page?page=1&pageSize=10&status=PAID
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "orderNo": "P20250101000001",
                "status": "PAID",
                "createTime": "2025-01-01T10:00:00",
                "expireTime": "2025-01-01T10:30:00",
                "reason": null,
                "totalPrice": "17997.00",
                "count": 3,
                "storeOrders": [
                    {
                        "orderNo": "S20250101000001",
                        "storeId": 10,
                        "storeName": "华为官方旗舰店",
                        "status": "PAID",
                        "totalPrice": "17997.00",
                        "count": 3,
                        "items": [
                            {
                                "id": 1,
                                "goodsId": 100,
                                "goodsName": "华为 Mate 70",
                                "goodsMainImageUrl": "https://example.com/goods1.jpg",
                                "goodsPrice": "5999.00",
                                "quantity": 2,
                                "totalPrice": "11998.00",
                                "commentStatus": false,
                                "selectedSpecs": {
                                    "颜色": "曜石黑",
                                    "内存": "256GB"
                                }
                            }
                        ]
                    }
                ]
            }
        ],
        "total": 50,
        "size": 10,
        "current": 1,
        "pages": 5
    }
}
```

**响应字段说明（records）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| orderNo | String | 订单号（父订单号或普通订单号） |
| status | String | 订单状态 |
| createTime | DateTime | 下单时间 |
| expireTime | DateTime | 支付截止时间 |
| reason | String | 取消/关闭原因 |
| totalPrice | String | 订单总价（单位：元） |
| count | Long | 商品总数量 |
| storeOrders | Array | 店铺子订单列表 |
| storeOrders[].orderNo | String | 子订单号 |
| storeOrders[].storeId | Long | 店铺 ID |
| storeOrders[].storeName | String | 店铺名称 |
| storeOrders[].status | String | 子订单状态 |
| storeOrders[].totalPrice | String | 子订单总价（单位：元） |
| storeOrders[].count | Long | 子订单商品数量 |
| storeOrders[].items | Array | 商品明细列表 |
| storeOrders[].items[].id | Long | 商品明细 ID |
| storeOrders[].items[].goodsId | Long | 商品 ID |
| storeOrders[].items[].goodsName | String | 商品名称快照 |
| storeOrders[].items[].goodsMainImageUrl | String | 商品主图 URL |
| storeOrders[].items[].goodsPrice | String | 商品单价（单位：元） |
| storeOrders[].items[].quantity | Integer | 购买数量 |
| storeOrders[].items[].totalPrice | String | 明细小计（单位：元） |
| storeOrders[].items[].commentStatus | Boolean | 是否已评价 |
| storeOrders[].items[].selectedSpecs | Map | 选择的规格（key: 规格名，value: 规格值） |

---

### 3. 查询支付状态

#### 基本信息

**接口描述**：查询订单的支付状态

**请求方式**：`GET`

**请求路径**：`/payment/status`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单号 |

**请求数据示例**：

```
GET /web/order/payment/status?orderNo=20250101000001
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

---

### 4. 取消订单

#### 基本信息

**接口描述**：取消订单（订单状态为 CREATED 时可操作）

**请求方式**：`POST`

**请求路径**：`/cancel`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单号 |
| reason | String | 否 | 取消原因 |

**请求数据示例**：

```json
{
    "orderNo": "20250101000001",
    "reason": "不想要了"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

### 5. 确认收货

#### 基本信息

**接口描述**：确认收货，将订单状态从 SHIPPED 改为 FINISHED

**请求方式**：`POST`

**请求路径**：`/confirm/{orderNo}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单编号 |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 店铺模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/store`

### 1. 获取店铺信息

#### 基本信息

**接口描述**：获取店铺基本信息

**请求方式**：`GET`

**请求路径**：`/info/{storeId}`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| storeId | Long | 是 | 店铺 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "10",
        "name": "华为官方旗舰店",
        "description": "华为官方直营店，品质保证",
        "avatarUrl": "https://example.com/store-avatar.jpg",
        "banner": "https://example.com/store-banner.jpg"
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | String | 店铺 ID |
| name | String | 店铺名称 |
| description | String | 店铺描述 |
| avatarUrl | String | 店铺头像 URL |
| banner | String | 店铺横幅 URL |

---

### 2. 分页获取店铺商品

#### 基本信息

**接口描述**：分页获取指定店铺的商品列表

**请求方式**：`GET`

**请求路径**：`/goods`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| storeId | Long | 是 | 店铺 ID |
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |

**请求数据示例**：

```
GET /web/store/goods?storeId=10&page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": 1,
                "name": "华为 Mate 70",
                "sellPoint": "旗舰芯片，超强影像",
                "mainImageUrl": "https://example.com/goods1.jpg",
                "minPrice": "5999.00",
                "sale": 5000
            }
        ],
        "total": 50,
        "size": 10,
        "current": 1,
        "pages": 5
    }
}
```

---

### 3. 查询入驻申请状态

#### 基本信息

**接口描述**：查询当前用户的店铺入驻申请审核状态

**请求方式**：`GET`

**请求路径**：`/application/status`

#### 请求说明

**请求参数**：无

**请求数据示例**：

```
GET /web/store/application/status
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "status": "PENDING",
        "storeNo": "STORE20250101000001"
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| status | String | 审核状态 |
| storeNo | String | 店铺编号 |

---

### 4. 提交入驻申请

#### 基本信息

**接口描述**：提交店铺入驻申请

**请求方式**：`POST`

**请求路径**：`/create`

#### 请求说明

**请求头参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| subjectType | String | 是 | 主体类型 |
| realName | String | 是 | 真实姓名 |
| idCard | String | 是 | 身份证号 |
| idCardValidStart | Date | 否 | 身份证有效期开始 |
| idCardValidEnd | Date | 否 | 身份证有效期结束 |
| idCardFront | String | 否 | 身份证正面照片 URL |
| idCardBack | String | 否 | 身份证背面照片 URL |
| licenseNumber | String | 否 | 营业执照编号 |
| licenseName | String | 否 | 营业执照名称 |
| establishmentDate | Date | 否 | 成立日期 |
| registeredAddress | String | 否 | 注册地址 |
| licensePhoto | String | 否 | 营业执照照片 URL |
| storeName | String | 是 | 店铺名称 |
| categories | Array | 否 | 经营分类 ID 列表 |
| shippingAddress | String | 是 | 发货地址 |
| bankAccountName | String | 否 | 银行账户名 |
| bankCardNumber | String | 否 | 银行卡号 |
| bankName | String | 否 | 开户银行 |
| bankBranchName | String | 否 | 支行名称 |
| bankMobile | String | 否 | 银行预留手机号 |

**请求数据示例**：

```json
{
    "subjectType": "PERSONAL",
    "realName": "张三",
    "idCard": "440305199001010001",
    "idCardValidStart": "2020-01-01",
    "idCardValidEnd": "2030-01-01",
    "idCardFront": "https://example.com/id-front.jpg",
    "idCardBack": "https://example.com/id-back.jpg",
    "storeName": "张三的小店",
    "categories": [10, 11],
    "shippingAddress": "广东省深圳市南山区科技中路1号"
}
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 秒杀模块

### 模块信息

**当前模块接口请求公共路径前缀**：`/web/seckill`

### 1. 获取当前整点秒杀活动商品

#### 基本信息

**接口描述**：获取当前整点的秒杀活动及商品分页列表

**请求方式**：`GET`

**请求路径**：`/activities/current-hour/goods/list`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| activityId | Long | 否 | 活动 ID |
| merchantId | Long | 否 | 商户 ID |

**请求数据示例**：

```
GET /web/seckill/activities/current-hour/goods/list?page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "activity": {
            "id": 1,
            "name": "14点整秒杀专场",
            "startHour": 14,
            "activityDate": "2026-05-07",
            "status": 1,
            "maxItems": 20,
            "createTime": "2026-05-06T10:00:00",
            "updateTime": "2026-05-06T10:00:00"
        },
        "goodsPage": {
            "records": [
                {
                    "id": 1,
                    "auditItemId": 100,
                    "status": "APPROVED",
                    "skuId": 1001,
                    "goodsName": "华为 Mate 70 秒杀版",
                    "mainImageUrl": "https://example.com/seckill1.jpg",
                    "seckillPrice": 3999.00,
                    "stock": 100
                }
            ],
            "total": 10,
            "size": 10,
            "current": 1,
            "pages": 1
        }
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| activity | Object | 秒杀活动信息 |
| activity.id | Long | 活动 ID |
| activity.name | String | 活动名称 |
| activity.startHour | Integer | 开始整点（0-23） |
| activity.activityDate | String | 活动日期（yyyy-MM-dd） |
| activity.status | Integer | 活动状态：0-报名中，1-进行中，2-已结束 |
| activity.maxItems | Integer | 最大商品数 |
| goodsPage | Object | 秒杀商品分页数据 |
| goodsPage.records | Array | 秒杀商品列表 |
| goodsPage.records[].id | Long | 秒杀商品 ID |
| goodsPage.records[].skuId | Long | SKU ID |
| goodsPage.records[].status | String | 审核状态 |
| goodsPage.records[].goodsName | String | 商品名称 |
| goodsPage.records[].mainImageUrl | String | 商品主图 |
| goodsPage.records[].seckillPrice | BigDecimal | 秒杀价格 |
| goodsPage.records[].stock | Integer | 秒杀库存 |

---

### 2. 获取指定时间整点秒杀活动商品

#### 基本信息

**接口描述**：获取指定时间对应整点的秒杀活动及商品列表

**请求方式**：`GET`

**请求路径**：`/activities/hour/goods/list`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetTime | DateTime | 是 | 指定时间（ISO 格式，如 2026-05-07T14:20:00） |
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| activityId | Long | 否 | 活动 ID |
| merchantId | Long | 否 | 商户 ID |

**请求数据示例**：

```
GET /web/seckill/activities/hour/goods/list?targetTime=2026-05-07T14:20:00&page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：同「获取当前整点秒杀活动商品」

---

### 3. 获取当天全部场次秒杀活动商品

#### 基本信息

**接口描述**：获取当天全部场次的秒杀活动及商品列表

**请求方式**：`GET`

**请求路径**：`/activities/today/goods/list`

#### 请求说明

**请求参数（Query String）**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| activityId | Long | 否 | 活动 ID |
| merchantId | Long | 否 | 商户 ID |

**请求数据示例**：

```
GET /web/seckill/activities/today/goods/list?page=1&pageSize=10
```

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "activity": {
                "id": 1,
                "name": "10点整秒杀专场",
                "startHour": 10,
                "activityDate": "2026-05-07",
                "status": 2,
                "maxItems": 20,
                "createTime": "2026-05-06T10:00:00",
                "updateTime": "2026-05-06T10:00:00"
            },
            "goodsPage": {
                "records": [],
                "total": 0,
                "size": 10,
                "current": 1,
                "pages": 0
            }
        },
        {
            "activity": {
                "id": 2,
                "name": "14点整秒杀专场",
                "startHour": 14,
                "activityDate": "2026-05-07",
                "status": 1,
                "maxItems": 20,
                "createTime": "2026-05-06T10:00:00",
                "updateTime": "2026-05-06T10:00:00"
            },
            "goodsPage": {
                "records": [],
                "total": 0,
                "size": 10,
                "current": 1,
                "pages": 0
            }
        }
    ]
}
```

---

### 4. 获取秒杀商品详情

#### 基本信息

**接口描述**：获取秒杀商品的聚合详情（秒杀信息 + SPU 详情）

**请求方式**：`GET`

**请求路径**：`/product/{id}/detail`

#### 请求说明

**路径参数**：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 秒杀商品 ID |

#### 响应说明

**响应数据示例**：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "seckillGoodsId": 1,
        "activityId": 1,
        "goodsId": 100,
        "selectedSkuId": 1001,
        "goodsName": "华为 Mate 70 秒杀版",
        "mainImageUrl": "https://example.com/seckill1.jpg",
        "seckillPrice": 3999.00,
        "stock": 100,
        "soldCount": 20,
        "remainingStock": 80,
        "goodsDetail": {
            "spu": {
                "id": 100,
                "goodsName": "华为 Mate 70",
                "sellPoint": "旗舰芯片，超强影像",
                "displayImageUrls": ["https://example.com/display1.jpg"],
                "descriptionImageUrls": ["https://example.com/desc1.jpg"],
                "createTime": "2025-12-01T10:00:00",
                "sale": 5000,
                "positiveRate": "98.5%"
            },
            "storeInfo": {
                "storeId": 10,
                "storeName": "华为官方旗舰店",
                "storeAvatarUrl": "https://example.com/store-avatar.jpg"
            },
            "specifications": [
                {
                    "name": "颜色",
                    "values": [
                        { "id": 1, "name": "曜石黑" }
                    ]
                }
            ],
            "skus": [
                {
                    "id": 1001,
                    "specValueIds": [1],
                    "price": "5999.00",
                    "inventory": 100,
                    "status": true
                }
            ]
        }
    }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| seckillGoodsId | Long | 秒杀商品 ID |
| activityId | Long | 秒杀活动 ID |
| goodsId | Long | 商品 ID |
| selectedSkuId | Long | 选中的 SKU ID |
| goodsName | String | 商品名称 |
| mainImageUrl | String | 商品主图 |
| seckillPrice | BigDecimal | 秒杀价格 |
| stock | Integer | 秒杀总库存 |
| soldCount | Integer | 已售数量 |
| remainingStock | Integer | 剩余库存 |
| goodsDetail | Object | 商品详情（结构同「获取商品详情」） |

---

## 附录

### 枚举值定义

#### OrderStatus（订单状态）

| 枚举值 | 说明 |
|--------|------|
| CREATED | 待支付 |
| PAID | 待发货 |
| SHIPPED | 待收货 |
| FINISHED | 已完成 |
| CANCELED | 已取消 |
| CLOSED | 已关闭 |

#### OrderType（订单类型）

| 枚举值 | 说明 |
|--------|------|
| PARENT | 父订单（多店铺聚合） |
| SUB | 子订单（父订单下的单店铺订单） |
| NORMAL | 普通订单（单店铺场景） |

#### PurchaseMode（购买方式）

| 枚举值 | 说明 |
|--------|------|
| CART_BUY | 购物车购买 |
| INSTANT_BUY | 立即购买 |

#### AccountType（账号类型）

| 枚举值 | 说明 |
|--------|------|
| NORMAL | 普通用户 |
| MERCHANT | 商户端 |
| ADMIN | 管理员 |

#### AuditBizType（审核业务类型）

| 枚举值 | 说明 |
|--------|------|
| GOODS | 商品 |
| STORE_REGISTER | 店铺注册 |
| SECKILL_GOODS | 秒杀活动 |
