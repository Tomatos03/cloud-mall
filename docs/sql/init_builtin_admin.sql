-- 内置管理员与视图资源初始化脚本（幂等）
-- 目标：
-- 1) 将 src/views 下页面注册为 resources
-- 2) 创建内置 admin 用户与管理员角色
-- 3) 管理员角色自动拥有全部资源

START TRANSACTION;

SET @admin_password_hash = '$2b$10$CKNA16DiYRn2tPShDQ.YUuRzzbKTemZFIRx532r9dEPIvldZGLI/O';
SET NAMES utf8mb4;

-- 0) 必需的 layout 根节点（router 中通过 name=home 判断）
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'layout',
    'layout:home',
    JSON_OBJECT(
        'label', '首页',
        'path', '/',
        'name', 'home',
        'component', 'home',
        'icon', 'House'
    ),
    0,
    0,
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM resources
    WHERE code = 'layout:home'
);

SET @home_id = (SELECT id FROM resources WHERE code = 'layout:home' LIMIT 1);

-- 1) 注册业务视图资源（src/views，静态路由 login/404 用 button 资源登记）
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:statistic',
    JSON_OBJECT(
        'label', '数据看板',
        'path', '/statistic',
        'name', 'statistic',
        'component', 'statistic',
        'icon', 'DataAnalysis'
    ),
    1,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:statistic');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:goods:list',
    JSON_OBJECT(
        'label', '商品列表',
        'path', '/goods/list',
        'name', 'goodsList',
        'component', 'goods/list',
        'icon', 'Goods'
    ),
    10,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:goods:list');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:goods:unit',
    JSON_OBJECT(
        'label', '商品单位',
        'path', '/goods/unit',
        'name', 'goodsUnit',
        'component', 'goods/unit',
        'icon', 'SetUp'
    ),
    11,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:goods:unit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:category',
    JSON_OBJECT(
        'label', '分类管理',
        'path', '/category',
        'name', 'category',
        'component', 'category',
        'icon', 'CollectionTag'
    ),
    12,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:category');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:banner',
    JSON_OBJECT(
        'label', '轮播图管理',
        'path', '/banner',
        'name', 'banner',
        'component', 'banner',
        'icon', 'PictureFilled'
    ),
    20,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:banner');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:seckill',
    JSON_OBJECT(
        'label', '秒杀活动',
        'path', '/seckill',
        'name', 'seckill',
        'component', 'seckill',
        'icon', 'Lightning'
    ),
    21,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:seckill');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:seckill:detail',
    JSON_OBJECT(
        'label', '秒杀详情',
        'path', '/seckill/detail/:id',
        'name', 'seckillDetail',
        'component', 'seckill/detail',
        'hidden', true
    ),
    22,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:seckill:detail');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:notice',
    JSON_OBJECT(
        'label', '公告管理',
        'path', '/notice',
        'name', 'notice',
        'component', 'notice',
        'icon', 'Bell'
    ),
    23,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:notice');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:order',
    JSON_OBJECT(
        'label', '订单管理',
        'path', '/order',
        'name', 'order',
        'component', 'order',
        'icon', 'Tickets'
    ),
    30,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:order');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:address',
    JSON_OBJECT(
        'label', '地址管理',
        'path', '/address',
        'name', 'address',
        'component', 'address',
        'icon', 'Location'
    ),
    31,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:address');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:audit',
    JSON_OBJECT(
        'label', '审核管理',
        'path', '/audit',
        'name', 'audit',
        'component', 'audit',
        'icon', 'DocumentChecked'
    ),
    32,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:audit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'catalog',
    'catalog:system',
    JSON_OBJECT(
        'label', '系统设置',
        'path', '/system',
        'name', 'systemCatalog',
        'icon', 'Setting',
        'redirect', '/system/menu'
    ),
    40,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'catalog:system');

SET @system_catalog_id = (SELECT id FROM resources WHERE code = 'catalog:system' LIMIT 1);

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:system:menu',
    JSON_OBJECT(
        'label', '菜单管理',
        'path', 'menu',
        'name', 'systemMenu',
        'component', 'system/menu',
        'icon', 'Menu'
    ),
    1,
    @system_catalog_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:system:menu');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:system:role',
    JSON_OBJECT(
        'label', '角色管理',
        'path', 'role',
        'name', 'systemRole',
        'component', 'system/role',
        'icon', 'UserFilled'
    ),
    2,
    @system_catalog_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:system:role');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:system:user',
    JSON_OBJECT(
        'label', '用户管理',
        'path', 'user',
        'name', 'systemUser',
        'component', 'system/user',
        'icon', 'User'
    ),
    3,
    @system_catalog_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:system:user');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT
    'menu',
    'view:coupon',
    JSON_OBJECT(
        'label', '优惠券管理',
        'path', '/coupon',
        'name', 'coupon',
        'component', 'coupon',
        'icon', 'Ticket'
    ),
    33,
    @home_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'view:coupon');

-- 1.1) 获取各菜单资源 ID（按钮权限将挂载在对应菜单下）
SET @statistic_id = (SELECT id FROM resources WHERE code = 'view:statistic' LIMIT 1);
SET @goods_list_id = (SELECT id FROM resources WHERE code = 'view:goods:list' LIMIT 1);
SET @goods_unit_id = (SELECT id FROM resources WHERE code = 'view:goods:unit' LIMIT 1);
SET @category_id = (SELECT id FROM resources WHERE code = 'view:category' LIMIT 1);
SET @banner_id = (SELECT id FROM resources WHERE code = 'view:banner' LIMIT 1);
SET @notice_id = (SELECT id FROM resources WHERE code = 'view:notice' LIMIT 1);
SET @order_id = (SELECT id FROM resources WHERE code = 'view:order' LIMIT 1);
SET @address_id = (SELECT id FROM resources WHERE code = 'view:address' LIMIT 1);
SET @audit_id = (SELECT id FROM resources WHERE code = 'view:audit' LIMIT 1);
SET @coupon_id = (SELECT id FROM resources WHERE code = 'view:coupon' LIMIT 1);
SET @system_menu_id = (SELECT id FROM resources WHERE code = 'view:system:menu' LIMIT 1);
SET @system_role_id = (SELECT id FROM resources WHERE code = 'view:system:role' LIMIT 1);
SET @system_user_id = (SELECT id FROM resources WHERE code = 'view:system:user' LIMIT 1);

-- 1.2) 注册按钮权限资源（挂载在对应菜单资源下）
-- 地址管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'address:view', JSON_OBJECT('label', '查看地址'), 1, @address_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'address:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'address:add', JSON_OBJECT('label', '新增地址'), 2, @address_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'address:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'address:edit', JSON_OBJECT('label', '编辑地址'), 3, @address_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'address:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'address:delete', JSON_OBJECT('label', '删除地址'), 4, @address_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'address:delete');

-- 审核管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'audit:view', JSON_OBJECT('label', '查看审核'), 1, @audit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'audit:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'audit:edit', JSON_OBJECT('label', '审核操作'), 2, @audit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'audit:edit');

-- 轮播图管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'banner:view', JSON_OBJECT('label', '查看轮播图'), 1, @banner_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'banner:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'banner:add', JSON_OBJECT('label', '新增轮播图'), 2, @banner_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'banner:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'banner:edit', JSON_OBJECT('label', '编辑轮播图'), 3, @banner_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'banner:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'banner:delete', JSON_OBJECT('label', '删除轮播图'), 4, @banner_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'banner:delete');

-- 分类管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'category:view', JSON_OBJECT('label', '查看分类'), 1, @category_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'category:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'category:add', JSON_OBJECT('label', '新增分类'), 2, @category_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'category:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'category:edit', JSON_OBJECT('label', '编辑分类'), 3, @category_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'category:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'category:delete', JSON_OBJECT('label', '删除分类'), 4, @category_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'category:delete');

-- 优惠券管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'coupon:view', JSON_OBJECT('label', '查看优惠券'), 1, @coupon_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'coupon:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'coupon:add', JSON_OBJECT('label', '新增优惠券'), 2, @coupon_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'coupon:add');

-- 商品管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'goods:view', JSON_OBJECT('label', '查看商品'), 1, @goods_list_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'goods:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'goods:add', JSON_OBJECT('label', '新增商品'), 2, @goods_list_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'goods:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'goods:edit', JSON_OBJECT('label', '编辑商品'), 3, @goods_list_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'goods:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'goods:delete', JSON_OBJECT('label', '删除商品'), 4, @goods_list_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'goods:delete');

-- 公告管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'notice:view', JSON_OBJECT('label', '查看公告'), 1, @notice_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'notice:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'notice:add', JSON_OBJECT('label', '新增公告'), 2, @notice_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'notice:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'notice:edit', JSON_OBJECT('label', '编辑公告'), 3, @notice_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'notice:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'notice:delete', JSON_OBJECT('label', '删除公告'), 4, @notice_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'notice:delete');

-- 订单管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'order:view', JSON_OBJECT('label', '查看订单'), 1, @order_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'order:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'order:add', JSON_OBJECT('label', '新增订单'), 2, @order_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'order:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'order:edit', JSON_OBJECT('label', '编辑订单'), 3, @order_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'order:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'order:delete', JSON_OBJECT('label', '删除订单'), 4, @order_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'order:delete');

-- 商品单位
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'unit:view', JSON_OBJECT('label', '查看单位'), 1, @goods_unit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'unit:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'unit:add', JSON_OBJECT('label', '新增单位'), 2, @goods_unit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'unit:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'unit:edit', JSON_OBJECT('label', '编辑单位'), 3, @goods_unit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'unit:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'unit:delete', JSON_OBJECT('label', '删除单位'), 4, @goods_unit_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'unit:delete');

-- 系统菜单管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'menu:view', JSON_OBJECT('label', '查看菜单'), 1, @system_menu_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'menu:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'menu:add', JSON_OBJECT('label', '新增菜单'), 2, @system_menu_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'menu:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'menu:edit', JSON_OBJECT('label', '编辑菜单'), 3, @system_menu_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'menu:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'menu:delete', JSON_OBJECT('label', '删除菜单'), 4, @system_menu_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'menu:delete');

-- 系统角色管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'role:view', JSON_OBJECT('label', '查看角色'), 1, @system_role_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'role:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'role:add', JSON_OBJECT('label', '新增角色'), 2, @system_role_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'role:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'role:edit', JSON_OBJECT('label', '编辑角色'), 3, @system_role_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'role:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'role:delete', JSON_OBJECT('label', '删除角色'), 4, @system_role_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'role:delete');

-- 系统用户管理
INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'user:view', JSON_OBJECT('label', '查看用户'), 1, @system_user_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'user:view');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'user:add', JSON_OBJECT('label', '新增用户'), 2, @system_user_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'user:add');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'user:edit', JSON_OBJECT('label', '编辑用户'), 3, @system_user_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'user:edit');

INSERT INTO resources (type, code, meta, sort, parent_id, enable)
SELECT 'button', 'user:delete', JSON_OBJECT('label', '删除用户'), 4, @system_user_id, 1
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE code = 'user:delete');

-- 2) 初始化内置管理员角色
INSERT INTO roles (name, built_in, enable, description)
SELECT
    '管理员',
    1,
    1,
    '系统内置管理员角色（默认拥有全部资源）'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = '管理员'
);

-- 3) 初始化 admin 用户（密码：zjlljz，bcrypt）
INSERT INTO `user` (username, password, nickname, types)
SELECT
    'admin',
    @admin_password_hash,
    '内置管理员',
    'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM `user`
    WHERE username = 'admin'
);

-- 4) 绑定 admin -> 管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT
    u.id,
    r.id
FROM `user` u
         JOIN roles r ON r.name = '管理员'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
);

-- 5) 管理员角色绑定全部资源（resources 全表）
INSERT INTO role_resources (role_id, resource_id)
SELECT
    r.id,
    res.id
FROM roles r
         JOIN resources res
WHERE r.name = '管理员'
  AND NOT EXISTS (
    SELECT 1
    FROM role_resources rr
    WHERE rr.role_id = r.id
      AND rr.resource_id = res.id
);

COMMIT;
