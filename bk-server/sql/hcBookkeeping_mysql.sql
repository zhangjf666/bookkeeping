/*==============================================================*/
/* Table: sys_dept                                              */
/*==============================================================*/
create table sys_dept
(
   id                   bigint(20) not null auto_increment comment 'id',
   name                 varchar(100) not null comment '名称',
   pid                  bigint(20) comment '上级部门id',
   sort                 int(5) comment '排序',
   type                 char(1) comment '部门类型',
   address              varchar(255) comment '联系地址',
   zip_code             varchar(100) comment '邮政编码',
   master               varchar(100) comment '负责人',
   phone                varchar(100) comment '电话',
   fax                  varchar(100) comment '传真',
   email                varchar(100) comment '邮箱',
   enabled              char(1) default '1' comment '状态(1:启用 0:禁用)',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   del_flag             char(1) not null default '0' comment '删除标记',
   primary key (id)
);

alter table sys_dept comment '部门表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_dept
(
   name
);

/*==============================================================*/
/* Table: sys_dict                                              */
/*==============================================================*/
create table sys_dict
(
   id                   bigint(20) not null auto_increment comment 'id',
   name                 varchar(255) not null comment '字典名称',
   description          varchar(255) comment '字典描述',
   type                 char(1) comment '字典类型(0:普通,1:树形)',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   primary key (id)
);

alter table sys_dict comment '数据字典表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_dict
(
   name
);

/*==============================================================*/
/* Table: sys_dict_detail                                       */
/*==============================================================*/
create table sys_dict_detail
(
   id                   bigint(20) not null auto_increment comment 'id',
   dict_id              bigint(20) not null comment '字典id',
   value                varchar(100) not null comment '数据值',
   name                 varchar(100) not null comment '数据名称',
   sort                 int(5) comment '排序',
   pid                  bigint(20) comment '上级id',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   primary key (id)
);

alter table sys_dict_detail comment '字典明细表';

/*==============================================================*/
/* Index: value                                                 */
/*==============================================================*/
create index value on sys_dict_detail
(
   value
);

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_dict_detail
(
   name
);

/*==============================================================*/
/* Index: dict_id                                               */
/*==============================================================*/
create index dict_id on sys_dict_detail
(
   dict_id
);

/*==============================================================*/
/* Table: sys_log                                               */
/*==============================================================*/
create table sys_log
(
   id                   bigint(20) not null auto_increment comment 'id',
   type                 varchar(10) default '1' comment '日志类型',
   title                varchar(255) default '' comment '日志标题',
   ip                   varchar(255) comment '操作IP地址',
   browser              varchar(255) comment '浏览器',
   user_agent           varchar(255) comment '用户代理',
   request_uri          varchar(255) comment '请求URI',
   method               varchar(400) comment '操作方式',
   params               text comment '操作提交的数据',
   request_time         datetime comment '请求时间',
   execute_time         varchar(255) comment '执行时长(ms)',
   response             text comment '返回内容',
   exception            text comment '异常信息',
   level                varchar(20) comment '日志等级',
   create_by            varchar(100) comment '访问用户',
   create_time          datetime comment '创建时间',
   primary key (id)
);

alter table sys_log comment '日志表';

/*==============================================================*/
/* Index: type                                                  */
/*==============================================================*/
create index type on sys_log
(
   type
);

/*==============================================================*/
/* Index: title                                                 */
/*==============================================================*/
create index title on sys_log
(
   title
);

/*==============================================================*/
/* Index: create_by                                             */
/*==============================================================*/
create index create_by on sys_log
(
   create_by
);

/*==============================================================*/
/* Table: sys_menu                                              */
/*==============================================================*/
create table sys_menu
(
   id                   bigint(20) not null auto_increment comment 'id',
   pid                  bigint(20) comment '上级菜单id',
   name                 varchar(100) not null comment '名称',
   description          varchar(200) comment '菜单描述',
   sort                 int(5) comment '排序',
   url                  varchar(2000) comment '访问url',
   is_outside           char(1) default '0' comment '是否外部地址',
   type                 char(1) comment '菜单类型(1:目录,2:菜单,3:按钮)',
   icon                 varchar(100) comment '图标',
   component_name       varchar(100) comment '组件名称',
   component            varchar(300) comment '组件地址',
   is_show              char(1) default '1' comment '是否显示(1:显示 0:不显示)',
   permission           varchar(200) comment '权限标识',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   primary key (id)
);

alter table sys_menu comment '菜单表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_menu
(
   name
);

/*==============================================================*/
/* Table: sys_param                                             */
/*==============================================================*/
create table sys_param
(
   id                  bigint(20) not null auto_increment comment 'id',
   name                 varchar(100) not null comment '参数名',
   value                varchar(100) comment '参数值',
   description          varchar(255) comment '参数描述',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   del_flag             char(1) default '0' comment '删除标记',
   primary key (id)
);

alter table sys_param comment '系统参数表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_param
(
   name
);

/*==============================================================*/
/* Table: sys_position                                          */
/*==============================================================*/
create table sys_position
(
   id                   bigint(20) not null auto_increment comment 'id',
   name                 varchar(100) not null comment '名称',
   description          varchar(255) comment '描述',
   name_en              varchar(200) comment '英文名称',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   primary key (id)
);

alter table sys_position comment '职位表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_position
(
   name
);

/*==============================================================*/
/* Index: name_en                                               */
/*==============================================================*/
create index name_en on sys_position
(
   name_en
);

/*==============================================================*/
/* Table: sys_role                                              */
/*==============================================================*/
create table sys_role
(
   id                   bigint(20) not null auto_increment comment 'id',
   name                 varchar(100) not null comment '角色名称',
   description          varchar(200) comment '描述',
   data_scope           char(1) comment '数据范围',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   primary key (id)
);

alter table sys_role comment '角色表';

/*==============================================================*/
/* Index: name                                                  */
/*==============================================================*/
create index name on sys_role
(
   name
);

/*==============================================================*/
/* Table: sys_role_dept                                         */
/*==============================================================*/
create table sys_role_dept
(
   id                   bigint(20) not null auto_increment comment 'id',
   role_id              bigint(20) not null comment '角色id',
   dept_id              bigint(20) not null comment '部门id',
   primary key (id)
);

alter table sys_role_dept comment '角色-部门表';

/*==============================================================*/
/* Index: role_id                                               */
/*==============================================================*/
create index role_id on sys_role_dept
(
   role_id
);

/*==============================================================*/
/* Table: sys_role_menu                                         */
/*==============================================================*/
create table sys_role_menu
(
   id                   bigint(20) not null auto_increment comment 'id',
   role_id              bigint(20) not null comment '角色id',
   menu_id              bigint(20) not null comment '菜单id',
   primary key (id)
);

alter table sys_role_menu comment '角色-菜单表';

/*==============================================================*/
/* Index: role_id                                               */
/*==============================================================*/
create index role_id on sys_role_menu
(
   role_id
);

/*==============================================================*/
/* Table: sys_user                                              */
/*==============================================================*/
create table sys_user
(
   id                   bigint(20) not null auto_increment comment 'id',
   dept_id              bigint(20) comment '部门id',
   username             varchar(100) not null comment '用户名',
   password             varchar(255) not null comment '密码',
   nick_name            varchar(100) comment '昵称',
   no                   varchar(100) comment '工号',
   gender               char(1) comment '性别',
   email                varchar(100) comment '邮箱',
   phone                varchar(100) comment '电话',
   mobile_phone         varchar(100) comment '手机',
   type                 char(1) default '0' comment '用户类型(0:普通用户,1:超级管理员))',
   avatar               varchar(1000) comment '用户头像',
   enabled              char(1) default '1' comment '状态:1:启用 0:禁用',
   create_by            varchar(100) comment '创建者',
   create_time          datetime comment '创建时间',
   update_by            varchar(100) comment '更新者',
   update_time          datetime comment '更新时间',
   remarks              varchar(255) comment '备注信息',
   del_flag             char(1) not null default '0' comment '删除标记(0:未删除,1已删除)',
   primary key (id)
);

alter table sys_user comment '用户表';

/*==============================================================*/
/* Index: username                                              */
/*==============================================================*/
create index username on sys_user
(
   username
);

/*==============================================================*/
/* Table: sys_user_position                                     */
/*==============================================================*/
create table sys_user_position
(
   id                   bigint(20) not null auto_increment comment 'id',
   user_id              bigint(20) not null comment '用户id',
   position_id          bigint(20) not null comment '职位id',
   primary key (id)
);

alter table sys_user_position comment '用户-职位表';

/*==============================================================*/
/* Index: user_id                                               */
/*==============================================================*/
create index user_id on sys_user_position
(
   user_id
);

/*==============================================================*/
/* Table: sys_user_role                                         */
/*==============================================================*/
create table sys_user_role
(
   id                   bigint(20) not null auto_increment comment 'id',
   user_id              bigint(20) not null comment '用户id',
   role_id              bigint(20) not null comment '角色id',
   primary key (id)
);

alter table sys_user_role comment '用户-角色表';

/*==============================================================*/
/* Index: user_id                                               */
/*==============================================================*/
create index user_id on sys_user_role
(
   user_id
);

-- ---
-- Table 'account_book'
-- 账本
-- ---

DROP TABLE IF EXISTS `account_book`;

CREATE TABLE `account_book` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` VARCHAR(200) NOT NULL COMMENT '名称',
  `description` VARCHAR(2000) NULL DEFAULT NULL COMMENT '描述',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户id',
  `image` VARCHAR(200) NULL DEFAULT NULL COMMENT '图标名称',
  `sort` INTEGER(10) NOT NULL DEFAULT 0 COMMENT '排序',
  `is_default` CHAR NOT NULL DEFAULT '0' COMMENT '是否默认账本(0:否,1:是)',
  PRIMARY KEY (`id`)
) COMMENT '账本';

-- ---
-- Table 'classify'
-- 分类表
-- ---

DROP TABLE IF EXISTS `classify`;

CREATE TABLE `classify` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` VARCHAR(100) NOT NULL COMMENT '名称',
  `pid` BIGINT(20) NOT NULL DEFAULT -1 COMMENT '父类id',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户id',
  `image` VARCHAR(200) NULL DEFAULT NULL COMMENT '图标名称',
  `sort` INTEGER(10) NOT NULL DEFAULT 0 COMMENT '排序',
  `type` CHAR(1) NOT NULL DEFAULT '0' COMMENT '类型(0:支出,1:收入)',
  `enable` CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用(0:不启用,1:启用)',
  PRIMARY KEY (`id`)
) COMMENT '分类表';

-- ---
-- Table 'income_expense'
-- 收入支出表
-- ---

DROP TABLE IF EXISTS `income_expense`;

CREATE TABLE `income_expense` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户id',
  `account_book_id` BIGINT(20) NOT NULL COMMENT '账本id',
  `type` CHAR(1) NOT NULL DEFAULT '0' COMMENT '类型(0:支出,1:收入)',
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `date` DATE NOT NULL COMMENT '产生时间',
  `remark` VARCHAR(1000) NULL DEFAULT NULL COMMENT '备注',
  `main_classify` BIGINT(20) NULL DEFAULT NULL COMMENT '主分类id',
  `sub_classify` BIGINT(20) NULL DEFAULT NULL COMMENT '子分类id',
  `is_credit_card` CHAR(1) NULL DEFAULT NULL COMMENT '是否信用卡消费(0:否,1;是)',
  `create_time`   datetime NOT NULL comment '创建时间',
  `update_time`   datetime NOT NULL comment '更新时间',
  PRIMARY KEY (`id`)
) COMMENT '收入支出表';

-- ---
-- Table 'user_config'
-- 用户配置表
-- ---

DROP TABLE IF EXISTS `user_config`;

CREATE TABLE `user_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户id',
  `name` VARCHAR(100) NOT NULL COMMENT '配置项目名',
  `value` VARCHAR(100) NOT NULL COMMENT '配置项的值',
  `description` VARCHAR(1000) NULL DEFAULT NULL COMMENT '描述',
  `enable` CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用(0:不启用,1:启用)',
  PRIMARY KEY (`id`)
) COMMENT '用户配置表';

-- ---
-- Table 'icon_config'
-- 图标配置表
-- ---

DROP TABLE IF EXISTS `icon_config`;

CREATE TABLE `icon_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `code` VARCHAR(100) NOT NULL COMMENT '图标代码',
  `type` CHAR(1) NOT NULL DEFAULT '0' COMMENT '图标类型(0:支出,1:收入)',
  PRIMARY KEY (`id`)
) COMMENT '图标配置表';

-- ---
-- Table 'user_search'
-- 用户搜索记录表
-- ---

DROP TABLE IF EXISTS `user_search`;

CREATE TABLE `user_search` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户id',
  `content` VARCHAR(100) NOT NULL COMMENT '搜索内容',
  `create_time`   datetime NOT NULL comment '创建时间',
  PRIMARY KEY (`id`)
) COMMENT '用户搜索记录表';