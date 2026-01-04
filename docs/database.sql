create table address
(
    id          bigint auto_increment comment '地址ID'
        primary key,
    user_id     bigint                                not null comment '用户ID',
    receiver    varchar(100)                          not null comment '收货人名字',
    region_code int                                   null comment '地区代码',
    detail      varchar(255)                          not null comment '详细地址',
    zip_code    varchar(6)                            not null comment '邮编(6位数字)',
    phone       varchar(11)                           not null comment '联系电话(11位手机号)',
    is_default  bit       default b'0'                not null comment '是否为默认地址',
    created_at  timestamp default current_timestamp() null comment '创建时间',
    updated_at  timestamp default current_timestamp() null on update current_timestamp() comment '更新时间'
)
    comment '地址管理' collate = utf8mb4_uca1400_ai_ci;

create index idx_user_id
    on address (user_id);

create table banner
(
    id           bigint       not null comment '主键ID',
    goods_id     int          null comment '关联商品ID',
    title        varchar(255) null comment '标题',
    info         varchar(255) null comment '说明',
    image_url    varchar(255) null comment '封面图片URL',
    is_recommend bit          not null comment '是否在首页推荐'
)
    comment '轮播图表' collate = utf8mb4_uca1400_ai_ci;

create table category
(
    id        int unsigned auto_increment comment '主键ID'
        primary key,
    name      varchar(100)              not null comment '分类名称',
    parent_id int unsigned              not null comment '父级分类ID',
    level     int unsigned default 1    not null comment '分类层级',
    sort      int                       not null comment '排序值',
    status    bit          default b'1' not null comment '状态（1:启用, 0:禁用）'
)
    comment '商品分类表' collate = utf8mb4_uca1400_ai_ci;

create index idx_parentId
    on category (parent_id);

create table favorite
(
    id          bigint auto_increment comment '收藏记录唯一ID'
        primary key,
    user_id     bigint                               not null comment '用户ID',
    goods_id    bigint                               not null comment '商品ID',
    added_at    datetime default current_timestamp() not null comment '收藏时间',
    goods_title varchar(255)                         null comment '商品标题',
    goods_img   varchar(512)                         null comment '商品图片',
    goods_price bigint                               null comment '商品价格',
    goods_desc  varchar(512)                         null comment '商品描述',
    store_id    bigint                               not null comment '店铺ID',
    constraint uniq_user_goods
        unique (user_id, goods_id)
)
    comment '商品收藏表' collate = utf8mb4_uca1400_ai_ci;

create table goods
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(255)     null comment '商品名称',
    category_id bigint           null comment '分类id',
    info        varchar(255)     null comment '商品简介',
    description longtext         null comment '商品详情',
    img         varchar(255)     null comment '商品图',
    img_list    varchar(255)     null comment '更多商品图',
    inventory   bigint           null comment '库存数量',
    price       bigint           null comment '商品价格, 单位分',
    unit        varchar(255)     null comment '价格计量单位',
    store_id    bigint           null comment '店铺id',
    store_name  varchar(255)     null comment '商品名称',
    date        varchar(255)     null comment '上架日期',
    status      tinyint          null comment '上架状态',
    sales       bigint default 0 null comment '销量'
)
    comment '商品管理';

create table goods_comment
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    order_item_id bigint                                   not null comment '订单明细ID（唯一，一次购买一次评价）',
    order_id      bigint                                   not null comment '订单ID（冗余，便于查询）',
    goods_id      bigint                                   not null comment '商品ID',
    user_id       bigint                                   not null comment '评论用户ID',
    user_nickname varchar(100)                             null,
    user_avatar   varchar(500)                             null,
    rating        tinyint                                  not null comment '评分：1~5',
    content       varchar(500)                             not null comment '评论内容',
    reply         varchar(500)                             null comment '商家回复',
    images        varchar(1000)                            null comment '评论图片，逗号分隔URL',
    is_anonymous  tinyint     default 0                    not null comment '是否匿名：0-否 1-是',
    create_time   datetime(3) default current_timestamp(3) null comment '创建时间',
    constraint uk_order_item
        unique (order_item_id)
)
    comment '商品评论表';

create index idx_goods_id
    on goods_comment (goods_id);

create index idx_order_id
    on goods_comment (order_id);

create table notice
(
    id      int auto_increment comment 'id'
        primary key,
    content varchar(255) null comment '内容'
)
    comment '公告管理';

create table order_item
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    order_id       bigint                                   not null comment '订单ID',
    goods_id       bigint                                   not null comment '商品ID',
    goods_name     varchar(255)                             null comment '商品名称',
    goods_img      varchar(255)                             null comment '商品主图',
    goods_price    bigint                                   null comment '下单时商品单价（分）',
    quantity       bigint                                   null comment '购买数量',
    total_price    bigint                                   null comment '明细小计（分）',
    comment_status tinyint     default 0                    not null comment '评论状态：0-未评价 1-已评价',
    create_time    datetime(3) default current_timestamp(3) null comment '创建时间'
)
    comment '订单明细表';

create table orders
(
    id           bigint auto_increment comment 'id'
        primary key,
    parent_id    bigint                                   not null comment '父订单id',
    no           varchar(255)                             null comment '订单号',
    user_id      bigint                                   null comment '用户id',
    store_id     bigint                                   null comment '商家id',
    num          bigint                                   null comment '商品数量',
    total_price  bigint                                   null comment '订单总价',
    user_name    varchar(255)                             null comment '下单用户名',
    user_address varchar(255)                             null comment '下单地址',
    user_phone   varchar(255)                             null comment '下单电话',
    status       varchar(255)                             null comment '状态',
    order_type   varchar(20) default 'NORMAL'             null comment '订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单（单店铺）',
    create_time  datetime(3) default current_timestamp(3) null comment '下单时间'
)
    comment '订单管理';

create table store
(
    id         bigint auto_increment comment '店铺ID'
        primary key,
    user_id    bigint                               not null comment '店主用户ID',
    info       varchar(255)                         null comment '店铺简介',
    avatar_url varchar(255)                         null comment '店铺头像url',
    name       varchar(100)                         not null comment '店铺名称',
    banner     varchar(255)                         null comment '店铺顶部横幅背景图 URL',
    updated_at datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    created_at datetime default current_timestamp() not null comment '创建时间'
)
    comment '店铺表' collate = utf8mb4_uca1400_ai_ci;

create index fk_shop_owner
    on store (user_id);

create table user
(
    id         bigint auto_increment comment '用户ID'
        primary key,
    username   varchar(50)                                           not null comment '用户名',
    password   varchar(255)                                          not null comment '用户密码',
    nickname   varchar(100)                                          null comment '用户昵称',
    phone      varchar(30)                                           null comment '手机号',
    email      varchar(100)                                          null comment '电子邮箱',
    bio        varchar(255)                                          null comment '个人简介',
    avatar_url varchar(500)                                          null comment '用户头像URL',
    role       enum ('NORMAL', 'ADMIN', 'MERCHANT') default 'NORMAL' not null comment '用户类型',
    constraint username
        unique (username)
)
    comment '用户表' collate = utf8mb4_uca1400_ai_ci;

