1. 项目采用功能分包，所有业务模块统一放在 oneline-shop-framework/models 目录下。
2. 某类（如 service、mapper、dto、vo 等）如果数量大于1，才为其创建目录。  
   - service 接口放在 service 目录，具体实现放在 service/impl 子目录。
   - mapper、dto、vo 也按此规则分目录。
3. Service 接口命名以 I 开头（如 IGoodsService），实现类去掉 I 前缀（如 GoodsService）。
4. ORM 框架统一使用 MyBatis-Plus。
5. 已注册全局结果处理器，controller 返回结果无需额外包装。
6. 统一使用 Lombok 注解简化 getter/setter 等方法。
7. 业务异常统一抛出 BusinessException，必须配合 BizErrorCode 枚举类。
8. 获取当前登录用户信息统一通过 UserContextHolder 工具类。
9. AppService（应用服务）如有多个 public 方法，需定义接口（如 IGoodsAppService），实现类实现该接口，接口只声明非私有方法。
10. 禁止创建总结文档，直接说明改动或重构内容即可。
11. 禁止使用全类名，必须 import 后再用类名。
12. 已注册公共字段自动填充处理器，无需重复处理。


项目数据库表定义SQL参考:
CREATE TABLE `address` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
`user_id` bigint(20) NOT NULL COMMENT '用户ID',
`receiver` varchar(100) NOT NULL COMMENT '收货人名字',
`region_code` int(11) DEFAULT NULL COMMENT '地区代码',
`detail` varchar(255) NOT NULL COMMENT '详细地址',
`full_address` varchar(255) NOT NULL COMMENT '完整地址',
`zip_code` varchar(6) NOT NULL COMMENT '邮编(6位数字)',
`phone` varchar(11) NOT NULL COMMENT '联系电话(11位手机号)',
`is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否为默认地址',
`created_at` timestamp NULL DEFAULT current_timestamp() COMMENT '创建时间',
`updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
PRIMARY KEY (`id`),
KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='地址管理';

CREATE TABLE `audit` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
`target_type` varchar(50) NOT NULL COMMENT '被审核对象类型: GOODS / SKU / OTHER',
`target_id` bigint(20) NOT NULL COMMENT '被审核对象ID',
`status` char(20) NOT NULL COMMENT '审核状态: 待审核, 通过, 拒绝, 已撤销',
`reason` varchar(255) DEFAULT NULL COMMENT '审核备注/拒绝原因',
`applicant_id` bigint(20) NOT NULL COMMENT '申请人(店铺)ID',
`applicant_name` varchar(255) DEFAULT NULL COMMENT '申请人(店铺)姓名',
`auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
`auditor_name` varchar(255) DEFAULT NULL COMMENT '审核人姓名',
`extra_info` longtext CHARACTER SET utf8mb4 COLLATE=utf8mb4_bin DEFAULT NULL COMMENT '扩展信息: 可存储SKU组合/商品规格等JSON' CHECK (json_valid(`extra_info`)),
`create_time` datetime DEFAULT current_timestamp() COMMENT '申请时间',
`audit_time` datetime DEFAULT NULL COMMENT '审核时间',
PRIMARY KEY (`id`),
KEY `idx_target` (`target_type`,`target_id`),
KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用审核表';

CREATE TABLE `banner` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`image_url` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
`goods_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID',
`goods_name` varchar(255) NOT NULL COMMENT '关联商品名称',
`is_recommend` bit(1) NOT NULL COMMENT '是否在首页推荐',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='轮播图表';

CREATE TABLE `category` (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`name` varchar(100) NOT NULL COMMENT '分类名称',
`parent_id` int(10) unsigned NOT NULL COMMENT '父级分类ID',
`level` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '分类层级',
`sort` int(11) NOT NULL COMMENT '排序值',
`status` bit(1) NOT NULL DEFAULT b'1' COMMENT '状态（1:启用, 0:禁用）',
PRIMARY KEY (`id`),
KEY `idx_parentId` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='商品分类表';

CREATE TABLE `favorite` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏记录唯一ID',
`user_id` bigint(20) NOT NULL COMMENT '用户ID',
`goods_id` bigint(20) NOT NULL COMMENT '商品ID',
`store_id` bigint(20) NOT NULL COMMENT '店铺ID',
`added_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '收藏时间',
`goods_name` varchar(255) NOT NULL COMMENT '商品名称',
`goods_main_image_url` varchar(255) NOT NULL COMMENT '商品主图',
`goods_price` bigint(20) NOT NULL COMMENT '商品价格',
`goods_sell_point` varchar(512) NOT NULL COMMENT '商品描述',
PRIMARY KEY (`id`),
UNIQUE KEY `uniq_user_goods` (`user_id`,`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='商品收藏表';

CREATE TABLE `goods` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`name` varchar(255) DEFAULT NULL COMMENT '商品名称',
`category_id` bigint(20) DEFAULT NULL COMMENT '分类id',
`category_id_path` varchar(255) DEFAULT NULL COMMENT '分类id路径, 例如: 1/5/7',
`unit_id` bigint(20) NOT NULL COMMENT '计量单位id',
`unit_name` varchar(255) DEFAULT NULL COMMENT '计量单位名称',
`sell_point` varchar(255) DEFAULT NULL COMMENT '商品卖点',
`description_images` varchar(255) DEFAULT NULL COMMENT '商品描述图',
`display_images` varchar(255) NOT NULL COMMENT '商品展示图(第一张为主图)',
`store_id` bigint(20) DEFAULT NULL COMMENT '店铺id',
`store_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
`sales` int(11) DEFAULT 0 COMMENT '总销量',
`max_price` bigint(20) DEFAULT NULL COMMENT '最高价格(单位:分)',
`min_price` bigint(20) DEFAULT NULL COMMENT '最低价格(单位:分)',
`status` tinyint(4) DEFAULT NULL COMMENT '商品上架状态',
`audit_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '商品审核状态',
`create_time` datetime DEFAULT current_timestamp() COMMENT '创建日期',
`create_user` varchar(255) DEFAULT NULL COMMENT '创建用户',
`update_time` datetime DEFAULT current_timestamp() COMMENT '更新时间',
`update_user` varchar(255) DEFAULT NULL COMMENT '更新用户',
`is_del` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品管理';

CREATE TABLE `goods_comment` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`order_item_id` bigint(20) NOT NULL COMMENT '订单明细ID（唯一，一次购买一次评价）',
`order_id` bigint(20) NOT NULL COMMENT '订单ID（冗余，便于查询）',
`goods_id` bigint(20) NOT NULL COMMENT '商品ID',
`sku_spec_snapshot` varchar(255) DEFAULT NULL COMMENT 'SKU规格快照，如：颜色:红 / 内存:512G',
`user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
`user_nickname` varchar(100) DEFAULT NULL,
`user_avatar` varchar(500) DEFAULT NULL,
`rating` tinyint(4) NOT NULL COMMENT '评分：1~5',
`content` varchar(500) NOT NULL COMMENT '评论内容',
`reply` varchar(500) DEFAULT NULL COMMENT '商家回复',
`image_urls` varchar(1000) DEFAULT NULL COMMENT '评论图片，逗号分隔URL',
`is_anonymous` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
`create_time` datetime(3) DEFAULT current_timestamp(3) COMMENT '创建时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_order_item` (`order_item_id`),
KEY `idx_goods_id` (`goods_id`),
KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评论表';

CREATE TABLE `goods_sku` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'sku id',
`goods_id` bigint(20) NOT NULL COMMENT '所属商品(spu)',
`price` bigint(20) NOT NULL COMMENT '售价(分)',
`inventory` bigint(20) NOT NULL COMMENT '库存',
`sales` bigint(20) NOT NULL DEFAULT 0 COMMENT '销量',
`status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态, 1-上架, 0-下架',
`create_time` datetime NOT NULL DEFAULT current_timestamp(),
`update_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
PRIMARY KEY (`id`),
KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=243 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

CREATE TABLE `goods_sku_spec` (
`sku_id` bigint(20) NOT NULL COMMENT 'sku id',
`spec_id` bigint(20) NOT NULL COMMENT '规格id',
`spec_value_id` bigint(20) NOT NULL COMMENT '规格值id',
PRIMARY KEY (`sku_id`,`spec_id`),
KEY `idx_sku_id` (`sku_id`),
KEY `idx_spec_value_id` (`spec_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU规格关联表';

CREATE TABLE `goods_unit` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '单位id',
`name` varchar(20) NOT NULL COMMENT '单位名称，如 件/个/瓶',
`status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
`sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
`create_time` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_unit_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品单位表';

CREATE TABLE `message_log` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`biz_id` varchar(64) NOT NULL COMMENT '业务唯一ID',
`biz_type` varchar(50) NOT NULL COMMENT '业务场景',
`topic` varchar(128) NOT NULL COMMENT '消息主题',
`payload` text NOT NULL COMMENT '消息内容',
`status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0-待处理 1-成功 2-失败 3-已结束',
`retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
`next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
`error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
`create_time` datetime NOT NULL DEFAULT current_timestamp(),
`update_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
PRIMARY KEY (`id`),
UNIQUE KEY `uk_biz` (`biz_id`,`biz_type`),
KEY `idx_status_retry` (`status`,`next_retry_time`),
KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='消息日志表';

CREATE TABLE `notice` (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
`content` varchar(255) DEFAULT NULL COMMENT '内容',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告管理';

CREATE TABLE `order_address` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单地址快照ID',
`order_id` bigint(20) NOT NULL COMMENT '订单ID',
`receiver` varchar(100) NOT NULL COMMENT '收货人',
`region_code` int(11) DEFAULT NULL COMMENT '地区代码',
`detail` varchar(255) NOT NULL COMMENT '详细地址',
`zip_code` varchar(6) NOT NULL COMMENT '邮编',
`phone` varchar(11) NOT NULL COMMENT '联系电话',
`created_at` timestamp NULL DEFAULT current_timestamp() COMMENT '快照创建时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单地址快照';

CREATE TABLE `order_item` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`order_id` bigint(20) NOT NULL COMMENT '订单ID',
`goods_id` bigint(20) NOT NULL COMMENT '商品ID',
`sku_specs` varchar(255) DEFAULT NULL COMMENT 'SKU规格快照，如：颜色=黑色;尺码=L',
`goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
`goods_main_image_url` varchar(255) DEFAULT NULL COMMENT '商品主图url',
`goods_price` bigint(20) DEFAULT NULL COMMENT '下单时商品单价（分）',
`quantity` int(11) DEFAULT NULL COMMENT '购买数量',
`total_price` bigint(20) DEFAULT NULL COMMENT '明细小计（分）',
`comment_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '评论状态：0-未评价 1-已评价',
`create_time` datetime(3) DEFAULT current_timestamp(3) COMMENT '创建时间',
`sku_id` bigint(20) NOT NULL COMMENT 'SKU ID（真实下单对象）',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

CREATE TABLE `orders` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`parent_id` bigint(20) DEFAULT NULL COMMENT '父订单id',
`no` varchar(255) DEFAULT NULL COMMENT '订单号',
`user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
`store_id` bigint(20) DEFAULT NULL COMMENT '商家id',
`quantity` int(11) DEFAULT NULL COMMENT '商品数量',
`total_price` bigint(20) DEFAULT NULL COMMENT '订单总价',
`user_name` varchar(255) DEFAULT NULL COMMENT '下单用户名',
`address` varchar(255) DEFAULT NULL COMMENT '下单地址',
`phone` varchar(255) DEFAULT NULL COMMENT '下单电话',
`status` varchar(255) DEFAULT NULL COMMENT '状态',
`order_type` varchar(20) DEFAULT 'NORMAL' COMMENT '订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单（单店铺）',
`reason` varchar(255) DEFAULT NULL COMMENT '订单取消或关闭原因（用于CANCELED/CLOSED状态）',
`create_time` datetime(3) DEFAULT current_timestamp(3) COMMENT '下单时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单管理';

CREATE TABLE `resources` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
`description` varchar(100) DEFAULT NULL COMMENT '资源描述',
`type` varchar(55) NOT NULL COMMENT '资源类型: menu, catalog, button',
`code` varchar(255) DEFAULT NULL COMMENT '资源代码(如: order:add, order:edit)',
`meta` longtext CHARACTER SET utf8mb4 COLLATE=utf8mb4_bin DEFAULT NULL COMMENT '前端元信息（如路由path、组件component、icon、keep_alive等）',
`sort` int(11) DEFAULT NULL COMMENT '排序值',
`parent_id` bigint(20) DEFAULT 0 COMMENT '父资源ID，顶级为0',
`enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
`create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='系统资源表';

CREATE TABLE `role_resources` (
`role_id` bigint(20) NOT NULL COMMENT '角色ID',
`resource_id` bigint(20) NOT NULL COMMENT '资源ID',
PRIMARY KEY (`role_id`,`resource_id`),
KEY `resource_id` (`resource_id`),
CONSTRAINT `role_resources_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
CONSTRAINT `role_resources_ibfk_2` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='角色与资源关联表';

CREATE TABLE `roles` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
`name` varchar(50) NOT NULL COMMENT '角色名称',
`built_in` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为内置角色(不可删除)',
`enable` tinyint(1) DEFAULT 1 COMMENT '是否启用',
`description` varchar(255) DEFAULT NULL COMMENT '角色描述',
`create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',
PRIMARY KEY (`id`),
UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='系统角色表';

CREATE TABLE `spec` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规格id',
`name` varchar(50) NOT NULL COMMENT '规格名，如 颜色、尺码',
`sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
`status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
`create_time` datetime NOT NULL DEFAULT current_timestamp(),
`update_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
PRIMARY KEY (`id`),
UNIQUE KEY `uk_spec_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规格名表';

CREATE TABLE `spec_value` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规格值id',
`spec_id` bigint(20) NOT NULL COMMENT '所属规格',
`value` varchar(50) NOT NULL COMMENT '规格值，如 红、XL',
`sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
`status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态',
`create_time` datetime NOT NULL DEFAULT current_timestamp(),
`update_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
PRIMARY KEY (`id`),
UNIQUE KEY `uk_spec_value` (`spec_id`,`value`),
KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规格值表';

CREATE TABLE `store` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
`user_id` bigint(20) NOT NULL COMMENT '店主用户ID',
`info` varchar(255) DEFAULT NULL COMMENT '店铺简介',
`avatar_url` varchar(255) DEFAULT NULL COMMENT '店铺头像url',
`name` varchar(100) NOT NULL COMMENT '店铺名称',
`banner` varchar(255) DEFAULT NULL COMMENT '店铺顶部横幅背景图 URL',
`updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
`created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
PRIMARY KEY (`id`),
KEY `fk_shop_owner` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='店铺表';

CREATE TABLE `store_category` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`store_id` bigint(20) NOT NULL COMMENT '店铺ID',
`category_id` int(10) unsigned NOT NULL COMMENT '类目ID',
`created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_store_category` (`store_id`,`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺与经营类目关联表';

CREATE TABLE `user` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
`username` varchar(50) NOT NULL COMMENT '用户名',
`password` varchar(255) NOT NULL COMMENT '用户密码',
`nickname` varchar(100) DEFAULT NULL COMMENT '用户昵称',
`phone` varchar(30) DEFAULT NULL COMMENT '手机号',
`email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
`bio` varchar(255) DEFAULT NULL COMMENT '个人简介',
`avatar_url` varchar(500) DEFAULT NULL COMMENT '用户头像URL',
`role` enum('NORMAL','ADMIN','MERCHANT') NOT NULL DEFAULT 'NORMAL' COMMENT '用户类型',
PRIMARY KEY (`id`),
UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='用户表';

CREATE TABLE `user_bank_account` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '银行卡ID',
`user_id` bigint(20) NOT NULL COMMENT '用户ID',
`qualification_id` bigint(20) DEFAULT NULL COMMENT '关联资质认证ID（如有需要可关联）',
`account_name` varchar(50) NOT NULL COMMENT '开户人姓名',
`card_number` varchar(30) NOT NULL COMMENT '银行卡号',
`bank_name` varchar(100) NOT NULL COMMENT '开户银行',
`branch_name` varchar(100) DEFAULT NULL COMMENT '开户支行',
`mobile` varchar(20) NOT NULL COMMENT '银行预留手机号',
`status` char(20) NOT NULL DEFAULT '待认证' COMMENT '状态: 待认证, 已认证, 拒绝, 已解绑',
`created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '添加时间',
`updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户银行卡信息表';

CREATE TABLE `user_qualification` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资质认证ID',
`user_id` bigint(20) NOT NULL COMMENT '用户ID',
`subject_type` varchar(20) NOT NULL COMMENT '主体类型: personal/individual/enterprise',
`real_name` varchar(50) NOT NULL COMMENT '真实姓名',
`id_card` varchar(30) NOT NULL COMMENT '身份证号',
`id_card_valid_start` date NOT NULL COMMENT '身份证有效期起',
`id_card_valid_end` date NOT NULL COMMENT '身份证有效期止',
`id_card_front` varchar(255) NOT NULL COMMENT '身份证正面照片URL',
`id_card_back` varchar(255) NOT NULL COMMENT '身份证反面照片URL',
`license_number` varchar(50) DEFAULT NULL COMMENT '营业执照编号',
`license_name` varchar(100) DEFAULT NULL COMMENT '营业执照名称',
`establishment_date` date DEFAULT NULL COMMENT '营业执照成立日期',
`registered_address` varchar(255) DEFAULT NULL COMMENT '注册地址',
`license_photo` varchar(255) DEFAULT NULL COMMENT '营业执照照片URL',
`categories` varchar(255) DEFAULT NULL COMMENT '可经营类目（逗号分隔或JSON）',
`created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
`updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资质认证信息表';

CREATE TABLE `user_roles` (
`user_id` bigint(20) NOT NULL COMMENT '用户ID',
`role_id` bigint(20) NOT NULL COMMENT '角色ID',
`create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',
PRIMARY KEY (`user_id`,`role_id`),
KEY `role_id` (`role_id`),
CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='用户与角色关联表';
