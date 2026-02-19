/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.0.2-MariaDB, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: online_shop
-- ------------------------------------------------------
-- Server version	12.0.2-MariaDB-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `address` VALUES
(4,2,'小黑',340207,'修士','安徽省/芜湖市/鸠江区','234343','17774703712',0x01,'2026-01-01 14:50:04','2026-01-01 14:50:04');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `audit`
--

DROP TABLE IF EXISTS `audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
  `extra_info` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展信息: 可存储SKU组合/商品规格等JSON' CHECK (json_valid(`extra_info`)),
  `create_time` datetime DEFAULT current_timestamp() COMMENT '申请时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用审核表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit`
--

LOCK TABLES `audit` WRITE;
/*!40000 ALTER TABLE `audit` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `audit` VALUES
(21,'GOODS',19,'REAUDIT',NULL,3,'store',NULL,NULL,'{\"goodsId\":\"19\",\"goodsName\":\"233333\",\"categoryId\":\"15\",\"categoryIdPath\":\"9/13/15\",\"unitId\":\"1\",\"unitName\":\"件\",\"sellPoint\":\"343434\",\"displayImageUrls\":[\"http://localhost:7002/uploads/image/fb196408622d48efbd23f0cc2c876aba.png\",\"http://localhost:7002/uploads/image/78439c9984bb420fb3be25034f88ebab.png\",\"http://localhost:7002/uploads/image/2217e260cc2d47fba1e87d8b30faa9db.jpg\"],\"descriptionImageUrls\":[\"http://localhost:7002/uploads/image/97cfc73ab31643fc9091d17339554d11.jpeg\",\"http://localhost:7002/uploads/image/fce8b4e0b0c1484a80281e8799ed19b1.png\"],\"storeId\":\"1\",\"storeName\":\"store的店铺\",\"status\":true,\"specifications\":[{\"name\":\"尺寸\",\"values\":[\"X\",\"M\",\"L\"]},{\"name\":\"颜色test\",\"values\":[\"红\",\"黄\",\"紫\",\"蓝\"]}],\"skus\":[{\"specs\":[{\"name\":\"尺寸\",\"value\":\"X\"},{\"name\":\"颜色test\",\"value\":\"红\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"M\"},{\"name\":\"颜色test\",\"value\":\"红\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"L\"},{\"name\":\"颜色test\",\"value\":\"红\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"X\"},{\"name\":\"颜色test\",\"value\":\"黄\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"M\"},{\"name\":\"颜色test\",\"value\":\"黄\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"L\"},{\"name\":\"颜色test\",\"value\":\"黄\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"X\"},{\"name\":\"颜色test\",\"value\":\"紫\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"M\"},{\"name\":\"颜色test\",\"value\":\"紫\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"L\"},{\"name\":\"颜色test\",\"value\":\"紫\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"X\"},{\"name\":\"颜色test\",\"value\":\"蓝\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"M\"},{\"name\":\"颜色test\",\"value\":\"蓝\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true},{\"specs\":[{\"name\":\"尺寸\",\"value\":\"L\"},{\"name\":\"颜色test\",\"value\":\"蓝\"}],\"price\":\"2.00\",\"inventory\":4,\"status\":true}]}','2026-02-13 21:52:06',NULL);
/*!40000 ALTER TABLE `audit` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `image_url` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `goods_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID',
  `goods_name` varchar(255) NOT NULL COMMENT '关联商品名称',
  `is_recommend` bit(1) NOT NULL COMMENT '是否在首页推荐',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `banner` VALUES
(3,'http://localhost:7000/uploads/image/ae11f044bde34af681fac460dc0b448c.jpeg',13,'测试商品1',0x01),
(4,'https://img.shetu66.com/2023/12/10/1702188059726159.png',14,'电脑',0x01),
(5,'https://img95.699pic.com/photo/40234/9066.jpg_wh860.jpg',13,'测试商品1',0x01),
(6,'http://localhost:7000/uploads/image/0ba62de206134a85a28390346351a90c.png',13,'测试商品1',0x01);
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `category` VALUES
(1,'电子产品',0,1,1,0x01),
(2,'手机',1,2,1,0x01),
(3,'智能手机',2,3,1,0x01),
(4,'功能机',2,3,2,0x01),
(5,'笔记本电脑',1,2,2,0x01),
(6,'超极本',5,3,1,0x01),
(7,'游戏本',5,3,2,0x01),
(8,'平板电脑',1,2,3,0x01),
(9,'家用电器',0,1,2,0x01),
(10,'冰箱',9,2,1,0x01),
(11,'双开门冰箱',10,3,1,0x01),
(12,'三开门冰箱',10,3,2,0x01),
(13,'洗衣机',9,2,2,0x01),
(14,'滚筒洗衣机',13,3,1,0x01),
(15,'波轮洗衣机',13,3,2,0x01),
(16,'空调',9,2,3,0x01),
(17,'挂式空调',16,3,1,0x01),
(18,'柜式空调',16,3,2,0x01),
(19,'服装',0,1,3,0x01),
(20,'男装',19,2,1,0x01),
(21,'T恤',20,3,1,0x01),
(22,'衬衫',20,3,2,0x01),
(23,'女装',19,2,2,0x01),
(24,'连衣裙',23,3,1,0x01),
(25,'半身裙',23,3,2,0x01),
(26,'童装',19,2,3,0x01),
(27,'玩具',0,1,4,0x01),
(28,'积木',27,2,1,0x01),
(29,'遥控车',27,2,2,0x01),
(30,'拼图',27,2,3,0x01);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `favorite` VALUES
(2,2,14,1,'2026-01-24 22:41:44','电脑','http://localhost:7000/uploads/image/581a23005d774a59ad0d291e7d831d64.png',15229,'http://localhost:7000/uploads/image/92e8ccd576c64acfbef48d33d84d7324.png,http://localhost:7000/uploads/image/1167d1174a8e4637b757ff04e3240f99.jpeg');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods`
--

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `goods` VALUES
(19,'3434',15,'9/13/15',1,'件','343434','http://localhost:7002/uploads/image/97cfc73ab31643fc9091d17339554d11.jpeg,http://localhost:7002/uploads/image/fce8b4e0b0c1484a80281e8799ed19b1.png','http://localhost:7002/uploads/image/fb196408622d48efbd23f0cc2c876aba.png,http://localhost:7002/uploads/image/78439c9984bb420fb3be25034f88ebab.png,http://localhost:7002/uploads/image/2217e260cc2d47fba1e87d8b30faa9db.jpg',1,'store的店铺',0,200,200,1,'REAUDIT','2026-02-13 21:52:06','store','2026-02-14 22:16:10','admin',0x00);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `goods_comment`
--

DROP TABLE IF EXISTS `goods_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_comment`
--

LOCK TABLES `goods_comment` WRITE;
/*!40000 ALTER TABLE `goods_comment` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `goods_comment` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `goods_sku`
--

DROP TABLE IF EXISTS `goods_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_sku`
--

LOCK TABLES `goods_sku` WRITE;
/*!40000 ALTER TABLE `goods_sku` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `goods_sku` VALUES
(93,15,1200,34,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(94,15,1900,54,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(95,15,1000,22,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(96,15,1100,123,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(97,15,1300,2343433,0,1,'2026-01-27 23:36:20','2026-02-10 16:58:55'),
(98,15,1300,45,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(99,15,2000,3,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(100,15,1700,2,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(101,15,1000,5,0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(108,18,23400,334,0,1,'2026-02-13 19:20:49','2026-02-13 19:20:49'),
(109,18,23400,334,0,1,'2026-02-13 19:20:49','2026-02-13 19:20:49'),
(110,18,23400,334,0,1,'2026-02-13 19:20:49','2026-02-13 19:20:49'),
(231,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(232,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(233,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(234,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(235,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(236,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(237,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(238,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(239,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(240,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(241,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10'),
(242,19,200,4,0,1,'2026-02-14 22:16:10','2026-02-14 22:16:10');
/*!40000 ALTER TABLE `goods_sku` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `goods_sku_spec`
--

DROP TABLE IF EXISTS `goods_sku_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_sku_spec` (
  `sku_id` bigint(20) NOT NULL COMMENT 'sku id',
  `spec_id` bigint(20) NOT NULL COMMENT '规格id',
  `spec_value_id` bigint(20) NOT NULL COMMENT '规格值id',
  PRIMARY KEY (`sku_id`,`spec_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_spec_value_id` (`spec_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU规格关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_sku_spec`
--

LOCK TABLES `goods_sku_spec` WRITE;
/*!40000 ALTER TABLE `goods_sku_spec` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `goods_sku_spec` VALUES
(93,3,32),
(94,3,32),
(95,3,32),
(108,3,32),
(111,3,32),
(123,3,32),
(126,3,32),
(129,3,32),
(132,3,32),
(135,3,32),
(138,3,32),
(141,3,32),
(144,3,32),
(147,3,32),
(150,3,32),
(153,3,32),
(156,3,32),
(159,3,32),
(162,3,32),
(168,3,32),
(171,3,32),
(174,3,32),
(175,3,32),
(176,3,32),
(183,3,32),
(184,3,32),
(185,3,32),
(192,3,32),
(207,3,32),
(209,3,32),
(211,3,32),
(213,3,32),
(215,3,32),
(217,3,32),
(219,3,32),
(221,3,32),
(223,3,32),
(225,3,32),
(227,3,32),
(229,3,32),
(96,3,33),
(97,3,33),
(98,3,33),
(109,3,33),
(112,3,33),
(124,3,33),
(127,3,33),
(130,3,33),
(133,3,33),
(136,3,33),
(139,3,33),
(142,3,33),
(145,3,33),
(148,3,33),
(151,3,33),
(154,3,33),
(157,3,33),
(160,3,33),
(163,3,33),
(169,3,33),
(172,3,33),
(177,3,33),
(178,3,33),
(179,3,33),
(186,3,33),
(187,3,33),
(188,3,33),
(193,3,33),
(208,3,33),
(210,3,33),
(212,3,33),
(214,3,33),
(216,3,33),
(218,3,33),
(220,3,33),
(222,3,33),
(224,3,33),
(226,3,33),
(228,3,33),
(230,3,33),
(99,3,34),
(100,3,34),
(101,3,34),
(110,3,34),
(113,3,34),
(125,3,34),
(128,3,34),
(131,3,34),
(134,3,34),
(137,3,34),
(140,3,34),
(143,3,34),
(146,3,34),
(149,3,34),
(152,3,34),
(155,3,34),
(158,3,34),
(161,3,34),
(164,3,34),
(170,3,34),
(173,3,34),
(180,3,34),
(181,3,34),
(182,3,34),
(189,3,34),
(190,3,34),
(191,3,34),
(194,3,34),
(95,4,35),
(98,4,35),
(101,4,35),
(176,4,35),
(179,4,35),
(182,4,35),
(185,4,35),
(188,4,35),
(191,4,35),
(196,4,35),
(199,4,35),
(202,4,35),
(205,4,35),
(209,4,35),
(210,4,35),
(215,4,35),
(216,4,35),
(221,4,35),
(222,4,35),
(227,4,35),
(228,4,35),
(232,4,35),
(235,4,35),
(238,4,35),
(241,4,35),
(94,4,36),
(97,4,36),
(100,4,36),
(175,4,36),
(178,4,36),
(181,4,36),
(184,4,36),
(187,4,36),
(190,4,36),
(197,4,36),
(200,4,36),
(203,4,36),
(206,4,36),
(211,4,36),
(212,4,36),
(217,4,36),
(218,4,36),
(223,4,36),
(224,4,36),
(229,4,36),
(230,4,36),
(233,4,36),
(236,4,36),
(239,4,36),
(242,4,36),
(93,4,41),
(96,4,41),
(99,4,41),
(195,4,41),
(198,4,41),
(201,4,41),
(204,4,41),
(207,4,41),
(208,4,41),
(213,4,41),
(214,4,41),
(219,4,41),
(220,4,41),
(225,4,41),
(226,4,41),
(231,4,41),
(234,4,41),
(237,4,41),
(240,4,41),
(174,4,42),
(177,4,42),
(180,4,42),
(183,4,42),
(186,4,42),
(189,4,42),
(195,10,43),
(196,10,43),
(197,10,43),
(231,10,43),
(232,10,43),
(233,10,43),
(198,10,44),
(199,10,44),
(200,10,44),
(234,10,44),
(235,10,44),
(236,10,44),
(201,10,45),
(202,10,45),
(203,10,45),
(240,10,45),
(241,10,45),
(242,10,45),
(204,10,46),
(205,10,46),
(206,10,46),
(237,10,46),
(238,10,46),
(239,10,46);
/*!40000 ALTER TABLE `goods_sku_spec` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `goods_unit`
--

DROP TABLE IF EXISTS `goods_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_unit`
--

LOCK TABLES `goods_unit` WRITE;
/*!40000 ALTER TABLE `goods_unit` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `goods_unit` VALUES
(1,'件',1,0,'2026-01-05 23:25:13','2026-01-05 23:45:42'),
(2,'箱',1,0,'2026-01-05 23:36:16','2026-01-05 23:36:16');
/*!40000 ALTER TABLE `goods_unit` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `message_log`
--

DROP TABLE IF EXISTS `message_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_log`
--

LOCK TABLES `message_log` WRITE;
/*!40000 ALTER TABLE `message_log` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `message_log` VALUES
(1,'20260126005854c4f94654','ORDER_CANCEL','order.cancel.topic','{\"id\":121,\"no\":\"20260126005854c4f94654\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":15234,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769362134119}',0,0,NULL,NULL,'2026-01-26 00:58:54','2026-01-26 00:58:54'),
(2,'202601260103547a7bdfec','ORDER_CANCEL','order_cancel_topic','{\"id\":123,\"no\":\"202601260103547a7bdfec\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":15234,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769360634126,\"expireTime\":1769362434126}',0,0,NULL,NULL,'2026-01-26 01:03:54','2026-01-26 01:03:54'),
(3,'202601260105463d022579','ORDER_CANCEL','order_cancel_topic','{\"id\":124,\"no\":\"202601260105463d022579\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23430,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769360746807,\"expireTime\":1769362546807}',0,0,NULL,NULL,'2026-01-26 01:05:46','2026-01-26 01:05:46'),
(4,'20260126010554aa1bd3e2','ORDER_CANCEL','order_cancel_topic','{\"id\":125,\"no\":\"20260126010554aa1bd3e2\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23430,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769360754343,\"expireTime\":1769362554343}',0,0,NULL,NULL,'2026-01-26 01:05:54','2026-01-26 01:05:54'),
(5,'202601260111409365340e','ORDER_CANCEL','order_cancel_topic','{\"id\":126,\"no\":\"202601260111409365340e\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23430,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769361100901,\"expireTime\":1769362900901}',0,0,NULL,NULL,'2026-01-26 01:11:40','2026-01-26 01:11:40'),
(6,'20260126144007fdeebf5f','ORDER_CANCEL','order_cancel_topic','{\"id\":127,\"no\":\"20260126144007fdeebf5f\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23430,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769409607496,\"expireTime\":1769411407496}',0,0,NULL,NULL,'2026-01-26 14:40:07','2026-01-26 14:40:07'),
(7,'2026012614483995188554','ORDER_CANCEL','order_cancel_topic','{\"id\":128,\"no\":\"2026012614483995188554\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23434,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769410119503,\"expireTime\":1769411919503}',0,0,NULL,NULL,'2026-01-26 14:48:39','2026-01-26 14:48:39'),
(8,'2026012617115859abb8e4','ORDER_TIMEOUT_CLOSE','order_cancel_topic','{\"id\":129,\"no\":\"2026012617115859abb8e4\",\"userId\":2,\"storeId\":1,\"quantity\":1,\"totalPrice\":23430,\"userName\":\"小黑\",\"address\":\"安徽省/芜湖市/鸠江区/修士\",\"phone\":\"17774703712\",\"status\":\"CREATED\",\"orderType\":\"NORMAL\",\"createTime\":1769418718367,\"expireTime\":1769420518367}',0,0,NULL,NULL,'2026-01-26 17:11:58','2026-01-26 17:11:58');
/*!40000 ALTER TABLE `message_log` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `content` varchar(255) DEFAULT NULL COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `notice` VALUES
(1,'为了提供更好的服务，我们将于每日凌晨2点至5点进行系统维护。在此期间，商城将暂时无法访问，敬请谅解！'),
(2,'测试第二条公告');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `order_address`
--

DROP TABLE IF EXISTS `order_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_address`
--

LOCK TABLES `order_address` WRITE;
/*!40000 ALTER TABLE `order_address` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `order_address` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `order_item` VALUES
(136,195,14,'颜色=红;尺寸=S;内存=128G','电脑','http://localhost:7000/uploads/image/581a23005d774a59ad0d291e7d831d64.png',15234,1,15234,0,'2026-01-28 17:42:09.985',57),
(137,195,13,'颜色=红','测试商品1','http://localhost:7000/uploads/image/38e010cd02ba4fda93b2d9c6dd6d1e14.png',23430,1,23430,0,'2026-01-28 17:42:09.985',54),
(138,196,15,'颜色=红;尺寸=X','xiao的商品','http://localhost:7000/uploads/image/7e4c338cb9e04f11a0505d97936c4f88.png',1200,1,1200,0,'2026-01-28 17:42:09.985',93),
(139,198,14,'颜色=红;尺寸=S;内存=128G','电脑','http://localhost:7000/uploads/image/581a23005d774a59ad0d291e7d831d64.png',15234,1,15234,0,'2026-01-28 17:42:14.216',57);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `orders` VALUES
(194,NULL,'P20260128174209c08daed0',2,NULL,2,39864,'小黑','安徽省/芜湖市/鸠江区/修士','17774703712','CREATED','PARENT',NULL,'2026-01-28 17:42:09.971'),
(195,194,'202601281742095242da22',2,1,2,38664,'小黑','安徽省/芜湖市/鸠江区/修士','17774703712','CREATED','SUB',NULL,'2026-01-28 17:42:09.962'),
(196,194,'20260128174209577507c4',2,11,1,1200,'小黑','安徽省/芜湖市/鸠江区/修士','17774703712','CREATED','SUB',NULL,'2026-01-28 17:42:09.965'),
(198,NULL,'20260128174214a382590e',2,1,1,15234,'小黑','安徽省/芜湖市/鸠江区/修士','17774703712','CANCELED','NORMAL','用户主动取消','2026-01-28 17:42:14.213');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `description` varchar(100) DEFAULT NULL COMMENT '资源描述',
  `type` varchar(55) NOT NULL COMMENT '资源类型: menu, catalog, button',
  `code` varchar(255) DEFAULT NULL COMMENT '资源代码(如: order:add, order:edit)',
  `meta` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '前端元信息（如路由path、组件component、icon、keep_alive等）',
  `sort` int(11) DEFAULT NULL COMMENT '排序值',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父资源ID，顶级为0',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='系统资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resources`
--

LOCK TABLES `resources` WRITE;
/*!40000 ALTER TABLE `resources` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `resources` VALUES
(1,'系统管理','catalog',NULL,'{\"icon\":\"setting\",\"label\":\"系统管理\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":\"system\"}',999,0,1,'2026-02-06 00:22:56'),
(2,'地址管理','menu','address:view','{\"icon\":\"location\",\"label\":\"地址管理\",\"name\":null,\"redirect\":null,\"component\":\"address\",\"path\":\"address\"}',9,0,1,'2026-02-06 00:22:56'),
(3,'新增地址','button','address:add','{\"icon\":null,\"label\":\"添加地址\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,2,1,'2026-02-06 00:22:56'),
(4,'编辑地址','button','address:edit','{\"icon\":null,\"label\":\"编辑地址\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,2,1,'2026-02-06 00:22:56'),
(5,'删除地址','button','address:delete','{\"icon\":null,\"label\":\"删除地址\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,2,1,'2026-02-06 00:22:56'),
(7,'审核操作','button','audit:edit','{\"label\":\"删除角色\"}',NULL,6,1,'2026-02-06 00:22:56'),
(8,'轮播图管理','menu','banner:view','{\"icon\":\"picture\",\"label\":\"轮播图管理\",\"name\":null,\"redirect\":null,\"component\":\"banner\",\"path\":\"banner\"}',8,0,1,'2026-02-06 00:22:56'),
(9,'新增轮播图','button','banner:add','{\"icon\":null,\"label\":\"添加轮播图\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,8,1,'2026-02-06 00:22:56'),
(10,'编辑轮播图','button','banner:edit','{\"icon\":null,\"label\":\"编辑轮播图\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,8,1,'2026-02-06 00:22:56'),
(11,'删除轮播图','button','banner:delete','{\"icon\":null,\"label\":\"删除轮播图\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,8,1,'2026-02-06 00:22:56'),
(12,'分类管理','menu','category:view','{\"icon\":\"grid\",\"label\":\"分类管理\",\"name\":null,\"redirect\":null,\"component\":\"category\",\"path\":\"category\"}',890,0,1,'2026-02-06 00:22:56'),
(13,'新增分类','button','category:add','{\"icon\":null,\"label\":\"添加分类\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,12,1,'2026-02-06 00:22:56'),
(14,'编辑分类','button','category:edit','{\"icon\":null,\"label\":\"编辑分类\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,12,1,'2026-02-06 00:22:56'),
(15,'删除分类','button','category:delete','{\"icon\":null,\"label\":\"删除分类\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,12,1,'2026-02-06 00:22:56'),
(17,'回复评论','button','comment:edit','{\"label\":\"删除角色\"}',NULL,16,1,'2026-02-06 00:22:56'),
(18,'商品管理','catalog','goods:view','{\"icon\":\"goods\",\"label\":\"商品管理\",\"name\":null,\"redirect\":null,\"component\":\"goods\",\"path\":\"goods\"}',8,0,1,'2026-02-06 00:22:56'),
(22,'公告管理','menu','notice:view','{\"icon\":\"bell\",\"label\":\"公告管理\",\"name\":null,\"redirect\":null,\"component\":\"notice\",\"path\":\"notice\"}',89,0,1,'2026-02-06 00:22:56'),
(23,'新增公告','button','notice:add','{\"icon\":null,\"label\":\"添加通知\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,22,1,'2026-02-06 00:22:56'),
(24,'编辑公告','button','notice:edit','{\"icon\":null,\"label\":\"编辑通知\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,22,1,'2026-02-06 00:22:56'),
(25,'删除公告','button','notice:delete','{\"icon\":null,\"label\":\"删除通知\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,22,1,'2026-02-06 00:22:56'),
(26,'订单管理','menu','order:view','{\"icon\":\"shopping-cart-full\",\"label\":\"订单管理\",\"name\":null,\"redirect\":null,\"component\":\"order\",\"path\":\"order\"}',89,0,1,'2026-02-06 00:22:56'),
(28,'统计管理','menu','statistic:view','{\"icon\":\"data-analysis\",\"label\":\"数据统计\",\"name\":null,\"redirect\":null,\"component\":\"statistic\",\"path\":\"statistic\"}',0,0,1,'2026-02-06 00:22:56'),
(30,'编辑店铺','button','store:edit','{\"label\":\"删除角色\"}',NULL,29,1,'2026-02-06 00:22:56'),
(32,'新增单位','button','unit:add','{\"icon\":null,\"label\":\"添加单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,31,1,'2026-02-06 00:22:56'),
(33,'编辑单位','button','unit:edit','{\"icon\":null,\"label\":\"编辑单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,31,1,'2026-02-06 00:22:56'),
(34,'删除单位','button','unit:delete','{\"icon\":null,\"label\":\"删除单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,31,1,'2026-02-06 00:22:56'),
(35,'用户管理','menu','user:view','{\"path\":\"user\",\"component\":\"system/user\",\"icon\":\"el-icon-user\",\"label\":\"用户管理\",\"name\":\"user\"}',NULL,1,1,'2026-02-06 00:22:56'),
(36,'新增用户','button','user:add','{\"icon\":null,\"label\":\"添加用户\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,35,1,'2026-02-06 00:22:56'),
(37,'编辑用户','button','user:edit','{\"icon\":null,\"label\":\"编辑用户\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,35,1,'2026-02-06 00:22:56'),
(38,'删除用户','button','user:delete','{\"icon\":null,\"label\":\"删除用户\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,35,1,'2026-02-06 00:22:56'),
(39,'角色管理','menu','role:view','{\"path\":\"role\",\"component\":\"system/role\",\"icon\":\"el-icon-user-group\",\"label\":\"角色管理\",\"keepAlive\":false,\"name\":\"role\"}',NULL,1,1,'2026-02-06 00:22:56'),
(40,'新增角色','button','role:add','{\"label\":\"新增角色\"}',NULL,39,1,'2026-02-06 00:22:56'),
(41,'编辑角色','button','role:edit','{\"label\":\"编辑角色\"}',NULL,39,1,'2026-02-06 00:22:56'),
(42,'删除角色','button','role:delete','{\"label\":\"删除角色\"}',NULL,39,1,'2026-02-06 00:22:56'),
(43,'菜单管理','menu','menu:view','{\"icon\":\"el-icon-menu\",\"label\":\"资源管理\",\"name\":null,\"redirect\":null,\"component\":\"/system/menu\",\"path\":\"menu\"}',0,1,1,'2026-02-06 00:22:56'),
(44,'新增菜单','button','menu:add','{\"icon\":null,\"label\":\"添加资源\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,43,1,'2026-02-06 00:22:56'),
(45,'编辑菜单','button','menu:edit','{\"icon\":null,\"label\":\"编辑资源\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,43,1,'2026-02-06 00:22:56'),
(46,'删除菜单','button','menu:delete','{\"icon\":null,\"label\":\"删除资源\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,43,1,'2026-02-06 00:22:56'),
(47,NULL,'menu','unit:view','{\"icon\":\"scale-to-original\",\"label\":\"单位管理\",\"name\":null,\"redirect\":null,\"component\":\"goods/unit\",\"path\":\"unit\"}',0,18,1,'2026-02-10 23:13:18'),
(50,NULL,'menu','good:list','{\"icon\":\"list\",\"label\":\"商品列表\",\"name\":null,\"redirect\":null,\"component\":\"goods/list\",\"path\":\"list\"}',0,18,1,'2026-02-10 23:21:52'),
(51,NULL,'button','unit:add','{\"icon\":null,\"label\":\"添加单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,47,1,'2026-02-11 00:11:37'),
(52,NULL,'button','unit:delete','{\"icon\":null,\"label\":\"删除单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,47,1,'2026-02-11 00:11:53'),
(53,NULL,'button','unit:edit','{\"icon\":null,\"label\":\"编辑单位\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,47,1,'2026-02-11 00:12:13'),
(54,NULL,'menu','audit:view','{\"icon\":\"check\",\"label\":\"审核管理\",\"name\":null,\"redirect\":null,\"component\":\"audit\",\"path\":\"audit\"}',0,0,1,'2026-02-14 16:03:56'),
(55,NULL,'button','audit:edit','{\"icon\":null,\"label\":\"审核操作\",\"name\":null,\"redirect\":null,\"component\":null,\"path\":null}',0,54,1,'2026-02-14 16:54:00');
/*!40000 ALTER TABLE `resources` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `role_resources`
--

DROP TABLE IF EXISTS `role_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_resources` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  PRIMARY KEY (`role_id`,`resource_id`),
  KEY `resource_id` (`resource_id`),
  CONSTRAINT `role_resources_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_resources_ibfk_2` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='角色与资源关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_resources`
--

LOCK TABLES `role_resources` WRITE;
/*!40000 ALTER TABLE `role_resources` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `role_resources` VALUES
(1,1),
(1,2),
(1,3),
(1,4),
(1,5),
(1,8),
(1,9),
(1,10),
(1,11),
(1,12),
(1,13),
(1,14),
(1,15),
(1,18),
(1,22),
(1,23),
(1,24),
(1,25),
(1,26),
(1,28),
(1,35),
(1,36),
(1,37),
(1,38),
(1,39),
(1,40),
(1,41),
(1,42),
(1,43),
(1,44),
(1,45),
(1,46),
(1,47),
(1,50),
(1,51),
(1,52),
(1,53),
(1,54),
(1,55);
/*!40000 ALTER TABLE `role_resources` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `roles` VALUES
(1,'admin',1,1,'系统管理员','2026-02-03 18:37:50');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `spec`
--

DROP TABLE IF EXISTS `spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spec`
--

LOCK TABLES `spec` WRITE;
/*!40000 ALTER TABLE `spec` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `spec` VALUES
(3,'颜色',0,1,'2026-01-07 23:33:20','2026-01-07 23:33:20'),
(4,'尺寸',0,1,'2026-01-07 23:33:20','2026-01-07 23:33:20'),
(5,'24324',0,1,'2026-01-12 18:47:46','2026-01-12 18:47:46'),
(6,'34',0,1,'2026-01-12 18:51:36','2026-01-12 18:51:36'),
(7,'234',0,1,'2026-01-12 19:03:11','2026-01-12 19:03:11'),
(8,'3434',0,1,'2026-01-13 01:11:43','2026-01-13 01:11:43'),
(9,'内存',0,1,'2026-01-23 01:44:14','2026-01-23 01:44:14'),
(10,'颜色test',0,1,'2026-02-14 14:51:53','2026-02-14 14:51:53');
/*!40000 ALTER TABLE `spec` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `spec_value`
--

DROP TABLE IF EXISTS `spec_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spec_value`
--

LOCK TABLES `spec_value` WRITE;
/*!40000 ALTER TABLE `spec_value` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `spec_value` VALUES
(15,4,'Q',0,1,'2026-01-11 21:25:36','2026-01-11 21:25:36'),
(21,5,'234',0,1,'2026-01-12 18:47:46','2026-01-12 18:47:46'),
(22,5,'3434',0,1,'2026-01-12 18:47:46','2026-01-12 18:47:46'),
(23,6,'3434',0,1,'2026-01-12 18:51:36','2026-01-12 18:51:36'),
(24,7,'34',0,1,'2026-01-12 19:03:11','2026-01-12 19:03:11'),
(32,3,'红',0,1,'2026-01-20 18:55:52','2026-01-20 18:55:52'),
(33,3,'黄',0,1,'2026-01-20 18:55:52','2026-01-20 18:55:52'),
(34,3,'蓝',0,1,'2026-01-20 18:55:52','2026-01-20 18:55:52'),
(35,4,'M',0,1,'2026-01-23 01:44:14','2026-01-23 01:44:14'),
(36,4,'L',0,1,'2026-01-23 01:44:14','2026-01-23 01:44:14'),
(41,4,'X',0,1,'2026-01-27 23:36:20','2026-01-27 23:36:20'),
(42,4,'S',0,1,'2026-02-14 14:07:16','2026-02-14 14:07:16'),
(43,10,'红',0,1,'2026-02-14 14:51:53','2026-02-14 14:51:53'),
(44,10,'黄',0,1,'2026-02-14 14:51:53','2026-02-14 14:51:53'),
(45,10,'蓝',0,1,'2026-02-14 14:51:53','2026-02-14 14:51:53'),
(46,10,'紫',0,1,'2026-02-14 14:51:53','2026-02-14 14:51:53');
/*!40000 ALTER TABLE `spec_value` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store`
--

LOCK TABLES `store` WRITE;
/*!40000 ALTER TABLE `store` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `store` VALUES
(1,3,'商品简介','/uploads/image/2fcb657c1e0e4b55a7b2fda2d2000a58.jpg','store的店铺','/uploads/image/26544a583ed14884bbf808c3df5b72ec.png','2025-12-25 17:02:54','2025-12-25 14:55:32'),
(11,4,NULL,NULL,'xiao的店铺',NULL,'2025-12-25 17:48:05','2025-12-25 17:48:05');
/*!40000 ALTER TABLE `store` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `store_category`
--

DROP TABLE IF EXISTS `store_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint(20) NOT NULL COMMENT '店铺ID',
  `category_id` int(10) unsigned NOT NULL COMMENT '类目ID',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_category` (`store_id`,`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺与经营类目关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_category`
--

LOCK TABLES `store_category` WRITE;
/*!40000 ALTER TABLE `store_category` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `store_category` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `user` VALUES
(1,'admin','$2a$10$Qo3LJYE3z4vx4SSxFQYWk.Bs1D822HCVAkxKUN6myqt7pmnEfc9lS','小黑',NULL,NULL,NULL,NULL,'ADMIN'),
(2,'root','$2a$10$Qo3LJYE3z4vx4SSxFQYWk.Bs1D822HCVAkxKUN6myqt7pmnEfc9lS','小黑','17774703712',NULL,'我的信息...','https://www.keaitupian.cn/cjpic/frombd/0/253/2279408239/3825398873.jpg','NORMAL'),
(3,'store','$2a$10$QLjyUNknVmL/EP7fCFlBrOQZkVeFPz5pQHkiHOtepa.N4l8Q8xf3O','store',NULL,NULL,NULL,NULL,'MERCHANT'),
(4,'xiao','$2a$10$A.yucvA5LHyVGqtNZb8KgeNz2JDv7mRRCDH35FFEOq7FaEEbmFaly','xiao',NULL,NULL,NULL,NULL,'MERCHANT');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user_bank_account`
--

DROP TABLE IF EXISTS `user_bank_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_bank_account`
--

LOCK TABLES `user_bank_account` WRITE;
/*!40000 ALTER TABLE `user_bank_account` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `user_bank_account` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user_qualification`
--

DROP TABLE IF EXISTS `user_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_qualification`
--

LOCK TABLES `user_qualification` WRITE;
/*!40000 ALTER TABLE `user_qualification` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `user_qualification` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='用户与角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `user_roles` VALUES
(1,1,'2026-02-03 18:40:55');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-02-16 15:12:24
