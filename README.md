# WMS Pro - 智能仓储管理系统

一个现代化的进销存管理系统，基于 Spring Boot 3 + Vue 3 + MySQL 构建。

## 🚀 快速启动

确保已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)，然后执行：

```bash
docker-compose up --build
```

首次启动需要 5-10 分钟，启动完成后访问：**http://localhost**

## 📋 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端管理系统 | http://localhost | Vue3 + Ant Design Vue |
| 后端 API | http://localhost:8080 | Spring Boot 3 |
| MySQL | localhost:3306 | 数据库 |

## 👥 测试账号

| 角色 | 用户名 | 密码 | 权限范围 |
|------|--------|------|----------|
| 超级管理员 | admin | admin123 | 全部权限 |
| 采购员 | purchase | admin123 | 采购管理、供应商查看、商品查看、库存查看 |
| 销售员 | sales | admin123 | 销售管理、客户查看、商品查看 |
| 仓库管理员 | warehouse | admin123 | 库存管理、仓库管理、商品查看 |

## ✨ 功能模块

### 系统管理
- 员工管理：用户注册、信息维护、状态管理
- 角色管理：角色配置、菜单权限分配
- 菜单管理：动态菜单配置
- 部门管理：树形组织架构
- 操作日志：系统操作记录追踪
- 数据备份：关键数据备份与恢复

### 基础数据
- 供应商管理：供应商信息维护
- 客户管理：客户信息维护
- 商品管理：商品分类、商品信息
- 仓库管理：多仓库配置

### 采购管理
- 采购申请：创建采购申请单
- 采购单管理：采购单查询与跟踪
- 采购审批：审批通过/拒绝
- 采购入库：到货入库操作

### 销售管理
- 销售报价：创建销售报价单
- 销售单管理：销售单查询与跟踪
- 销售审核：审核确认
- 发货出库：发货操作
- 财务收款：收款记录

### 库存管理
- 库存查询：实时库存查看
- 库存盘点：盘点单管理
- 库存调拨：仓库间调拨
- 报损报溢：库存调整
- 库存预警：安全库存监控

### 统计报表
- 销售排行：商品销售排名
- 采购趋势：采购数据分析
- 库存预警报表：预警商品统计
- 毛利分析：利润分析报表（基于发货时记录的成本价计算）

## 🔐 权限设计

系统采用 RBAC（基于角色的访问控制）模型：

- **菜单权限**：控制用户可见的菜单项
- **操作权限**：控制用户可执行的操作（增删改查）
- **数据权限**：基于部门的数据隔离

权限粒度示例：
- `biz:purchase:apply` - 采购申请
- `biz:purchase:approve` - 采购审批
- `biz:customer:list` - 客户查看
- `biz:customer:edit` - 客户编辑

## 📊 业务流程设计

### 采购流程
采用单据生命周期模式，一张采购单贯穿全流程：
- **状态0（待审批）**：采购申请阶段
- **状态1（已通过）**：审批通过，待入库
- **状态2（已拒绝）**：审批拒绝
- **状态3（已入库）**：采购完成

### 销售流程
支持报价→销售单→发货→收款的完整流程：
- **状态-1（报价中）**：销售报价阶段
- **状态0（待审核）**：销售单待审核
- **状态1（已审核）**：审核通过，待发货
- **状态2（已发货）**：已出库，待收款
- **状态3（已收款）**：销售完成
- **状态4（已取消）**：订单取消

### 成本核算
- 销售发货时自动记录当时的商品采购价作为成本价
- 毛利分析基于发货时记录的成本价计算，确保历史利润数据准确

## 🛠 技术栈

**后端**
- Java 17
- Spring Boot 3.2
- MyBatis-Plus
- MySQL 8.0
- JWT 认证

**前端**
- Vue 3 + Composition API
- Vite
- Ant Design Vue 4
- Pinia
- Vue Router
- Axios
- ECharts

**部署**
- Docker
- Docker Compose
- Nginx

## 📁 项目结构

```
├── backend/                 # 后端项目
│   ├── src/main/java/com/wms/
│   │   ├── annotation/      # 自定义注解 (@Log, @RequirePermission)
│   │   ├── aspect/          # AOP切面 (日志、权限)
│   │   ├── common/          # 公共类 (Result, PageResult, BusinessException)
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── interceptor/     # 拦截器 (JWT认证)
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── service/         # 服务层
│   │   ├── util/            # 工具类
│   │   └── vo/              # 视图对象
│   └── src/main/resources/
│       ├── mapper/          # MyBatis XML
│       ├── application.yml  # 配置文件
│       └── schema.sql       # 数据库初始化脚本
├── frontend-admin/          # 前端项目
│   ├── src/
│   │   ├── api/             # API接口封装
│   │   ├── components/      # 公共组件
│   │   ├── layout/          # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── styles/          # 全局样式
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面组件
│   └── Dockerfile
├── docker-compose.yml       # Docker编排配置
└── README.md
```

## 🔧 开发命令

```bash
# 一键启动（首次）
docker-compose up --build

# 后台启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 仅查看后端日志
docker-compose logs -f backend

# 停止服务
docker-compose down

# 停止并清除数据
docker-compose down -v

# 重新构建单个服务
docker-compose up --build backend
```

## ⚙️ 默认配置

以下配置可在 `backend/src/main/resources/application.yml` 中修改：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `security.default-role-id` | 3 | 新用户注册时默认分配的角色ID（销售员） |
| `security.default-password` | 123456 | 新建用户/重置密码时的默认密码 |
| `security.super-admin-roles` | SUPER_ADMIN | 超级管理员角色编码 |
| `backup.path` | ./backups | 数据备份存储路径 |
| `backup.keep-days` | 30 | 备份文件保留天数 |

## 💻 本地开发

```bash
# 启动 MySQL + 后端
docker-compose up mysql backend

# 本地启动前端（另开终端）
cd frontend-admin
npm install
npm run dev
```

前端开发服务器：http://localhost:5173

## 📝 API 文档

主要 API 端点：

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/login | 用户登录 |
| 认证 | GET /api/auth/captcha | 获取验证码 |
| 用户 | GET /api/sys/user/info | 获取当前用户信息 |
| 采购 | POST /api/biz/purchase | 创建采购单 |
| 销售 | POST /api/biz/sale | 创建销售单 |
| 库存 | GET /api/biz/stock/page | 库存查询 |
| 报表 | GET /api/report/sale-rank | 销售排行 |
