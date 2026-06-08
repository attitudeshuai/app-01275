import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页', public: true } },
      // 系统管理
      { path: 'system/user', name: 'User', component: () => import('@/views/system/user/index.vue'), meta: { title: '员工管理' } },
      { path: 'system/role', name: 'Role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'Menu', component: () => import('@/views/system/menu/index.vue'), meta: { title: '菜单管理' } },
      { path: 'system/dept', name: 'Dept', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理' } },
      { path: 'system/log', name: 'Log', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志' } },
      { path: 'system/backup', name: 'Backup', component: () => import('@/views/system/backup/index.vue'), meta: { title: '数据备份' } },
      // 基础数据
      { path: 'base/supplier', name: 'Supplier', component: () => import('@/views/base/supplier/index.vue'), meta: { title: '供应商管理' } },
      { path: 'base/customer', name: 'Customer', component: () => import('@/views/base/customer/index.vue'), meta: { title: '客户管理' } },
      { path: 'base/goods', name: 'Goods', component: () => import('@/views/base/goods/index.vue'), meta: { title: '商品管理' } },
      { path: 'base/warehouse', name: 'Warehouse', component: () => import('@/views/base/warehouse/index.vue'), meta: { title: '仓库管理' } },
      // 采购管理
      { path: 'purchase/apply', name: 'PurchaseApply', component: () => import('@/views/purchase/apply/index.vue'), meta: { title: '采购申请' } },
      { path: 'purchase/order', name: 'PurchaseOrder', component: () => import('@/views/purchase/order/index.vue'), meta: { title: '采购单管理' } },
      { path: 'purchase/approve', name: 'PurchaseApprove', component: () => import('@/views/purchase/approve/index.vue'), meta: { title: '采购审批' } },
      { path: 'purchase/inbound', name: 'PurchaseInbound', component: () => import('@/views/purchase/inbound/index.vue'), meta: { title: '采购入库' } },
      // 销售管理
      { path: 'sale/quote', name: 'SaleQuote', component: () => import('@/views/sale/quote/index.vue'), meta: { title: '销售报价' } },
      { path: 'sale/order', name: 'SaleOrder', component: () => import('@/views/sale/order/index.vue'), meta: { title: '销售单管理' } },
      { path: 'sale/approve', name: 'SaleApprove', component: () => import('@/views/sale/approve/index.vue'), meta: { title: '销售审核' } },
      { path: 'sale/ship', name: 'SaleShip', component: () => import('@/views/sale/ship/index.vue'), meta: { title: '发货出库' } },
      { path: 'sale/receive', name: 'SaleReceive', component: () => import('@/views/sale/receive/index.vue'), meta: { title: '财务收款' } },
      // 库存管理
      { path: 'stock/query', name: 'StockQuery', component: () => import('@/views/stock/query/index.vue'), meta: { title: '库存查询' } },
      { path: 'stock/check', name: 'StockCheck', component: () => import('@/views/stock/check/index.vue'), meta: { title: '库存盘点' } },
      { path: 'stock/transfer', name: 'StockTransfer', component: () => import('@/views/stock/transfer/index.vue'), meta: { title: '库存调拨' } },
      { path: 'stock/adjust', name: 'StockAdjust', component: () => import('@/views/stock/adjust/index.vue'), meta: { title: '报损报溢' } },
      { path: 'stock/warning', name: 'StockWarning', component: () => import('@/views/stock/warning/index.vue'), meta: { title: '库存预警' } },
      // 统计报表
      { path: 'report/sale-rank', name: 'SaleRank', component: () => import('@/views/report/sale-rank/index.vue'), meta: { title: '销售排行' } },
      { path: 'report/purchase-trend', name: 'PurchaseTrend', component: () => import('@/views/report/purchase-trend/index.vue'), meta: { title: '采购趋势' } },
      { path: 'report/stock-warning', name: 'StockWarningReport', component: () => import('@/views/report/stock-warning/index.vue'), meta: { title: '库存预警报表' } },
      { path: 'report/profit', name: 'Profit', component: () => import('@/views/report/profit/index.vue'), meta: { title: '毛利分析' } },
      // 个人中心
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人中心', public: true } },
      // 403 无权限页面
      { path: '403', name: 'Forbidden', component: () => import('@/views/403/index.vue'), meta: { title: '无权限访问', public: true, forbidden: true } },
      // 404 页面 - 放在最后匹配所有未定义的路由
      { path: ':pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/404/index.vue'), meta: { title: '页面未找到', public: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 登录验证 + 权限校验
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 公开页面无需登录
  if (to.meta.public && !token) {
    if (to.path === '/login' || to.path === '/register') {
      return next()
    }
  }
  
  // 未登录跳转登录页
  if (!token) {
    return next('/login')
  }
  
  // 已登录访问登录/注册页，跳转首页
  if (to.path === '/login' || to.path === '/register') {
    return next('/dashboard')
  }
  
  // 公开页面直接放行
  if (to.meta.public) {
    return next()
  }
  
  // 获取用户信息进行权限校验
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  
  // 检查路由权限
  if (userStore.userInfo && !userStore.hasPathPermission(to.path)) {
    console.warn(`无权限访问: ${to.path}`)
    return next({ path: '/403', query: { redirect: to.fullPath } })
  }
  
  next()
})

export default router
