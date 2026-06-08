-- 进销存系统数据库初始化脚本
-- Database: wms

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `status` tinyint DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 系统菜单表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `type` tinyint DEFAULT 1 COMMENT '类型 1目录 2菜单 3按钮',
  `sort` int DEFAULT 0 COMMENT '排序',
  `visible` tinyint DEFAULT 1 COMMENT '是否显示',
  `status` tinyint DEFAULT 1,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ----------------------------
-- 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父部门ID',
  `sort` int DEFAULT 0 COMMENT '排序',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作',
  `method` varchar(200) DEFAULT NULL COMMENT '方法',
  `params` text COMMENT '参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `exec_time` bigint DEFAULT NULL COMMENT '执行时长(ms)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 供应商表
-- ----------------------------
DROP TABLE IF EXISTS `biz_supplier`;
CREATE TABLE `biz_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(50) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(50) DEFAULT NULL COMMENT '银行账号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ----------------------------
-- 客户表
-- ----------------------------
DROP TABLE IF EXISTS `biz_customer`;
CREATE TABLE `biz_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_code` varchar(50) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `credit_limit` decimal(12,2) DEFAULT 0 COMMENT '信用额度',
  `remark` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ----------------------------
-- 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `biz_category`;
CREATE TABLE `biz_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
  `sort` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 商品表
-- ----------------------------
DROP TABLE IF EXISTS `biz_goods`;
CREATE TABLE `biz_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_code` varchar(50) NOT NULL COMMENT '商品编码',
  `goods_name` varchar(100) NOT NULL COMMENT '商品名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `purchase_price` decimal(12,2) DEFAULT 0 COMMENT '采购价',
  `sale_price` decimal(12,2) DEFAULT 0 COMMENT '销售价',
  `safety_stock` int DEFAULT 0 COMMENT '安全库存',
  `max_stock` int DEFAULT 99999 COMMENT '最大库存',
  `remark` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------
-- 仓库表
-- ----------------------------
DROP TABLE IF EXISTS `biz_warehouse`;
CREATE TABLE `biz_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `warehouse_code` varchar(50) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `manager` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(500) DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `deleted` tinyint DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- ----------------------------
-- 库存表
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock`;
CREATE TABLE `biz_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int DEFAULT 0 COMMENT '库存数量',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_warehouse` (`goods_id`, `warehouse_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ----------------------------
-- 采购单表
-- ----------------------------
DROP TABLE IF EXISTS `biz_purchase_order`;
CREATE TABLE `biz_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT '采购单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint DEFAULT NULL COMMENT '入库仓库ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `total_amount` decimal(12,2) DEFAULT 0 COMMENT '总金额',
  `status` tinyint DEFAULT 0 COMMENT '状态 0待审批 1已通过 2已拒绝 3已入库',
  `remark` varchar(500) DEFAULT NULL,
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `inbound_time` datetime DEFAULT NULL COMMENT '入库时间',
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_status` (`status`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购单表';

-- ----------------------------
-- 采购单明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_purchase_item`;
CREATE TABLE `biz_purchase_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '采购单ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(12,2) NOT NULL COMMENT '单价',
  `amount` decimal(12,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购单明细表';

-- ----------------------------
-- 销售单表
-- ----------------------------
DROP TABLE IF EXISTS `biz_sale_order`;
CREATE TABLE `biz_sale_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT '销售单号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint DEFAULT NULL COMMENT '出库仓库ID',
  `salesman_id` bigint NOT NULL COMMENT '销售员ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `total_amount` decimal(12,2) DEFAULT 0 COMMENT '总金额',
  `received_amount` decimal(12,2) DEFAULT 0 COMMENT '已收金额',
  `status` tinyint DEFAULT 0 COMMENT '状态 -1报价中 0待审核 1已审核 2已发货 3已收款 4已取消',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `quote_time` datetime DEFAULT NULL COMMENT '报价时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '客户确认时间',
  `approve_time` datetime DEFAULT NULL,
  `approver_id` bigint DEFAULT NULL,
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收款时间',
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单表';

-- ----------------------------
-- 销售单明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_sale_item`;
CREATE TABLE `biz_sale_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '销售单ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(12,2) NOT NULL COMMENT '单价',
  `amount` decimal(12,2) NOT NULL COMMENT '金额',
  `cost_price` decimal(12,2) DEFAULT NULL COMMENT '成本价(发货时记录)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单明细表';

-- ----------------------------
-- 库存记录表(出入库流水)
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock_record`;
CREATE TABLE `biz_stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `record_type` varchar(20) NOT NULL COMMENT '类型 IN入库 OUT出库',
  `quantity` int NOT NULL COMMENT '数量',
  `before_qty` int DEFAULT 0 COMMENT '变动前数量',
  `after_qty` int DEFAULT 0 COMMENT '变动后数量',
  `ref_type` varchar(50) DEFAULT NULL COMMENT '关联类型',
  `ref_id` bigint DEFAULT NULL COMMENT '关联ID',
  `ref_no` varchar(50) DEFAULT NULL COMMENT '关联单号',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存记录表';

-- ----------------------------
-- 库存盘点单表
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock_check`;
CREATE TABLE `biz_stock_check` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_no` varchar(50) NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` tinyint DEFAULT 0 COMMENT '状态 0进行中 1已完成',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点单表';

-- ----------------------------
-- 库存盘点明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock_check_item`;
CREATE TABLE `biz_stock_check_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_id` bigint NOT NULL COMMENT '盘点单ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `system_qty` int DEFAULT 0 COMMENT '系统数量',
  `actual_qty` int DEFAULT 0 COMMENT '实际数量',
  `diff_qty` int DEFAULT 0 COMMENT '差异数量',
  PRIMARY KEY (`id`),
  KEY `idx_check_id` (`check_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- ----------------------------
-- 库存调拨表
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock_transfer`;
CREATE TABLE `biz_stock_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `transfer_no` varchar(50) NOT NULL COMMENT '调拨单号',
  `from_warehouse_id` bigint NOT NULL COMMENT '调出仓库ID',
  `to_warehouse_id` bigint NOT NULL COMMENT '调入仓库ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '调拨数量',
  `status` tinyint DEFAULT 1 COMMENT '状态 1已完成',
  `operator_id` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨表';

-- ----------------------------
-- 库存调整表(报损报溢)
-- ----------------------------
DROP TABLE IF EXISTS `biz_stock_adjust`;
CREATE TABLE `biz_stock_adjust` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `adjust_no` varchar(50) NOT NULL COMMENT '调整单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `adjust_type` varchar(20) NOT NULL COMMENT '类型 LOSS报损 OVERFLOW报溢',
  `quantity` int NOT NULL COMMENT '调整数量',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `operator_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adjust_no` (`adjust_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整表';

-- ----------------------------
-- 验证码缓存表
-- ----------------------------
DROP TABLE IF EXISTS `sys_captcha`;
CREATE TABLE `sys_captcha` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(50) NOT NULL COMMENT '唯一标识',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码缓存表';

-- ----------------------------
-- 系统备份表
-- ----------------------------
DROP TABLE IF EXISTS `sys_backup`;
CREATE TABLE `sys_backup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `backup_name` varchar(200) NOT NULL COMMENT '备份名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小(字节)',
  `backup_type` varchar(20) DEFAULT 'MANUAL' COMMENT '备份类型 MANUAL手动 AUTO自动',
  `status` tinyint DEFAULT 1 COMMENT '状态 0失败 1成功',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统备份表';

-- ----------------------------
-- 系统通知表
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `type` varchar(50) DEFAULT 'SYSTEM' COMMENT '类型 STOCK_WARNING库存预警 SYSTEM系统通知',
  `user_id` bigint DEFAULT NULL COMMENT '接收用户ID，null表示所有用户',
  `status` tinyint DEFAULT 0 COMMENT '状态 0未读 1已读',
  `ref_id` bigint DEFAULT NULL COMMENT '关联ID',
  `ref_type` varchar(50) DEFAULT NULL COMMENT '关联类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ----------------------------
-- 收款记录表
-- ----------------------------
DROP TABLE IF EXISTS `biz_payment_record`;
CREATE TABLE `biz_payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payment_no` varchar(50) NOT NULL COMMENT '收款单号',
  `order_id` bigint NOT NULL COMMENT '关联销售单ID',
  `order_no` varchar(50) DEFAULT NULL COMMENT '关联销售单号',
  `amount` decimal(12,2) NOT NULL COMMENT '收款金额',
  `payment_method` varchar(20) DEFAULT 'BANK' COMMENT '收款方式: CASH现金 BANK银行转账 ALIPAY支付宝 WECHAT微信',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收款时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款记录表';

-- ----------------------------
-- 用户偏好设置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_preference`;
CREATE TABLE `sys_user_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pref_key` varchar(100) NOT NULL COMMENT '偏好键',
  `pref_value` text COMMENT '偏好值(JSON)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pref` (`user_id`, `pref_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好设置表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 初始化部门
INSERT INTO `sys_dept` (`id`, `dept_name`, `parent_id`, `sort`, `leader`) VALUES
(1, '总公司', 0, 1, '管理员'),
(2, '采购部', 1, 1, NULL),
(3, '销售部', 1, 2, NULL),
(4, '仓储部', 1, 3, NULL),
(5, '财务部', 1, 4, NULL);

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限'),
(2, '采购员', 'PURCHASER', '采购相关权限'),
(3, '销售员', 'SALESMAN', '销售相关权限'),
(4, '仓库管理员', 'WAREHOUSE_ADMIN', '仓库相关权限');

-- 初始化用户 密码: admin123 (BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `dept_id`, `status`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138000', 1, 1),
(2, 'purchase', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '采购员', '13800138001', 2, 1),
(3, 'sales', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '销售员', '13800138002', 3, 1);

-- 初始化用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2), (3, 3);

-- 初始化菜单 (使用 Ant Design Vue 图标名称)
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `path`, `component`, `perms`, `icon`, `type`, `sort`, `visible`) VALUES
(1, '首页', 0, '/dashboard', 'dashboard/index', 'dashboard:view', 'home', 1, 1, 1),
(2, '系统管理', 0, '/system', NULL, NULL, 'setting', 1, 2, 1),
(3, '员工管理', 2, '/system/user', 'system/user/index', 'sys:user:list', 'user', 2, 1, 1),
(4, '角色管理', 2, '/system/role', 'system/role/index', 'sys:role:list', 'team', 2, 2, 1),
(5, '菜单管理', 2, '/system/menu', 'system/menu/index', 'sys:menu:list', 'menu', 2, 3, 1),
(6, '部门管理', 2, '/system/dept', 'system/dept/index', 'sys:dept:list', 'apartment', 2, 4, 1),
(7, '操作日志', 2, '/system/log', 'system/log/index', 'sys:log:list', 'file-text', 2, 5, 1),
(35, '数据备份', 2, '/system/backup', 'system/backup/index', 'sys:backup:list', 'save', 2, 6, 1),
(8, '基础数据', 0, '/base', NULL, NULL, 'database', 1, 3, 1),
(9, '供应商管理', 8, '/base/supplier', 'base/supplier/index', 'biz:supplier:list', 'contacts', 2, 1, 1),
(10, '客户管理', 8, '/base/customer', 'base/customer/index', 'biz:customer:list', 'solution', 2, 2, 1),
(11, '商品管理', 8, '/base/goods', 'base/goods/index', 'biz:goods:list', 'gift', 2, 3, 1),
(12, '仓库管理', 8, '/base/warehouse', 'base/warehouse/index', 'biz:warehouse:list', 'inbox', 2, 4, 1),
(13, '采购管理', 0, '/purchase', NULL, NULL, 'shopping-cart', 1, 4, 1),
(14, '采购申请', 13, '/purchase/apply', 'purchase/apply/index', 'biz:purchase:apply', 'form', 2, 1, 1),
(15, '采购单管理', 13, '/purchase/order', 'purchase/order/index', 'biz:purchase:list', 'file-done', 2, 2, 1),
(16, '采购审批', 13, '/purchase/approve', 'purchase/approve/index', 'biz:purchase:approve', 'audit', 2, 3, 1),
(17, '采购入库', 13, '/purchase/inbound', 'purchase/inbound/index', 'biz:purchase:inbound', 'import', 2, 4, 1),
(18, '销售管理', 0, '/sale', NULL, NULL, 'shop', 1, 5, 1),
(19, '销售报价', 18, '/sale/quote', 'sale/quote/index', 'biz:sale:quote', 'dollar', 2, 1, 1),
(20, '销售单管理', 18, '/sale/order', 'sale/order/index', 'biz:sale:list', 'file-done', 2, 2, 1),
(21, '销售审核', 18, '/sale/approve', 'sale/approve/index', 'biz:sale:approve', 'audit', 2, 3, 1),
(22, '发货出库', 18, '/sale/ship', 'sale/ship/index', 'biz:sale:ship', 'export', 2, 4, 1),
(23, '财务收款', 18, '/sale/receive', 'sale/receive/index', 'biz:sale:receive', 'dollar', 2, 5, 1),
(24, '库存管理', 0, '/stock', NULL, NULL, 'inbox', 1, 6, 1),
(25, '库存查询', 24, '/stock/query', 'stock/query/index', 'biz:stock:list', 'search', 2, 1, 1),
(26, '库存盘点', 24, '/stock/check', 'stock/check/index', 'biz:stock:check', 'solution', 2, 2, 1),
(27, '库存调拨', 24, '/stock/transfer', 'stock/transfer/index', 'biz:stock:transfer', 'swap', 2, 3, 1),
(28, '报损报溢', 24, '/stock/adjust', 'stock/adjust/index', 'biz:stock:adjust', 'edit', 2, 4, 1),
(29, '库存预警', 24, '/stock/warning', 'stock/warning/index', 'biz:stock:warning', 'alert', 2, 5, 1),
(30, '统计报表', 0, '/report', NULL, NULL, 'bar-chart', 1, 7, 1),
(31, '销售排行', 30, '/report/sale-rank', 'report/sale-rank/index', 'report:sale:rank', 'rise', 2, 1, 1),
(32, '采购趋势', 30, '/report/purchase-trend', 'report/purchase-trend/index', 'report:purchase:trend', 'line-chart', 2, 2, 1),
(33, '库存预警报表', 30, '/report/stock-warning', 'report/stock-warning/index', 'report:stock:warning', 'warning', 2, 3, 1),
(34, '毛利分析', 30, '/report/profit', 'report/profit/index', 'report:profit', 'fund', 2, 4, 1);

-- 超级管理员拥有所有菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 1, id FROM `sys_menu`;

-- 采购员权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 1), (2, 8), (2, 9), (2, 11), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17);

-- 销售员权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 1), (3, 8), (3, 10), (3, 11), (3, 18), (3, 19), (3, 20), (3, 21), (3, 22), (3, 23);

-- 仓库管理员权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(4, 1), (4, 8), (4, 11), (4, 12), (4, 24), (4, 25), (4, 26), (4, 27), (4, 28), (4, 29);

-- 初始化仓库
INSERT INTO `biz_warehouse` (`id`, `warehouse_code`, `warehouse_name`, `address`, `manager`, `phone`) VALUES
(1, 'WH001', '北京主仓库', '北京市朝阳区建国路88号', '张三', '13900001001'),
(2, 'WH002', '上海分仓库', '上海市浦东新区陆家嘴金融中心', '李四', '13900001002'),
(3, 'WH003', '广州分仓库', '广州市天河区珠江新城', '王五', '13900001003');

-- 初始化商品分类
INSERT INTO `biz_category` (`id`, `category_name`, `parent_id`, `sort`) VALUES
(1, '电子产品', 0, 1),
(2, '办公用品', 0, 2),
(3, '食品饮料', 0, 3),
(4, '手机', 1, 1),
(5, '电脑', 1, 2),
(6, '配件', 1, 3),
(7, '文具', 2, 1),
(8, '办公设备', 2, 2);

-- 初始化供应商
INSERT INTO `biz_supplier` (`id`, `supplier_code`, `supplier_name`, `contact`, `phone`, `email`, `address`, `bank_name`, `bank_account`) VALUES
(1, 'SUP001', '华为技术有限公司', '张经理', '13800001001', 'zhang@huawei.com', '深圳市龙岗区坂田华为基地', '中国工商银行', '6222021234567890123'),
(2, 'SUP002', '联想集团', '李经理', '13800001002', 'li@lenovo.com', '北京市海淀区联想总部', '中国建设银行', '6227001234567890456'),
(3, 'SUP003', '小米科技有限公司', '王经理', '13800001003', 'wang@xiaomi.com', '北京市海淀区清河小米科技园', '招商银行', '6225881234567890789'),
(4, 'SUP004', '得力集团有限公司', '赵经理', '13800001004', 'zhao@deli.com', '浙江省宁波市北仑区', '中国农业银行', '6228481234567890012'),
(5, 'SUP005', '晨光文具股份有限公司', '刘经理', '13800001005', 'liu@mg.com', '上海市奉贤区晨光工业园', '中国银行', '6217001234567890345');

-- 初始化客户
INSERT INTO `biz_customer` (`id`, `customer_code`, `customer_name`, `contact`, `phone`, `email`, `address`, `credit_limit`) VALUES
(1, 'CUS001', '阿里巴巴集团', '马总', '13900002001', 'ma@alibaba.com', '杭州市余杭区阿里巴巴西溪园区', 1000000.00),
(2, 'CUS002', '腾讯科技有限公司', '刘总', '13900002002', 'liu@tencent.com', '深圳市南山区腾讯大厦', 800000.00),
(3, 'CUS003', '字节跳动有限公司', '张总', '13900002003', 'zhang@bytedance.com', '北京市海淀区字节跳动总部', 600000.00),
(4, 'CUS004', '京东集团', '陈总', '13900002004', 'chen@jd.com', '北京市亦庄经济开发区京东总部', 500000.00),
(5, 'CUS005', '美团点评', '王总', '13900002005', 'wang@meituan.com', '北京市朝阳区望京美团总部', 400000.00),
(6, 'CUS006', '网易公司', '丁总', '13900002006', 'ding@netease.com', '杭州市滨江区网易大厦', 300000.00);

-- 初始化商品
INSERT INTO `biz_goods` (`id`, `goods_code`, `goods_name`, `category_id`, `unit`, `spec`, `purchase_price`, `sale_price`, `safety_stock`, `max_stock`) VALUES
(1, 'GOODS001', 'iPhone 15 Pro Max', 4, '台', '256GB 原色钛金属', 8500.00, 9999.00, 50, 500),
(2, 'GOODS002', 'iPhone 15 Pro', 4, '台', '128GB 蓝色钛金属', 7200.00, 8499.00, 50, 500),
(3, 'GOODS003', '华为 Mate 60 Pro', 4, '台', '512GB 雅丹黑', 6500.00, 7999.00, 80, 600),
(4, 'GOODS004', '小米14 Ultra', 4, '台', '512GB 黑色', 5200.00, 6499.00, 100, 800),
(5, 'GOODS005', 'MacBook Pro 14', 5, '台', 'M3 Pro 18GB/512GB', 14500.00, 16999.00, 30, 200),
(6, 'GOODS006', 'ThinkPad X1 Carbon', 5, '台', 'i7-1365U 16GB/512GB', 9800.00, 11999.00, 40, 300),
(7, 'GOODS007', '联想小新Pro 16', 5, '台', 'R7-7840HS 16GB/512GB', 4500.00, 5499.00, 60, 400),
(8, 'GOODS008', 'AirPods Pro 2', 6, '副', '第二代 USB-C', 1500.00, 1899.00, 100, 1000),
(9, 'GOODS009', 'Apple Watch S9', 6, '块', '45mm GPS版', 2800.00, 3499.00, 50, 500),
(10, 'GOODS010', '得力中性笔', 7, '盒', '0.5mm 黑色 12支/盒', 8.00, 15.00, 500, 5000),
(11, 'GOODS011', '晨光A4复印纸', 7, '包', '70g 500张/包', 18.00, 28.00, 300, 3000),
(12, 'GOODS012', '得力订书机', 7, '个', '12号标准型', 12.00, 22.00, 200, 2000),
(13, 'GOODS013', '惠普激光打印机', 8, '台', 'HP LaserJet M1136', 1200.00, 1599.00, 20, 100),
(14, 'GOODS014', '爱普生投影仪', 8, '台', 'CB-FH52 4000流明', 4500.00, 5699.00, 10, 50),
(15, 'GOODS015', '罗技无线鼠标', 6, '个', 'MX Master 3S', 650.00, 849.00, 80, 600);

-- 初始化库存
INSERT INTO `biz_stock` (`goods_id`, `warehouse_id`, `quantity`) VALUES
(1, 1, 120), (1, 2, 80), (1, 3, 50),
(2, 1, 150), (2, 2, 100),
(3, 1, 200), (3, 2, 150), (3, 3, 100),
(4, 1, 300), (4, 2, 200),
(5, 1, 50), (5, 2, 30),
(6, 1, 80), (6, 2, 60),
(7, 1, 120), (7, 2, 80), (7, 3, 60),
(8, 1, 500), (8, 2, 300), (8, 3, 200),
(9, 1, 150), (9, 2, 100),
(10, 1, 2000), (10, 2, 1500), (10, 3, 1000),
(11, 1, 1500), (11, 2, 1000), (11, 3, 800),
(12, 1, 800), (12, 2, 600),
(13, 1, 40), (13, 2, 30),
(14, 1, 20), (14, 2, 15),
(15, 1, 200), (15, 2, 150), (15, 3, 100);

-- 初始化采购单
INSERT INTO `biz_purchase_order` (`id`, `order_no`, `supplier_id`, `warehouse_id`, `applicant_id`, `total_amount`, `status`, `remark`, `apply_time`, `approve_time`, `approver_id`, `inbound_time`) VALUES
(1, 'PO202601010001', 1, 1, 2, 850000.00, 3, '年初备货', '2026-01-01 09:00:00', '2026-01-01 10:00:00', 1, '2026-01-02 14:00:00'),
(2, 'PO202601050002', 3, 1, 2, 520000.00, 3, '小米新品采购', '2026-01-05 09:30:00', '2026-01-05 11:00:00', 1, '2026-01-06 15:00:00'),
(3, 'PO202601100003', 4, 2, 2, 15000.00, 3, '办公用品补货', '2026-01-10 10:00:00', '2026-01-10 14:00:00', 1, '2026-01-11 09:00:00'),
(4, 'PO202601150004', 2, 1, 2, 490000.00, 1, '联想电脑采购', '2026-01-15 09:00:00', '2026-01-15 16:00:00', 1, NULL),
(5, 'PO202601200005', 1, 2, 2, 720000.00, 0, '华为手机补货申请', '2026-01-20 11:00:00', NULL, NULL, NULL);

-- 初始化采购单明细
INSERT INTO `biz_purchase_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(1, 1, 100, 8500.00, 850000.00),
(2, 4, 100, 5200.00, 520000.00),
(3, 10, 500, 8.00, 4000.00),
(3, 11, 300, 18.00, 5400.00),
(3, 12, 200, 12.00, 2400.00),
(3, 15, 5, 650.00, 3250.00),
(4, 6, 50, 9800.00, 490000.00),
(5, 3, 100, 6500.00, 650000.00),
(5, 8, 100, 700.00, 70000.00);

-- 初始化销售单
INSERT INTO `biz_sale_order` (`id`, `order_no`, `customer_id`, `warehouse_id`, `salesman_id`, `total_amount`, `received_amount`, `status`, `remark`, `create_time`, `approve_time`, `approver_id`, `ship_time`, `receive_time`) VALUES
(1, 'SO202601020001', 1, 1, 3, 199980.00, 199980.00, 3, '阿里巴巴采购', '2026-01-02 10:00:00', '2026-01-02 11:00:00', 1, '2026-01-03 09:00:00', '2026-01-05 14:00:00'),
(2, 'SO202601050002', 2, 1, 3, 169980.00, 169980.00, 3, '腾讯采购', '2026-01-05 14:00:00', '2026-01-05 15:00:00', 1, '2026-01-06 10:00:00', '2026-01-08 16:00:00'),
(3, 'SO202601080003', 3, 2, 3, 129960.00, 129960.00, 3, '字节跳动采购', '2026-01-08 09:30:00', '2026-01-08 10:30:00', 1, '2026-01-09 11:00:00', '2026-01-10 15:00:00'),
(4, 'SO202601120004', 4, 1, 3, 54990.00, 0.00, 2, '京东采购', '2026-01-12 11:00:00', '2026-01-12 14:00:00', 1, '2026-01-13 09:00:00', NULL),
(5, 'SO202601180005', 5, 2, 3, 84990.00, 0.00, 1, '美团采购', '2026-01-18 10:00:00', '2026-01-18 15:00:00', 1, NULL, NULL),
(6, 'SO202601250006', 6, 1, 3, 33990.00, 0.00, 0, '网易采购申请', '2026-01-25 09:00:00', NULL, NULL, NULL, NULL);

-- 初始化销售单明细
INSERT INTO `biz_sale_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(1, 1, 20, 9999.00, 199980.00),
(2, 5, 10, 16999.00, 169990.00),
(3, 3, 15, 7999.00, 119985.00),
(3, 8, 5, 1899.00, 9495.00),
(4, 7, 10, 5499.00, 54990.00),
(5, 1, 5, 9999.00, 49995.00),
(5, 9, 10, 3499.00, 34990.00),
(6, 5, 2, 16999.00, 33998.00);

-- 初始化库存记录
INSERT INTO `biz_stock_record` (`goods_id`, `warehouse_id`, `record_type`, `quantity`, `before_qty`, `after_qty`, `ref_type`, `ref_id`, `ref_no`, `operator_id`, `remark`, `create_time`) VALUES
(1, 1, 'IN', 100, 20, 120, 'PURCHASE', 1, 'PO202601010001', 1, '采购入库', '2026-01-02 14:00:00'),
(4, 1, 'IN', 100, 200, 300, 'PURCHASE', 2, 'PO202601050002', 1, '采购入库', '2026-01-06 15:00:00'),
(1, 1, 'OUT', 20, 120, 100, 'SALE', 1, 'SO202601020001', 1, '销售出库', '2026-01-03 09:00:00'),
(5, 1, 'OUT', 10, 60, 50, 'SALE', 2, 'SO202601050002', 1, '销售出库', '2026-01-06 10:00:00'),
(3, 2, 'OUT', 15, 165, 150, 'SALE', 3, 'SO202601080003', 1, '销售出库', '2026-01-09 11:00:00');

-- 初始化库存调拨记录
INSERT INTO `biz_stock_transfer` (`transfer_no`, `from_warehouse_id`, `to_warehouse_id`, `goods_id`, `quantity`, `status`, `operator_id`, `remark`, `create_time`) VALUES
('TR202601080001', 1, 2, 1, 30, 1, 1, '北京调往上海', '2026-01-08 10:00:00'),
('TR202601150002', 1, 3, 3, 50, 1, 1, '北京调往广州', '2026-01-15 14:00:00'),
('TR202601200003', 2, 3, 8, 100, 1, 1, '上海调往广州', '2026-01-20 09:00:00');

-- 初始化库存调整记录
INSERT INTO `biz_stock_adjust` (`adjust_no`, `warehouse_id`, `goods_id`, `adjust_type`, `quantity`, `reason`, `operator_id`, `create_time`) VALUES
('ADJ202601100001', 1, 10, 'LOSS', 50, '包装破损报废', 1, '2026-01-10 11:00:00'),
('ADJ202601180002', 2, 11, 'OVERFLOW', 20, '盘点发现多余', 1, '2026-01-18 15:00:00');

-- 添加今日(2026-02-01)测试数据
INSERT INTO `biz_purchase_order` (`id`, `order_no`, `supplier_id`, `warehouse_id`, `applicant_id`, `total_amount`, `status`, `remark`, `apply_time`, `approve_time`, `approver_id`, `inbound_time`) VALUES
(6, 'PO202602010001', 1, 1, 2, 425000.00, 1, '华为手机补货', '2026-02-01 09:00:00', '2026-02-01 10:00:00', 1, NULL),
(7, 'PO202602010002', 3, 2, 2, 260000.00, 0, '小米手机采购申请', '2026-02-01 11:00:00', NULL, NULL, NULL);

INSERT INTO `biz_purchase_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(6, 1, 50, 8500.00, 425000.00),
(7, 4, 50, 5200.00, 260000.00);

INSERT INTO `biz_sale_order` (`id`, `order_no`, `customer_id`, `warehouse_id`, `salesman_id`, `total_amount`, `received_amount`, `status`, `remark`, `create_time`, `approve_time`, `approver_id`, `ship_time`, `receive_time`) VALUES
(7, 'SO202602010001', 1, 1, 3, 99990.00, 99990.00, 3, '阿里巴巴追加采购', '2026-02-01 09:30:00', '2026-02-01 10:00:00', 1, '2026-02-01 14:00:00', NULL),
(8, 'SO202602010002', 2, 1, 3, 84995.00, 0.00, 1, '腾讯新订单', '2026-02-01 11:00:00', '2026-02-01 12:00:00', 1, NULL, NULL);

INSERT INTO `biz_sale_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(7, 1, 10, 9999.00, 99990.00),
(8, 5, 5, 16999.00, 84995.00);

-- =====================================================
-- 扩展测试数据 - 供测试人员使用
-- =====================================================

-- 更多测试用户 (密码都是: admin123)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `dept_id`, `status`) VALUES
(4, 'warehouse', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '仓库管理员', '13800138003', 'warehouse@wms.com', 4, 1),
(5, 'finance', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '财务人员', '13800138004', 'finance@wms.com', 5, 1),
(6, 'zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', '13800138005', 'zhangsan@wms.com', 2, 1),
(7, 'lisi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李四', '13800138006', 'lisi@wms.com', 3, 1),
(8, 'wangwu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王五', '13800138007', 'wangwu@wms.com', 4, 1),
(9, 'zhaoliu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵六', '13800138008', 'zhaoliu@wms.com', 3, 1),
(10, 'test', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', '13800138009', 'test@wms.com', 1, 1);

-- 用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES 
(4, 4), (5, 3), (6, 2), (7, 3), (8, 4), (9, 3), (10, 1);

-- 更多部门
INSERT INTO `sys_dept` (`id`, `dept_name`, `parent_id`, `sort`, `leader`, `phone`) VALUES
(6, '华北区', 3, 1, '陈经理', '13900003001'),
(7, '华东区', 3, 2, '林经理', '13900003002'),
(8, '华南区', 3, 3, '黄经理', '13900003003'),
(9, '技术部', 1, 5, '周经理', '13900003004'),
(10, '人事部', 1, 6, '吴经理', '13900003005');

-- 更多供应商
INSERT INTO `biz_supplier` (`id`, `supplier_code`, `supplier_name`, `contact`, `phone`, `email`, `address`, `bank_name`, `bank_account`, `remark`) VALUES
(6, 'SUP006', 'OPPO广东移动通信', '孙经理', '13800001006', 'sun@oppo.com', '广东省东莞市长安镇', '中国工商银行', '6222021234567890678', '手机供应商'),
(7, 'SUP007', 'vivo移动通信', '周经理', '13800001007', 'zhou@vivo.com', '广东省东莞市长安镇', '中国建设银行', '6227001234567890901', '手机供应商'),
(8, 'SUP008', '戴尔科技集团', '吴经理', '13800001008', 'wu@dell.com', '上海市浦东新区', '花旗银行', '6225881234567891234', '电脑供应商'),
(9, 'SUP009', '惠普中国', '郑经理', '13800001009', 'zheng@hp.com', '北京市朝阳区', '汇丰银行', '6228481234567891567', '打印设备供应商'),
(10, 'SUP010', '三星电子', '冯经理', '13800001010', 'feng@samsung.com', '上海市浦东新区', '渣打银行', '6217001234567891890', '电子产品供应商');

-- 更多客户
INSERT INTO `biz_customer` (`id`, `customer_code`, `customer_name`, `contact`, `phone`, `email`, `address`, `credit_limit`, `remark`) VALUES
(7, 'CUS007', '百度公司', '李总', '13900002007', 'li@baidu.com', '北京市海淀区百度大厦', 500000.00, '互联网公司'),
(8, 'CUS008', '滴滴出行', '程总', '13900002008', 'cheng@didi.com', '北京市海淀区滴滴大厦', 350000.00, '出行平台'),
(9, 'CUS009', '拼多多', '黄总', '13900002009', 'huang@pinduoduo.com', '上海市长宁区', 450000.00, '电商平台'),
(10, 'CUS010', '快手科技', '宿总', '13900002010', 'su@kuaishou.com', '北京市海淀区', 280000.00, '短视频平台'),
(11, 'CUS011', '小红书', '毛总', '13900002011', 'mao@xiaohongshu.com', '上海市黄浦区', 220000.00, '社交电商'),
(12, 'CUS012', '哔哩哔哩', '陈总', '13900002012', 'chen@bilibili.com', '上海市杨浦区', 180000.00, '视频平台');

-- 更多商品
INSERT INTO `biz_goods` (`id`, `goods_code`, `goods_name`, `category_id`, `unit`, `spec`, `purchase_price`, `sale_price`, `safety_stock`, `max_stock`, `remark`) VALUES
(16, 'GOODS016', 'OPPO Find X7 Ultra', 4, '台', '512GB 海阔天空', 4800.00, 5999.00, 60, 500, '旗舰手机'),
(17, 'GOODS017', 'vivo X100 Pro', 4, '台', '512GB 白月光', 4500.00, 5499.00, 60, 500, '旗舰手机'),
(18, 'GOODS018', '三星 Galaxy S24 Ultra', 4, '台', '512GB 钛灰', 8200.00, 9699.00, 40, 300, '旗舰手机'),
(19, 'GOODS019', 'Dell XPS 15', 5, '台', 'i7-13700H 32GB/1TB', 12000.00, 14499.00, 25, 150, '高端笔记本'),
(20, 'GOODS020', 'Surface Pro 9', 5, '台', 'i7 16GB/256GB', 9500.00, 11299.00, 30, 200, '二合一平板'),
(21, 'GOODS021', 'iPad Pro 12.9', 5, '台', 'M2芯片 256GB', 8500.00, 9999.00, 40, 300, '平板电脑'),
(22, 'GOODS022', 'Sony WH-1000XM5', 6, '副', '黑色 降噪耳机', 2200.00, 2699.00, 50, 400, '降噪耳机'),
(23, 'GOODS023', 'Bose QC45', 6, '副', '黑色 降噪耳机', 1800.00, 2299.00, 50, 400, '降噪耳机'),
(24, 'GOODS024', '机械键盘', 6, '个', 'Cherry红轴 87键', 450.00, 599.00, 100, 800, '外设'),
(25, 'GOODS025', '显示器支架', 6, '个', '双臂 17-32寸', 280.00, 399.00, 80, 600, '外设配件'),
(26, 'GOODS026', '得力文件夹', 7, '个', 'A4 双夹', 5.00, 12.00, 500, 5000, '办公文具'),
(27, 'GOODS027', '白板笔', 7, '盒', '12支/盒 黑色', 15.00, 28.00, 300, 3000, '办公文具'),
(28, 'GOODS028', '碎纸机', 8, '台', '得力 9939', 800.00, 1099.00, 15, 100, '办公设备'),
(29, 'GOODS029', '扫描仪', 8, '台', '爱普生 DS-530', 2500.00, 3199.00, 10, 80, '办公设备'),
(30, 'GOODS030', '会议摄像头', 8, '台', '罗技 C930e', 650.00, 899.00, 20, 150, '会议设备');

-- 更多库存数据
INSERT INTO `biz_stock` (`goods_id`, `warehouse_id`, `quantity`) VALUES
(16, 1, 150), (16, 2, 100), (16, 3, 80),
(17, 1, 180), (17, 2, 120),
(18, 1, 80), (18, 2, 60),
(19, 1, 40), (19, 2, 25),
(20, 1, 60), (20, 2, 40),
(21, 1, 100), (21, 2, 70), (21, 3, 50),
(22, 1, 120), (22, 2, 80),
(23, 1, 100), (23, 2, 70),
(24, 1, 300), (24, 2, 200), (24, 3, 150),
(25, 1, 200), (25, 2, 150),
(26, 1, 2500), (26, 2, 1800), (26, 3, 1200),
(27, 1, 1500), (27, 2, 1000),
(28, 1, 30), (28, 2, 20),
(29, 1, 25), (29, 2, 15),
(30, 1, 50), (30, 2, 35), (30, 3, 25);

-- 更多采购单
INSERT INTO `biz_purchase_order` (`id`, `order_no`, `supplier_id`, `warehouse_id`, `applicant_id`, `total_amount`, `status`, `remark`, `apply_time`, `approve_time`, `approver_id`, `inbound_time`) VALUES
(8, 'PO202601220001', 6, 1, 6, 480000.00, 3, 'OPPO手机采购', '2026-01-22 09:00:00', '2026-01-22 11:00:00', 1, '2026-01-23 14:00:00'),
(9, 'PO202601250002', 7, 2, 6, 450000.00, 3, 'vivo手机采购', '2026-01-25 10:00:00', '2026-01-25 14:00:00', 1, '2026-01-26 10:00:00'),
(10, 'PO202601280003', 8, 1, 6, 360000.00, 1, 'Dell电脑采购', '2026-01-28 09:30:00', '2026-01-28 15:00:00', 1, NULL),
(11, 'PO202601300004', 9, 2, 2, 75000.00, 0, '惠普打印机采购申请', '2026-01-30 11:00:00', NULL, NULL, NULL),
(12, 'PO202602010003', 10, 1, 6, 820000.00, 0, '三星手机采购申请', '2026-02-01 14:00:00', NULL, NULL, NULL);

INSERT INTO `biz_purchase_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(8, 16, 100, 4800.00, 480000.00),
(9, 17, 100, 4500.00, 450000.00),
(10, 19, 30, 12000.00, 360000.00),
(11, 13, 50, 1200.00, 60000.00),
(11, 29, 5, 2500.00, 12500.00),
(11, 30, 3, 650.00, 1950.00),
(12, 18, 100, 8200.00, 820000.00);

-- 更多销售单
INSERT INTO `biz_sale_order` (`id`, `order_no`, `customer_id`, `warehouse_id`, `salesman_id`, `total_amount`, `received_amount`, `status`, `remark`, `create_time`, `approve_time`, `approver_id`, `ship_time`, `receive_time`) VALUES
(9, 'SO202601220001', 7, 1, 7, 119980.00, 119980.00, 3, '百度采购', '2026-01-22 10:00:00', '2026-01-22 11:00:00', 1, '2026-01-23 09:00:00', '2026-01-25 14:00:00'),
(10, 'SO202601240002', 8, 2, 7, 54990.00, 54990.00, 3, '滴滴采购', '2026-01-24 14:00:00', '2026-01-24 15:00:00', 1, '2026-01-25 10:00:00', '2026-01-27 16:00:00'),
(11, 'SO202601260003', 9, 1, 9, 89985.00, 89985.00, 3, '拼多多采购', '2026-01-26 09:30:00', '2026-01-26 10:30:00', 1, '2026-01-27 11:00:00', '2026-01-29 15:00:00'),
(12, 'SO202601280004', 10, 2, 9, 64990.00, 0.00, 2, '快手采购', '2026-01-28 11:00:00', '2026-01-28 14:00:00', 1, '2026-01-29 09:00:00', NULL),
(13, 'SO202601300005', 11, 1, 7, 49995.00, 0.00, 1, '小红书采购', '2026-01-30 10:00:00', '2026-01-30 15:00:00', 1, NULL, NULL),
(14, 'SO202602010003', 12, 2, 9, 26970.00, 0.00, 0, '哔哩哔哩采购申请', '2026-02-01 15:00:00', NULL, NULL, NULL, NULL);

INSERT INTO `biz_sale_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(9, 16, 20, 5999.00, 119980.00),
(10, 17, 10, 5499.00, 54990.00),
(11, 3, 10, 7999.00, 79990.00),
(11, 8, 5, 1899.00, 9495.00),
(12, 4, 10, 6499.00, 64990.00),
(13, 1, 5, 9999.00, 49995.00),
(14, 22, 10, 2699.00, 26990.00);

-- 更多库存记录
INSERT INTO `biz_stock_record` (`goods_id`, `warehouse_id`, `record_type`, `quantity`, `before_qty`, `after_qty`, `ref_type`, `ref_id`, `ref_no`, `operator_id`, `remark`, `create_time`) VALUES
(16, 1, 'IN', 100, 50, 150, 'PURCHASE', 8, 'PO202601220001', 1, '采购入库', '2026-01-23 14:00:00'),
(17, 2, 'IN', 100, 20, 120, 'PURCHASE', 9, 'PO202601250002', 1, '采购入库', '2026-01-26 10:00:00'),
(16, 1, 'OUT', 20, 150, 130, 'SALE', 9, 'SO202601220001', 1, '销售出库', '2026-01-23 09:00:00'),
(17, 2, 'OUT', 10, 120, 110, 'SALE', 10, 'SO202601240002', 1, '销售出库', '2026-01-25 10:00:00'),
(3, 1, 'OUT', 10, 210, 200, 'SALE', 11, 'SO202601260003', 1, '销售出库', '2026-01-27 11:00:00'),
(8, 1, 'OUT', 5, 505, 500, 'SALE', 11, 'SO202601260003', 1, '销售出库', '2026-01-27 11:00:00'),
(4, 2, 'OUT', 10, 210, 200, 'SALE', 12, 'SO202601280004', 1, '销售出库', '2026-01-29 09:00:00');

-- 更多库存调拨
INSERT INTO `biz_stock_transfer` (`transfer_no`, `from_warehouse_id`, `to_warehouse_id`, `goods_id`, `quantity`, `status`, `operator_id`, `remark`, `create_time`) VALUES
('TR202601250001', 1, 3, 16, 30, 1, 4, '北京调往广州', '2026-01-25 10:00:00'),
('TR202601280002', 2, 1, 17, 20, 1, 4, '上海调往北京', '2026-01-28 14:00:00'),
('TR202602010001', 1, 2, 21, 20, 1, 8, '北京调往上海', '2026-02-01 09:00:00');

-- 更多库存调整
INSERT INTO `biz_stock_adjust` (`adjust_no`, `warehouse_id`, `goods_id`, `adjust_type`, `quantity`, `reason`, `operator_id`, `create_time`) VALUES
('ADJ202601250001', 1, 26, 'LOSS', 100, '仓库进水损坏', 4, '2026-01-25 11:00:00'),
('ADJ202601280002', 2, 27, 'OVERFLOW', 50, '盘点发现多余', 4, '2026-01-28 15:00:00'),
('ADJ202602010001', 1, 24, 'LOSS', 20, '产品质量问题退回', 8, '2026-02-01 10:00:00');

-- 库存盘点单
INSERT INTO `biz_stock_check` (`id`, `check_no`, `warehouse_id`, `status`, `operator_id`, `remark`, `create_time`, `finish_time`) VALUES
(1, 'CK202601150001', 1, 1, 4, '月中盘点', '2026-01-15 09:00:00', '2026-01-15 18:00:00'),
(2, 'CK202601310001', 2, 1, 4, '月末盘点', '2026-01-31 09:00:00', '2026-01-31 17:00:00'),
(3, 'CK202602010001', 1, 0, 8, '月初盘点进行中', '2026-02-01 09:00:00', NULL);

-- 库存盘点明细
INSERT INTO `biz_stock_check_item` (`check_id`, `goods_id`, `system_qty`, `actual_qty`, `diff_qty`) VALUES
(1, 1, 120, 118, -2),
(1, 3, 200, 200, 0),
(1, 5, 50, 50, 0),
(1, 10, 2000, 1995, -5),
(2, 2, 100, 100, 0),
(2, 4, 200, 202, 2),
(2, 8, 300, 298, -2),
(3, 1, 100, 0, 0),
(3, 3, 200, 0, 0),
(3, 16, 130, 0, 0);

-- 操作日志
INSERT INTO `sys_log` (`user_id`, `username`, `module`, `operation`, `method`, `params`, `ip`, `exec_time`, `create_time`) VALUES
(1, 'admin', '系统管理', '用户登录', 'AuthController.login', '{"username":"admin"}', '192.168.1.100', 125, '2026-02-01 08:30:00'),
(2, 'purchase', '采购管理', '新增采购单', 'PurchaseController.create', '{"orderNo":"PO202602010001"}', '192.168.1.101', 89, '2026-02-01 09:00:00'),
(3, 'sales', '销售管理', '新增销售单', 'SaleController.create', '{"orderNo":"SO202602010001"}', '192.168.1.102', 76, '2026-02-01 09:30:00'),
(1, 'admin', '采购管理', '审批采购单', 'PurchaseController.approve', '{"id":6,"status":1}', '192.168.1.100', 45, '2026-02-01 10:00:00'),
(4, 'warehouse', '库存管理', '库存调拨', 'StockController.transfer', '{"transferNo":"TR202602010001"}', '192.168.1.103', 112, '2026-02-01 09:00:00'),
(8, 'wangwu', '库存管理', '库存调整', 'StockController.adjust', '{"adjustNo":"ADJ202602010001"}', '192.168.1.104', 67, '2026-02-01 10:00:00'),
(7, 'lisi', '销售管理', '新增销售单', 'SaleController.create', '{"orderNo":"SO202602010003"}', '192.168.1.105', 82, '2026-02-01 15:00:00'),
(6, 'zhangsan', '采购管理', '新增采购单', 'PurchaseController.create', '{"orderNo":"PO202602010003"}', '192.168.1.106', 95, '2026-02-01 14:00:00');

-- =====================================================
-- Dashboard 展示数据 - 近7天销售/采购数据
-- =====================================================

-- 近7天已完成的销售单 (status=3 已收款)
INSERT INTO `biz_sale_order` (`id`, `order_no`, `customer_id`, `warehouse_id`, `salesman_id`, `total_amount`, `received_amount`, `status`, `remark`, `create_time`, `approve_time`, `approver_id`, `ship_time`, `receive_time`) VALUES
-- 1月26日
(15, 'SO202601260001', 1, 1, 3, 159984.00, 159984.00, 3, '阿里巴巴手机采购', '2026-01-26 09:00:00', '2026-01-26 10:00:00', 1, '2026-01-26 14:00:00', '2026-01-26 17:00:00'),
(16, 'SO202601260002', 2, 2, 7, 84995.00, 84995.00, 3, '腾讯电脑采购', '2026-01-26 11:00:00', '2026-01-26 12:00:00', 1, '2026-01-26 15:00:00', '2026-01-26 18:00:00'),
-- 1月27日
(17, 'SO202601270001', 3, 1, 3, 119985.00, 119985.00, 3, '字节跳动采购', '2026-01-27 09:30:00', '2026-01-27 10:30:00', 1, '2026-01-27 14:00:00', '2026-01-27 17:00:00'),
(18, 'SO202601270002', 4, 2, 9, 64990.00, 64990.00, 3, '京东采购', '2026-01-27 14:00:00', '2026-01-27 15:00:00', 1, '2026-01-27 17:00:00', '2026-01-27 19:00:00'),
-- 1月28日
(19, 'SO202601280001', 5, 1, 7, 169990.00, 169990.00, 3, '美团采购', '2026-01-28 10:00:00', '2026-01-28 11:00:00', 1, '2026-01-28 15:00:00', '2026-01-28 18:00:00'),
(20, 'SO202601280002', 6, 2, 3, 53970.00, 53970.00, 3, '网易采购', '2026-01-28 13:00:00', '2026-01-28 14:00:00', 1, '2026-01-28 16:00:00', '2026-01-28 19:00:00'),
-- 1月29日
(21, 'SO202601290001', 7, 1, 9, 95984.00, 95984.00, 3, '百度采购', '2026-01-29 09:00:00', '2026-01-29 10:00:00', 1, '2026-01-29 14:00:00', '2026-01-29 17:00:00'),
(22, 'SO202601290002', 8, 2, 7, 109980.00, 109980.00, 3, '滴滴采购', '2026-01-29 11:00:00', '2026-01-29 12:00:00', 1, '2026-01-29 15:00:00', '2026-01-29 18:00:00'),
-- 1月30日
(23, 'SO202601300001', 9, 1, 3, 143985.00, 143985.00, 3, '拼多多采购', '2026-01-30 09:30:00', '2026-01-30 10:30:00', 1, '2026-01-30 14:00:00', '2026-01-30 17:00:00'),
(24, 'SO202601300002', 10, 2, 9, 77985.00, 77985.00, 3, '快手采购', '2026-01-30 14:00:00', '2026-01-30 15:00:00', 1, '2026-01-30 17:00:00', '2026-01-30 19:00:00'),
-- 1月31日
(25, 'SO202601310001', 11, 1, 7, 199980.00, 199980.00, 3, '小红书采购', '2026-01-31 09:00:00', '2026-01-31 10:00:00', 1, '2026-01-31 14:00:00', '2026-01-31 17:00:00'),
(26, 'SO202601310002', 12, 2, 3, 67490.00, 67490.00, 3, '哔哩哔哩采购', '2026-01-31 11:00:00', '2026-01-31 12:00:00', 1, '2026-01-31 15:00:00', '2026-01-31 18:00:00'),
-- 2月1日(今日) - 已完成的销售
(27, 'SO202602010004', 1, 1, 3, 179982.00, 179982.00, 3, '阿里巴巴今日采购', '2026-02-01 09:00:00', '2026-02-01 09:30:00', 1, '2026-02-01 11:00:00', '2026-02-01 14:00:00'),
(28, 'SO202602010005', 2, 2, 7, 112495.00, 112495.00, 3, '腾讯今日采购', '2026-02-01 10:00:00', '2026-02-01 10:30:00', 1, '2026-02-01 13:00:00', '2026-02-01 15:00:00'),
(29, 'SO202602010006', 3, 1, 9, 95984.00, 95984.00, 3, '字节跳动今日采购', '2026-02-01 11:00:00', '2026-02-01 11:30:00', 1, '2026-02-01 14:00:00', '2026-02-01 16:00:00');

-- 近7天销售单明细
INSERT INTO `biz_sale_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
-- 1月26日
(15, 3, 20, 7999.00, 159980.00),
(16, 5, 5, 16999.00, 84995.00),
-- 1月27日
(17, 4, 15, 6499.00, 97485.00),
(17, 8, 10, 1899.00, 18990.00),
(18, 17, 10, 5499.00, 54990.00),
(18, 15, 10, 849.00, 8490.00),
-- 1月28日
(19, 5, 10, 16999.00, 169990.00),
(20, 22, 20, 2699.00, 53980.00),
-- 1月29日
(21, 16, 16, 5999.00, 95984.00),
(22, 17, 20, 5499.00, 109980.00),
-- 1月30日
(23, 1, 10, 9999.00, 99990.00),
(23, 3, 5, 7999.00, 39995.00),
(24, 4, 12, 6499.00, 77988.00),
-- 1月31日
(25, 1, 20, 9999.00, 199980.00),
(26, 18, 5, 9699.00, 48495.00),
(26, 8, 10, 1899.00, 18990.00),
-- 2月1日(今日)
(27, 1, 18, 9999.00, 179982.00),
(28, 5, 5, 16999.00, 84995.00),
(28, 9, 8, 3499.00, 27992.00),
(29, 16, 16, 5999.00, 95984.00);

-- 近7天采购单(已入库 status=3)
INSERT INTO `biz_purchase_order` (`id`, `order_no`, `supplier_id`, `warehouse_id`, `applicant_id`, `total_amount`, `status`, `remark`, `apply_time`, `approve_time`, `approver_id`, `inbound_time`) VALUES
(13, 'PO202601260001', 1, 1, 2, 650000.00, 3, '华为手机采购', '2026-01-26 09:00:00', '2026-01-26 10:00:00', 1, '2026-01-26 15:00:00'),
(14, 'PO202601270001', 3, 2, 6, 520000.00, 3, '小米手机采购', '2026-01-27 09:00:00', '2026-01-27 10:00:00', 1, '2026-01-27 15:00:00'),
(15, 'PO202601280001', 2, 1, 2, 490000.00, 3, '联想电脑采购', '2026-01-28 09:00:00', '2026-01-28 10:00:00', 1, '2026-01-28 15:00:00'),
(16, 'PO202601290001', 4, 2, 6, 25000.00, 3, '办公用品采购', '2026-01-29 09:00:00', '2026-01-29 10:00:00', 1, '2026-01-29 15:00:00'),
(17, 'PO202601300001', 5, 1, 2, 18000.00, 3, '文具采购', '2026-01-30 09:00:00', '2026-01-30 10:00:00', 1, '2026-01-30 15:00:00'),
(18, 'PO202601310001', 6, 2, 6, 480000.00, 3, 'OPPO手机采购', '2026-01-31 09:00:00', '2026-01-31 10:00:00', 1, '2026-01-31 15:00:00'),
(19, 'PO202602010004', 7, 1, 2, 450000.00, 3, 'vivo手机今日采购', '2026-02-01 08:00:00', '2026-02-01 09:00:00', 1, '2026-02-01 12:00:00');

INSERT INTO `biz_purchase_item` (`order_id`, `goods_id`, `quantity`, `price`, `amount`) VALUES
(13, 3, 100, 6500.00, 650000.00),
(14, 4, 100, 5200.00, 520000.00),
(15, 6, 50, 9800.00, 490000.00),
(16, 10, 1000, 8.00, 8000.00),
(16, 11, 500, 18.00, 9000.00),
(16, 12, 500, 12.00, 6000.00),
(17, 26, 1000, 5.00, 5000.00),
(17, 27, 500, 15.00, 7500.00),
(17, 10, 500, 8.00, 4000.00),
(18, 16, 100, 4800.00, 480000.00),
(19, 17, 100, 4500.00, 450000.00);

-- =====================================================
-- 库存预警数据 - 调整部分商品库存低于安全库存
-- =====================================================
UPDATE `biz_stock` SET `quantity` = 20 WHERE `goods_id` = 5 AND `warehouse_id` = 1;  -- MacBook Pro 安全库存30
UPDATE `biz_stock` SET `quantity` = 15 WHERE `goods_id` = 5 AND `warehouse_id` = 2;  -- MacBook Pro 安全库存30
UPDATE `biz_stock` SET `quantity` = 25 WHERE `goods_id` = 6 AND `warehouse_id` = 1;  -- ThinkPad 安全库存40
UPDATE `biz_stock` SET `quantity` = 30 WHERE `goods_id` = 9 AND `warehouse_id` = 1;  -- Apple Watch 安全库存50
UPDATE `biz_stock` SET `quantity` = 8 WHERE `goods_id` = 14 AND `warehouse_id` = 1;  -- 投影仪 安全库存10
UPDATE `biz_stock` SET `quantity` = 5 WHERE `goods_id` = 14 AND `warehouse_id` = 2;  -- 投影仪 安全库存10
UPDATE `biz_stock` SET `quantity` = 35 WHERE `goods_id` = 18 AND `warehouse_id` = 1; -- 三星手机 安全库存40
UPDATE `biz_stock` SET `quantity` = 20 WHERE `goods_id` = 19 AND `warehouse_id` = 1; -- Dell XPS 安全库存25
UPDATE `biz_stock` SET `quantity` = 25 WHERE `goods_id` = 20 AND `warehouse_id` = 1; -- Surface 安全库存30
UPDATE `biz_stock` SET `quantity` = 35 WHERE `goods_id` = 21 AND `warehouse_id` = 1; -- iPad Pro 安全库存40
UPDATE `biz_stock` SET `quantity` = 40 WHERE `goods_id` = 22 AND `warehouse_id` = 1; -- Sony耳机 安全库存50
UPDATE `biz_stock` SET `quantity` = 40 WHERE `goods_id` = 23 AND `warehouse_id` = 1; -- Bose耳机 安全库存50

-- =====================================================
-- 测试账号汇总 (密码统一为: admin123)
-- =====================================================
-- admin     - 超级管理员 (所有权限)
-- purchase  - 采购员 (采购相关权限)
-- sales     - 销售员 (销售相关权限)
-- warehouse - 仓库管理员 (库存相关权限)
-- finance   - 财务人员 (销售权限)
-- zhangsan  - 张三 (采购员)
-- lisi      - 李四 (销售员)
-- wangwu    - 王五 (仓库管理员)
-- zhaoliu   - 赵六 (销售员)
-- test      - 测试用户 (超级管理员权限)