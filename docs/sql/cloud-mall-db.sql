create table address
(
    id           bigint auto_increment comment '地址ID'
        primary key,
    user_id      bigint                              not null comment '用户ID',
    receiver     varchar(100)                        not null comment '收货人名字',
    region_code  int                                 null comment '地区代码',
    detail       varchar(255)                        not null comment '详细地址',
    full_address varchar(255)                        not null comment '完整地址',
    zip_code     varchar(6)                          not null comment '邮编(6位数字)',
    phone        varchar(11)                         not null comment '联系电话(11位手机号)',
    is_default   bit       default b'0'              not null comment '是否为默认地址',
    created_at   timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '地址管理' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create index idx_user_id
    on address (user_id);

create table audit
(
    id             bigint auto_increment comment '批次ID'
        primary key,
    audit_no       varchar(64)                        not null comment '批次编号（如：AUD2024030612345678）',
    biz_type       varchar(50)                        not null comment '业务类型: GOODS_PUBLISH/SHOP_SETTLEMENT/SECKILL_GOODS/REFUND_BATCH...',
    biz_pid        bigint                             null comment '''父业务对象ID''',
    status         char(20)                           not null comment '批次状态: PENDING/APPROVED/REJECTED/PARTIAL',
    total_count    int      default 0                 not null comment '总项目数',
    approved_count int      default 0                 not null comment '已通过数',
    rejected_count int      default 0                 not null comment '已拒绝数',
    applicant_id   bigint                             not null comment '申请人ID',
    applicant_name varchar(255)                       null comment '申请人名称',
    auditor_id     int                                null comment '审核人Id',
    auditor_name   varchar(50)                        null comment '审核人名称',
    audit_time     datetime                           null comment '审核时间',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_audit_no
        unique (audit_no)
)
    comment '审核批次表' engine = InnoDB;

create index idx_applicant
    on audit (applicant_id, biz_type, status);

create index idx_biz_status
    on audit (biz_type, status);

create table audit_item
(
    id           bigint auto_increment comment '明细ID'
        primary key,
    audit_id     bigint                       not null comment '关联批次ID',
    biz_id       bigint                       null comment '业务对象ID（如商品ID）',
    status       char(20)                     not null comment '明细状态: PENDING/APPROVED/REJECTED',
    reason       varchar(255)                 null comment '审批意见/拒绝原因',
    snapshot     longtext collate utf8mb4_bin null comment '业务对象快照JSON',
    prev_item_id bigint                       null comment '上一条审核明细ID',
    is_latest    tinyint(1) default 1         not null comment '是否为最新审核记录:1=是,0=否'
)
    comment '审核明细表' engine = InnoDB;

create index idx_audit_id
    on audit_item (audit_id);

create index idx_status
    on audit_item (status);

create index idx_target
    on audit_item (biz_id);

create table banner
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    image_url    text         null comment '封面图片URL',
    goods_id     bigint       null comment '关联商品ID',
    goods_name   varchar(255) not null comment '关联商品名称',
    is_recommend bit          not null comment '是否在首页推荐'
)
    comment '轮播图表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create table category
(
    id        int unsigned auto_increment comment '主键ID'
        primary key,
    name      varchar(100)              not null comment '分类名称',
    parent_id int unsigned              not null comment '父级分类ID',
    level     int unsigned default '1'  not null comment '分类层级',
    sort      int                       not null comment '排序值',
    status    bit          default b'1' not null comment '状态（1:启用, 0:禁用）'
)
    comment '商品分类表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create index idx_parentId
    on category (parent_id);

create table chat_session
(
    id          bigint auto_increment
        primary key,
    buyer_id    bigint                             not null comment '买家ID',
    agent_id    bigint                             not null comment '店铺客服ID',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_buyer_store
        unique (buyer_id, agent_id)
)
    engine = InnoDB
    collate = utf8mb4_unicode_ci;

create table chat_message
(
    id          bigint auto_increment
        primary key,
    sender_id   bigint                                not null comment '用户ID',
    session_id  bigint                                not null comment '会话ID',
    content     varchar(1000)                         not null comment '消息内容',
    type        varchar(20) default '0'               null comment '消息类型：test文本, image图片',
    is_read     tinyint(1)                            null comment '是否已读',
    create_time datetime    default CURRENT_TIMESTAMP null,
    expire_time datetime                              null comment '过期时间',
    constraint fk_conversation
        foreign key (session_id) references chat_session (id)
            on delete cascade
)
    engine = InnoDB
    collate = utf8mb4_unicode_ci;

create index idx_conversation_id
    on chat_message (session_id);

create index idx_expire_time
    on chat_message (expire_time);

create index idx_buyer_id
    on chat_session (buyer_id);

create index idx_store_id
    on chat_session (agent_id);

create table favorite
(
    id                   bigint auto_increment comment '收藏记录唯一ID'
        primary key,
    user_id              bigint                             not null comment '用户ID',
    goods_id             bigint                             not null comment '商品ID',
    store_id             bigint                             not null comment '店铺ID',
    added_at             datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    goods_name           varchar(255)                       not null comment '商品名称',
    goods_main_image_url text                               not null comment '商品主图',
    goods_price          bigint                             not null comment '商品价格',
    goods_sell_point     varchar(512)                       not null comment '商品描述',
    constraint uniq_user_goods
        unique (user_id, goods_id)
)
    comment '商品收藏表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create table goods
(
    id                 bigint auto_increment comment 'id'
        primary key,
    name               varchar(255)                          null comment '商品名称',
    category_id        bigint                                null comment '分类id',
    category_id_path   varchar(255)                          null comment '分类id路径, 例如: 1/5/7',
    unit_id            bigint                                not null comment '计量单位id',
    unit_name          varchar(255)                          null comment '计量单位名称',
    sell_point         varchar(255)                          null comment '商品卖点',
    description_images text                                  null comment '商品描述图',
    display_images     text                                  not null comment '商品展示图(第一张为主图)',
    store_id           bigint                                null comment '店铺id',
    store_name         varchar(255)                          null comment '商品名称',
    sales              int         default 0                 null comment '总销量',
    max_price          bigint                                null comment '最高价格(单位:分)',
    min_price          bigint                                null comment '最低价格(单位:分)',
    status             tinyint                               null comment '商品上架状态',
    audit_status       varchar(20) default 'PENDING'         not null comment '商品审核状态',
    create_time        datetime    default CURRENT_TIMESTAMP null comment '创建日期',
    create_user        varchar(255)                          null comment '创建用户',
    update_time        datetime    default CURRENT_TIMESTAMP null comment '更新时间',
    update_user        varchar(255)                          null comment '更新用户',
    is_del             bit         default b'0'              not null comment '逻辑删除'
)
    comment '商品管理' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create table goods_comment
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    order_item_id     bigint                                   not null comment '订单明细ID（唯一，一次购买一次评价）',
    order_id          bigint                                   not null comment '订单ID（冗余，便于查询）',
    goods_id          bigint                                   not null comment '商品ID',
    sku_spec_snapshot varchar(255)                             null comment 'SKU规格快照，如：颜色:红 / 内存:512G',
    user_id           bigint                                   not null comment '评论用户ID',
    user_nickname     varchar(100)                             null,
    user_avatar       varchar(500)                             null,
    rating            tinyint                                  not null comment '评分：1~5',
    content           varchar(500)                             not null comment '评论内容',
    reply             varchar(500)                             null comment '商家回复',
    image_urls        varchar(1000)                            null comment '评论图片，逗号分隔URL',
    is_anonymous      tinyint     default 0                    not null comment '是否匿名：0-否 1-是',
    create_time       datetime(3) default CURRENT_TIMESTAMP(3) null comment '创建时间',
    constraint uk_order_item
        unique (order_item_id)
)
    comment '商品评论表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create index idx_goods_id
    on goods_comment (goods_id);

create index idx_order_id
    on goods_comment (order_id);

create table goods_sku
(
    id             bigint auto_increment comment 'sku id'
        primary key,
    goods_id       bigint                             not null comment '所属商品(spu)',
    price          bigint                             not null comment '售价(分)',
    inventory      bigint                             not null comment '库存',
    sales          bigint   default 0                 not null comment '销量',
    status         tinyint  default 1                 not null comment '状态, 1-上架, 0-下架',
    create_time    datetime default CURRENT_TIMESTAMP not null,
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    main_image_url varchar(500)                       null comment '商品主图URL快照（冗余字段）',
    store_id       bigint                             null comment '店铺ID快照（冗余字段）',
    spec_snapshot  varchar(255)                       null comment 'SKU规格组合快照（示例：红 XL 512G）',
    goods_name     varchar(255)                       null comment '商品名称快照'
)
    comment '商品SKU表' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

create index idx_goods_id
    on goods_sku (goods_id);

create table goods_sku_spec
(
    id            bigint auto_increment
        primary key,
    sku_id        bigint not null comment 'sku id',
    spec_id       bigint not null comment '规格id',
    spec_value_id bigint not null comment '规格值id',
    constraint uk_sku_spec
        unique (sku_id, spec_id)
)
    comment 'SKU规格关联表' engine = InnoDB
                            collate = utf8mb4_unicode_ci;

create index idx_sku_id
    on goods_sku_spec (sku_id);

create index idx_spec_value_id
    on goods_sku_spec (spec_value_id);

create table goods_unit
(
    id          bigint auto_increment comment '单位id'
        primary key,
    name        varchar(20)                        not null comment '单位名称，如 件/个/瓶',
    status      tinyint  default 1                 not null comment '状态：1启用 0禁用',
    sort        int      default 0                 not null comment '排序值，越小越靠前',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_unit_name
        unique (name)
)
    comment '商品单位表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create table message_log
(
    id              bigint auto_increment
        primary key,
    biz_id          varchar(64)                        not null comment '业务唯一ID',
    biz_type        varchar(50)                        not null comment '业务场景',
    topic           varchar(128)                       not null comment '消息主题',
    payload         text                               not null comment '消息内容',
    status          tinyint  default 0                 not null comment '0-待处理 1-成功 2-失败 3-已结束',
    retry_count     int      default 0                 not null comment '重试次数',
    next_retry_time datetime                           null comment '下次重试时间',
    error_msg       varchar(500)                       null comment '失败原因',
    create_time     datetime default CURRENT_TIMESTAMP not null,
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_biz
        unique (biz_id, biz_type)
)
    comment '消息日志表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create index idx_create_time
    on message_log (create_time);

create index idx_status_retry
    on message_log (status, next_retry_time);

create table notice
(
    id      int auto_increment comment 'id'
        primary key,
    content varchar(255) null comment '内容'
)
    comment '公告管理' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create table order_item
(
    id                   bigint auto_increment comment '主键ID'
        primary key,
    order_id             bigint                                   not null comment '订单ID',
    goods_id             bigint                                   not null comment '商品ID',
    sku_specs            varchar(255)                             null comment 'SKU规格快照，如：颜色=黑色;尺码=L',
    goods_name           varchar(255)                             null comment '商品名称',
    goods_main_image_url text                                     null comment '商品主图url',
    goods_price          bigint                                   null comment '下单时商品单价（分）',
    quantity             int                                      null comment '购买数量',
    total_price          bigint                                   null comment '明细小计（分）',
    comment_status       tinyint     default 0                    not null comment '评论状态：0-未评价 1-已评价',
    create_time          datetime(3) default CURRENT_TIMESTAMP(3) null comment '创建时间',
    sku_id               bigint                                   not null comment 'SKU ID（真实下单对象）'
)
    comment '订单明细表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create table orders
(
    id          bigint auto_increment comment 'id'
        primary key,
    parent_id   bigint                                   null comment '父订单id',
    no          varchar(255)                             null comment '订单号',
    user_id     bigint                                   null comment '用户id',
    store_id    bigint                                   null comment '商家id',
    quantity    int                                      null comment '商品数量',
    total_price bigint                                   null comment '订单总价',
    user_name   varchar(255)                             null comment '下单用户名',
    address     varchar(255)                             null comment '下单地址',
    phone       varchar(255)                             null comment '下单电话',
    status      varchar(255)                             null comment '状态',
    order_type  varchar(20) default 'NORMAL'             null comment '订单类型：PARENT-父订单, SUB-子订单, NORMAL-普通订单（单店铺）',
    reason      varchar(255)                             null comment '订单取消或关闭原因（用于CANCELED/CLOSED状态）',
    create_time datetime(3) default CURRENT_TIMESTAMP(3) null comment '下单时间'
)
    comment '订单管理' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create table resources
(
    id          bigint auto_increment comment '资源ID'
        primary key,
    type        varchar(55)                          not null comment '资源类型: menu, catalog, button',
    code        varchar(255)                         null comment '资源代码(如: order:add, order:edit)',
    meta        longtext collate utf8mb4_bin         null comment '前端元信息（如路由path、组件component、icon、keep_alive等）',
    sort        int                                  null comment '排序值',
    parent_id   bigint     default 0                 null comment '父资源ID，顶级为0',
    enable      tinyint(1) default 1                 not null comment '是否启用',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '系统资源表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create table roles
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    name        varchar(50)                          not null comment '角色名称',
    built_in    tinyint(1) default 0                 not null comment '是否为内置角色(不可删除)',
    enable      tinyint(1) default 1                 null comment '是否启用',
    description varchar(255)                         null comment '角色描述',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    constraint name
        unique (name)
)
    comment '系统角色表' engine = InnoDB
                         collate = utf8mb4_unicode_ci;

create table role_resources
(
    id          bigint auto_increment
        primary key,
    role_id     bigint not null comment '角色ID',
    resource_id bigint not null comment '资源ID',
    constraint uk_role_resource
        unique (role_id, resource_id),
    constraint role_resources_ibfk_1
        foreign key (role_id) references roles (id)
            on delete cascade,
    constraint role_resources_ibfk_2
        foreign key (resource_id) references resources (id)
            on delete cascade
)
    comment '角色与资源关联表' engine = InnoDB
                               collate = utf8mb4_unicode_ci;

create index resource_id
    on role_resources (resource_id);

create table seckill_activity
(
    id            bigint auto_increment
        primary key,
    name          varchar(255)                       not null comment '活动名称',
    start_hour    tinyint                            not null comment '开始小时（0-23）',
    activity_date date                               not null comment '活动日期',
    status        tinyint  default 0                 not null comment '状态：0报名中 1进行中 2已结束',
    max_items     int                                null comment '活动最大商品数',
    create_time   datetime default CURRENT_TIMESTAMP null,
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_date_hour
        unique (activity_date, start_hour)
)
    comment '秒杀活动表' engine = InnoDB;

create index idx_date
    on seckill_activity (activity_date);

create index idx_status
    on seckill_activity (status);

create table seckill_goods
(
    id             bigint auto_increment
        primary key,
    activity_id    bigint                             not null comment '活动ID（外键）',
    sku_id         bigint                             not null comment '商品SKU_ID（外键）',
    goods_name     varchar(255)                       null comment '商品名称快照',
    main_image_url varchar(500)                       null comment '商品主图URL快照',
    merchant_id    bigint                             not null comment '商家ID（外键）',
    origin_price   bigint                             null comment '商品原价',
    seckill_price  bigint                             not null comment '秒杀价格',
    stock          int                                not null comment '秒杀库存',
    sold_count     int      default 0                 null comment '已售数量',
    create_time    datetime default CURRENT_TIMESTAMP null,
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_activity_product
        unique (activity_id, sku_id),
    constraint seckill_goods_ibfk_1
        foreign key (activity_id) references seckill_activity (id)
)
    comment '秒杀商品表 - 只包含审核通过的秒杀商品' engine = InnoDB;

create index idx_merchant
    on seckill_goods (merchant_id);

create table seckill_order
(
    id          bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id     bigint unsigned                        not null comment '用户ID',
    goods_id    bigint unsigned                        not null comment '秒杀商品ID',
    order_no    varchar(64)                            not null comment '订单号（唯一）',
    quantity    int unsigned default '1'               not null comment '购买数量',
    price       decimal(10, 2)                         not null comment '秒杀价格',
    status      tinyint      default 0                 not null comment '订单状态（0-未支付, 1-已支付, 2-已取消）',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    pay_time    datetime                               null comment '支付时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_order_no
        unique (order_no),
    constraint uk_user_goods
        unique (user_id, goods_id)
)
    comment '秒杀订单表' engine = InnoDB;

create index idx_goods_id
    on seckill_order (goods_id);

create index idx_status
    on seckill_order (status);

create table spec
(
    id          bigint auto_increment comment '规格id'
        primary key,
    name        varchar(50)                        not null comment '规格名，如 颜色、尺码',
    sort        int      default 0                 not null comment '排序',
    status      tinyint  default 1                 not null comment '状态 1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_spec_name
        unique (name)
)
    comment '规格名表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create table spec_value
(
    id          bigint auto_increment comment '规格值id'
        primary key,
    spec_id     bigint                             not null comment '所属规格',
    value       varchar(50)                        not null comment '规格值，如 红、XL',
    sort_order  int      default 0                 not null comment '排序',
    status      tinyint  default 1                 not null comment '状态',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_spec_value
        unique (spec_id, value)
)
    comment '规格值表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

create index idx_spec_id
    on spec_value (spec_id);

create table store
(
    id         bigint auto_increment comment '店铺ID'
        primary key,
    no         varchar(40)                        null comment '店铺编号',
    user_id    bigint                             not null comment '店主用户ID',
    info       varchar(255)                       null comment '店铺简介',
    avatar_url varchar(500)                       null comment '店铺头像url',
    name       varchar(100)                       not null comment '店铺名称',
    banner     varchar(500)                       null comment '店铺顶部横幅背景图 URL',
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '店铺表' engine = InnoDB
                     collate = utf8mb4_unicode_ci;

create index fk_shop_owner
    on store (user_id);

create table user
(
    id         bigint auto_increment comment '用户ID'
        primary key,
    username   varchar(50)                                          not null comment '用户名',
    password   varchar(255)                                         not null comment '用户密码',
    nickname   varchar(100)                                         null comment '用户昵称',
    phone      varchar(30)                                          null comment '手机号',
    email      varchar(100)                                         null comment '电子邮箱',
    bio        varchar(255)                                         null comment '个人简介',
    avatar_url varchar(500)                                         null comment '用户头像URL',
    types      set ('NORMAL', 'ADMIN', 'MERCHANT') default 'NORMAL' null comment '用户类型',
    constraint username
        unique (username)
)
    comment '用户表' engine = InnoDB
                     collate = utf8mb4_unicode_ci;

create table user_qualification
(
    id                  bigint auto_increment comment '资质认证ID'
        primary key,
    audit_status        varchar(30) default 'PENDING'         not null comment '审核状态',
    user_id             bigint                                not null comment '用户ID',
    subject_type        varchar(20)                           not null comment '主体类型: personal/individual/enterprise',
    real_name           varchar(50)                           not null comment '真实姓名',
    id_card             varchar(30)                           not null comment '身份证号',
    id_card_valid_start date                                  not null comment '身份证有效期起',
    id_card_valid_end   date                                  not null comment '身份证有效期止',
    id_card_front       varchar(255)                          not null comment '身份证正面照片URL',
    id_card_back        varchar(255)                          not null comment '身份证反面照片URL',
    license_number      varchar(50)                           null comment '营业执照编号',
    license_name        varchar(100)                          null comment '营业执照名称',
    establishment_date  date                                  null comment '营业执照成立日期',
    registered_address  varchar(255)                          null comment '注册地址',
    license_photo       varchar(255)                          null comment '营业执照照片URL',
    categories          varchar(255)                          null comment '可经营类目（逗号分隔或JSON）',
    account_name        varchar(50)                           null comment '开户人姓名',
    card_number         varchar(30)                           null comment '银行卡号',
    bank_name           varchar(100)                          null comment '开户银行',
    branch_name         varchar(100)                          null comment '开户支行',
    mobile              varchar(20)                           null comment '银行预留手机号',
    created_at          datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户资质认证信息表（包含银行卡信息）' engine = InnoDB
                                                 collate = utf8mb4_unicode_ci;

create index idx_subject_type
    on user_qualification (subject_type);

create index idx_user_id
    on user_qualification (user_id);

create table user_roles
(
    user_id     bigint                             not null comment '用户ID',
    role_id     bigint                             not null comment '角色ID',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    primary key (user_id, role_id),
    constraint user_roles_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint user_roles_ibfk_2
        foreign key (role_id) references roles (id)
            on delete cascade
)
    comment '用户与角色关联表' engine = InnoDB
                               collate = utf8mb4_unicode_ci;

create index role_id
    on user_roles (role_id);