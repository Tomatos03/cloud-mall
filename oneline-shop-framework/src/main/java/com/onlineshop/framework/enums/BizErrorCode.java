package com.onlineshop.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 * 统一管理系统中所有业务异常的错误码和错误信息
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Getter
@AllArgsConstructor
public enum BizErrorCode {
    
    // ==================== 通用错误码 (1xxx) ====================
    INVALID_PARAM(1000, "请求参数无效"),

    // ==================== 用户认证相关错误码 (10xxx) ====================
    USERNAME_OR_PASSWORD_ERROR(10000, "用户名或密码错误"),
    UNKNOWN_ROLE(10001, "未知角色"),
    USER_ALREADY_EXISTS(10002, "用户已经存在"),
    USER_NOT_EXISTS(10003, "用户不存在"),
    PASSWORD_NOT_MATCH(10004, "密码不匹配"),

    // ==================== 商品分类相关错误码 (20xxx) ====================
    CATEGORY_BEYOND_MAX_LEVEL(20000, "分类层级超出最大限制"),
    GOODS_OR_SHOP_NOT_EXIST(20001, "商品或店铺不存在"),

    // ==================== 地址相关错误码 (30xxx) ====================
    ADDRESS_NOT_EXIST(30000, "地址不存在"),

    // ==================== 搜索相关错误码 (40xxx) ====================
    UNKNOWN_SEARCH_ORDER_TYPE(40000, "未知的搜索排序类型"),

    // ==================== 订单相关错误码 (50xxx) ====================
    // 订单创建相关
    CREATE_ORDER_FAILURE(50000, "创建订单失败"),
    ORDER_DATA_IS_NULL(50001, "订单数据为空"),
    ORDER_GOODS_ID_IS_NULL(50002, "商品ID为空"),
    ORDER_QUANTITY_INVALID(50003, "商品数量无效"),
    GOODS_NOT_IN_CART(50004, "购物车中不存在该商品"),
    GOODS_NOT_EXIST(50005, "商品不存在"),
    GOODS_OFF_SHELF(50006, "商品已下架"),
    GOODS_STOCK_INSUFFICIENT(50007, "商品库存不足"),
    ORDER_CREATE_FAILED(50012, "订单创建失败"),
    
    // 订单状态相关
    INVALID_ORDER_STATUS(50008, "无效的订单状态"),
    ORDER_NOT_EXIST(50009, "订单不存在"),
    ORDER_NOT_BELONG_TO_USER(50010, "订单不属于当前用户"),
    INVALID_ORDER_TYPE(50013, "无效的订单类型"),
    ORDER_STATUS_NOT_PAID(50014, "订单状态不是待发货，无法发货"),
    ORDER_STATUS_NOT_CREATED(50015, "订单状态不是待支付，无法取消"),
    ORDER_STATUS_NOT_FINISHED(50017, "订单未完成，无法进行此操作"),
    
    // 订单评价相关
    ORDER_NO_COMMENT(50016, "订单无买家评价，无法回复"),
    ORDER_ITEM_NOT_EXIST(50018, "订单明细不存在"),
    
    // 已废弃或很少使用
    USER_ID_IS_NULL(50011, "用户ID不能为空"),  // 拦截器保证非空，不再使用

    // ==================== 购物车相关错误码 (60xxx) ====================
    UNKNOW_CART_TYPE(60000, "未知的购物车类型"),
    CART_GOODS_NOT_EXIST(60001, "商品不存在"),
    CART_GOODS_NOT_BELONG_TO_STORE(60002, "商品不属于该店铺"),
    CART_GOODS_OFF_SHELF(60003, "商品已下架"),
    CART_GOODS_STOCK_INSUFFICIENT(60004, "商品库存不足"),
    CART_STORE_NOT_EXIST(60005, "店铺不存在"),
    CART_ITEM_NOT_EXIST(60006, "购物车中不存在该商品"),

    // ==================== 商品评论相关错误码 (70xxx) ====================
    // 评论参数验证相关
    COMMENT_RATING_INVALID(70000, "评分必须在1-5之间"),
    COMMENT_CONTENT_EMPTY(70001, "评论内容不能为空"),
    COMMENT_GOODS_NAME_EMPTY(70002, "商品名称不能为空"),
    
    // 评论数据相关
    COMMENT_NOT_FOUND(70003, "评论不存在"),
    COMMENT_ALREADY_EXISTS(70004, "评论已存在，无法重复评价"),
    
    // 评论权限相关
    COMMENT_PERMISSION_DENIED(70005, "无权删除他人的评论"),
    
    // 评论关联数据相关
    COMMENT_ORDER_ITEM_NOT_EXIST(70006, "订单明细不存在，无法评论"),
    COMMENT_GOODS_NOT_EXIST(70007, "商品不存在，无法评论"),
    COMMENT_USER_NOT_EXIST(70008, "用户不存在，无法评论"),

    // ==================== 文件上传相关错误码 (80xxx) ====================
    // 文件验证相关
    FILE_EMPTY(80000, "文件不能为空"),
    FILE_SIZE_EXCEEDS_LIMIT(80001, "文件大小不能超过5MB"),
    FILE_EXTENSION_NOT_ALLOWED(80002, "只允许jpg和png格式"),
    FILE_MIME_TYPE_INVALID(80003, "文件类型不合法"),
    
    // 文件操作相关
    FILE_SAVE_FAILED(80004, "文件保存失败"),
    UPLOAD_DIR_CREATE_FAILED(80005, "无法创建上传目录"),
    
    // ==================== 权限和店铺相关错误码 (500xx) ====================
    NO_PERMISSION(500014, "无权访问"),
    STORE_NOT_EXIST(500015, "店铺不存在"),
    MERCHANT_NO_SHOP(500016, "商家没有关联这个店铺"),
    INSUFFICIENT_PERMISSIONS(500017, "权限不足"),
    INVALID_ROLE(500018, "无效的用户角色");

    final int code;
    final String errorMessage;
}