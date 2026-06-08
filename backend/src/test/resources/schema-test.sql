-- H2 兼容的测试数据库初始化脚本

-- 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  username varchar(50) NOT NULL,
  password varchar(100) NOT NULL,
  real_name varchar(50),
  phone varchar(20),
  email varchar(100),
  dept_id bigint,
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_username ON sys_user(username);

-- 系统角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  role_name varchar(50) NOT NULL,
  role_code varchar(50) NOT NULL,
  remark varchar(200),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  role_id bigint NOT NULL
);

-- 系统菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  menu_name varchar(50) NOT NULL,
  parent_id bigint DEFAULT 0,
  path varchar(200),
  component varchar(200),
  perms varchar(100),
  icon varchar(50),
  type tinyint DEFAULT 1,
  sort int DEFAULT 0,
  visible tinyint DEFAULT 1,
  status tinyint DEFAULT 1,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  role_id bigint NOT NULL,
  menu_id bigint NOT NULL
);

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  dept_name varchar(50) NOT NULL,
  parent_id bigint DEFAULT 0,
  sort int DEFAULT 0,
  leader varchar(50),
  phone varchar(20),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint,
  username varchar(50),
  module varchar(50),
  operation varchar(50),
  method varchar(200),
  params clob,
  ip varchar(50),
  exec_time bigint,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 供应商表
DROP TABLE IF EXISTS biz_supplier;
CREATE TABLE biz_supplier (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  supplier_code varchar(50) NOT NULL,
  supplier_name varchar(100) NOT NULL,
  contact varchar(50),
  phone varchar(20),
  email varchar(100),
  address varchar(200),
  bank_name varchar(100),
  bank_account varchar(50),
  remark varchar(500),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 客户表
DROP TABLE IF EXISTS biz_customer;
CREATE TABLE biz_customer (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  customer_code varchar(50) NOT NULL,
  customer_name varchar(100) NOT NULL,
  contact varchar(50),
  phone varchar(20),
  email varchar(100),
  address varchar(200),
  credit_limit decimal(12,2) DEFAULT 0,
  remark varchar(500),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 商品分类表
DROP TABLE IF EXISTS biz_category;
CREATE TABLE biz_category (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  category_name varchar(50) NOT NULL,
  parent_id bigint DEFAULT 0,
  sort int DEFAULT 0,
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
DROP TABLE IF EXISTS biz_goods;
CREATE TABLE biz_goods (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  goods_code varchar(50) NOT NULL,
  goods_name varchar(100) NOT NULL,
  category_id bigint,
  unit varchar(20),
  spec varchar(100),
  purchase_price decimal(12,2) DEFAULT 0,
  sale_price decimal(12,2) DEFAULT 0,
  safety_stock int DEFAULT 0,
  max_stock int DEFAULT 99999,
  remark varchar(500),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 仓库表
DROP TABLE IF EXISTS biz_warehouse;
CREATE TABLE biz_warehouse (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  warehouse_code varchar(50) NOT NULL,
  warehouse_name varchar(100) NOT NULL,
  address varchar(200),
  manager varchar(50),
  phone varchar(20),
  remark varchar(500),
  status tinyint DEFAULT 1,
  deleted tinyint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 库存表
DROP TABLE IF EXISTS biz_stock;
CREATE TABLE biz_stock (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  goods_id bigint NOT NULL,
  warehouse_id bigint NOT NULL,
  quantity int DEFAULT 0,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 采购单表
DROP TABLE IF EXISTS biz_purchase_order;
CREATE TABLE biz_purchase_order (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  order_no varchar(50) NOT NULL,
  supplier_id bigint NOT NULL,
  warehouse_id bigint,
  applicant_id bigint NOT NULL,
  dept_id bigint,
  total_amount decimal(12,2) DEFAULT 0,
  status tinyint DEFAULT 0,
  remark varchar(500),
  apply_time timestamp DEFAULT CURRENT_TIMESTAMP,
  approve_time timestamp,
  approver_id bigint,
  approve_remark varchar(500),
  inbound_time timestamp,
  deleted tinyint DEFAULT 0
);

-- 采购单明细表
DROP TABLE IF EXISTS biz_purchase_item;
CREATE TABLE biz_purchase_item (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  order_id bigint NOT NULL,
  goods_id bigint NOT NULL,
  quantity int NOT NULL,
  price decimal(12,2) NOT NULL,
  amount decimal(12,2) NOT NULL
);

-- 销售单表
DROP TABLE IF EXISTS biz_sale_order;
CREATE TABLE biz_sale_order (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  order_no varchar(50) NOT NULL,
  customer_id bigint NOT NULL,
  warehouse_id bigint,
  salesman_id bigint NOT NULL,
  dept_id bigint,
  total_amount decimal(12,2) DEFAULT 0,
  received_amount decimal(12,2) DEFAULT 0,
  status tinyint DEFAULT 0,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  quote_time timestamp,
  confirm_time timestamp,
  approve_time timestamp,
  approver_id bigint,
  ship_time timestamp,
  receive_time timestamp,
  deleted tinyint DEFAULT 0
);

-- 销售单明细表
DROP TABLE IF EXISTS biz_sale_item;
CREATE TABLE biz_sale_item (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  order_id bigint NOT NULL,
  goods_id bigint NOT NULL,
  quantity int NOT NULL,
  price decimal(12,2) NOT NULL,
  amount decimal(12,2) NOT NULL,
  cost_price decimal(12,2)
);

-- 库存记录表
DROP TABLE IF EXISTS biz_stock_record;
CREATE TABLE biz_stock_record (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  goods_id bigint NOT NULL,
  warehouse_id bigint NOT NULL,
  record_type varchar(20) NOT NULL,
  quantity int NOT NULL,
  before_qty int DEFAULT 0,
  after_qty int DEFAULT 0,
  ref_type varchar(50),
  ref_id bigint,
  ref_no varchar(50),
  operator_id bigint,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 库存盘点单表
DROP TABLE IF EXISTS biz_stock_check;
CREATE TABLE biz_stock_check (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  check_no varchar(50) NOT NULL,
  warehouse_id bigint NOT NULL,
  status tinyint DEFAULT 0,
  operator_id bigint,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  finish_time timestamp
);

-- 库存盘点明细表
DROP TABLE IF EXISTS biz_stock_check_item;
CREATE TABLE biz_stock_check_item (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  check_id bigint NOT NULL,
  goods_id bigint NOT NULL,
  system_qty int DEFAULT 0,
  actual_qty int DEFAULT 0,
  diff_qty int DEFAULT 0
);

-- 库存调拨表
DROP TABLE IF EXISTS biz_stock_transfer;
CREATE TABLE biz_stock_transfer (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  transfer_no varchar(50) NOT NULL,
  from_warehouse_id bigint NOT NULL,
  to_warehouse_id bigint NOT NULL,
  goods_id bigint NOT NULL,
  quantity int NOT NULL,
  status tinyint DEFAULT 1,
  operator_id bigint,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 库存调整表
DROP TABLE IF EXISTS biz_stock_adjust;
CREATE TABLE biz_stock_adjust (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  adjust_no varchar(50) NOT NULL,
  warehouse_id bigint NOT NULL,
  goods_id bigint NOT NULL,
  adjust_type varchar(20) NOT NULL,
  quantity int NOT NULL,
  reason varchar(500),
  operator_id bigint,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 验证码缓存表
DROP TABLE IF EXISTS sys_captcha;
CREATE TABLE sys_captcha (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  uuid varchar(50) NOT NULL,
  code varchar(10) NOT NULL,
  expire_time timestamp NOT NULL
);

-- 系统备份表
DROP TABLE IF EXISTS sys_backup;
CREATE TABLE sys_backup (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  backup_name varchar(200) NOT NULL,
  file_path varchar(500) NOT NULL,
  file_size bigint DEFAULT 0,
  backup_type varchar(20) DEFAULT 'MANUAL',
  status tinyint DEFAULT 1,
  remark varchar(500),
  operator_id bigint,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 系统通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  title varchar(200) NOT NULL,
  content text,
  type varchar(50) DEFAULT 'SYSTEM',
  user_id bigint,
  status tinyint DEFAULT 0,
  ref_id bigint,
  ref_type varchar(50),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  read_time timestamp
);

-- 收款记录表
DROP TABLE IF EXISTS biz_payment_record;
CREATE TABLE biz_payment_record (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  payment_no varchar(50) NOT NULL,
  order_id bigint NOT NULL,
  order_no varchar(50),
  amount decimal(12,2) NOT NULL,
  payment_method varchar(20) DEFAULT 'BANK',
  remark varchar(500),
  operator_id bigint,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 用户偏好设置表
DROP TABLE IF EXISTS sys_user_preference;
CREATE TABLE sys_user_preference (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  pref_key varchar(100) NOT NULL,
  pref_value text,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO sys_dept (id, dept_name, parent_id, sort, leader) VALUES
(1, '总公司', 0, 1, '管理员'),
(2, '采购部', 1, 1, NULL);

INSERT INTO sys_role (id, role_name, role_code, remark) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限');

INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Zf4iu9S1Rz0fXqjKvJeWa', '系统管理员', '13800138000', 1, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO biz_warehouse (id, warehouse_code, warehouse_name, address, manager) VALUES
(1, 'WH001', '主仓库', '北京市朝阳区', '张三');

INSERT INTO biz_supplier (id, supplier_code, supplier_name, contact, phone) VALUES
(1, 'SUP001', '测试供应商', '联系人', '13800000000');

INSERT INTO biz_customer (id, customer_code, customer_name, contact, phone) VALUES
(1, 'CUS001', '测试客户', '联系人', '13900000000');

INSERT INTO biz_goods (id, goods_code, goods_name, unit, purchase_price, sale_price, safety_stock) VALUES
(1, 'G001', '测试商品', '个', 10.00, 20.00, 10);

INSERT INTO biz_stock (goods_id, warehouse_id, quantity) VALUES (1, 1, 100);
