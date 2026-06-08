<template>
  <a-layout class="app-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="64"
      class="app-sider"
    >
      <!-- Logo -->
      <div class="sider-logo" :class="{ collapsed }">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 22V12H15V22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-show="!collapsed" class="logo-text">智慧仓储</span>
        </transition>
      </div>

      <!-- 菜单 -->
      <div class="sider-menu">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          theme="light"
          @click="handleMenuClick"
        >
          <!-- 动态渲染菜单 -->
          <template v-for="menu in userMenus" :key="menu.path || menu.id">
            <!-- 无子菜单 -->
            <a-menu-item v-if="!menu.children || menu.children.length === 0" :key="menu.path">
              <template #icon>
                <component :is="getIcon(menu.icon)" />
              </template>
              <span>{{ menu.menuName }}</span>
            </a-menu-item>
            <!-- 有子菜单 -->
            <a-sub-menu v-else :key="menu.path || String(menu.id)">
              <template #icon>
                <component :is="getIcon(menu.icon)" />
              </template>
              <template #title>{{ menu.menuName }}</template>
              <a-menu-item v-for="child in menu.children" :key="child.path">
                <template #icon>
                  <component :is="getIcon(child.icon)" />
                </template>
                {{ child.menuName }}
              </a-menu-item>
            </a-sub-menu>
          </template>
        </a-menu>
      </div>

      <!-- 底部折叠按钮 -->
      <div class="sider-footer">
        <div class="collapse-btn" @click="collapsed = !collapsed">
          <menu-unfold-outlined v-if="collapsed" />
          <menu-fold-outlined v-else />
        </div>
      </div>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout class="app-main">
      <!-- 顶部导航 -->
      <a-layout-header class="app-header">
        <div class="header-left">
          <a-breadcrumb class="header-breadcrumb">
            <a-breadcrumb-item>
              <router-link to="/"><HomeOutlined /></router-link>
            </a-breadcrumb-item>
            <a-breadcrumb-item v-if="currentMenu">{{ currentMenu }}</a-breadcrumb-item>
            <a-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 通知铃铛 -->
          <a-dropdown placement="bottomRight" :trigger="['click']">
            <a-badge :count="unreadCount" :offset="[-2, 2]">
              <span class="action-btn">
                <BellOutlined />
              </span>
            </a-badge>
            <template #overlay>
              <div class="notification-dropdown">
                <div class="notification-header">
                  <span>通知消息</span>
                  <a-button type="link" size="small" @click="handleMarkAllRead" v-if="unreadCount > 0">全部已读</a-button>
                </div>
                <div class="notification-list" v-if="notifications.length > 0">
                  <div 
                    v-for="item in notifications" 
                    :key="item.id" 
                    class="notification-item"
                    :class="{ 'warning': item.type === 'STOCK_WARNING' || item.type === 'STOCK_OVERFLOW' }"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notification-icon">
                      <WarningOutlined v-if="item.type === 'STOCK_WARNING' || item.type === 'STOCK_OVERFLOW'" />
                      <BellOutlined v-else />
                    </div>
                    <div class="notification-content">
                      <div class="notification-title">{{ item.title }}</div>
                      <div class="notification-desc">{{ item.content }}</div>
                      <div class="notification-time">{{ formatDateTime(item.createTime) }}</div>
                    </div>
                  </div>
                </div>
                <div class="notification-empty" v-else>
                  <a-empty description="暂无通知" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
                </div>
              </div>
            </template>
          </a-dropdown>

          <!-- 全屏 -->
          <span class="action-btn" @click="toggleFullscreen">
            <FullscreenOutlined v-if="!isFullscreen" />
            <FullscreenExitOutlined v-else />
          </span>

          <!-- 分隔线 -->
          <span class="header-divider"></span>

          <!-- 用户信息 -->
          <a-dropdown placement="bottomRight">
            <span class="user-dropdown">
              <a-avatar :size="32" class="user-avatar">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
              <DownOutlined class="dropdown-arrow" />
            </span>
            <template #overlay>
              <a-menu @click="handleCommand" class="user-menu">
                <a-menu-item key="profile">
                  <UserOutlined />
                  <span>个人中心</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" class="logout-item">
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Modal, Empty } from 'ant-design-vue'
import { 
  FullscreenOutlined, FullscreenExitOutlined, BellOutlined, WarningOutlined,
  HomeOutlined, SettingOutlined, TeamOutlined, SafetyCertificateOutlined,
  MenuOutlined, ApartmentOutlined, FileTextOutlined, DatabaseOutlined,
  UserOutlined, ContactsOutlined, GiftOutlined, ShoppingCartOutlined,
  FormOutlined, FileDoneOutlined, AuditOutlined, ImportOutlined,
  ShopOutlined, ExportOutlined, DollarOutlined, InboxOutlined,
  SearchOutlined, SolutionOutlined, SwapOutlined, EditOutlined,
  AlertOutlined, BarChartOutlined, RiseOutlined, LineChartOutlined,
  FundOutlined, MenuFoldOutlined, MenuUnfoldOutlined, DownOutlined,
  LogoutOutlined, SaveOutlined
} from '@ant-design/icons-vue'
import { getUnreadNotifications, getUnreadNotificationCount, markNotificationAsRead, markAllNotificationsAsRead } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const selectedKeys = ref([])
const openKeys = ref([])
const isFullscreen = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let notificationTimer = null

// 图标映射
const iconMap = {
  'home': HomeOutlined,
  'HomeOutlined': HomeOutlined,
  'setting': SettingOutlined,
  'SettingOutlined': SettingOutlined,
  'team': TeamOutlined,
  'TeamOutlined': TeamOutlined,
  'safety-certificate': SafetyCertificateOutlined,
  'SafetyCertificateOutlined': SafetyCertificateOutlined,
  'menu': MenuOutlined,
  'MenuOutlined': MenuOutlined,
  'apartment': ApartmentOutlined,
  'ApartmentOutlined': ApartmentOutlined,
  'file-text': FileTextOutlined,
  'FileTextOutlined': FileTextOutlined,
  'database': DatabaseOutlined,
  'DatabaseOutlined': DatabaseOutlined,
  'user': UserOutlined,
  'UserOutlined': UserOutlined,
  'contacts': ContactsOutlined,
  'ContactsOutlined': ContactsOutlined,
  'gift': GiftOutlined,
  'GiftOutlined': GiftOutlined,
  'shopping-cart': ShoppingCartOutlined,
  'ShoppingCartOutlined': ShoppingCartOutlined,
  'form': FormOutlined,
  'FormOutlined': FormOutlined,
  'file-done': FileDoneOutlined,
  'FileDoneOutlined': FileDoneOutlined,
  'audit': AuditOutlined,
  'AuditOutlined': AuditOutlined,
  'import': ImportOutlined,
  'ImportOutlined': ImportOutlined,
  'shop': ShopOutlined,
  'ShopOutlined': ShopOutlined,
  'export': ExportOutlined,
  'ExportOutlined': ExportOutlined,
  'dollar': DollarOutlined,
  'DollarOutlined': DollarOutlined,
  'inbox': InboxOutlined,
  'InboxOutlined': InboxOutlined,
  'search': SearchOutlined,
  'SearchOutlined': SearchOutlined,
  'solution': SolutionOutlined,
  'SolutionOutlined': SolutionOutlined,
  'swap': SwapOutlined,
  'SwapOutlined': SwapOutlined,
  'edit': EditOutlined,
  'EditOutlined': EditOutlined,
  'alert': AlertOutlined,
  'AlertOutlined': AlertOutlined,
  'bar-chart': BarChartOutlined,
  'BarChartOutlined': BarChartOutlined,
  'rise': RiseOutlined,
  'RiseOutlined': RiseOutlined,
  'line-chart': LineChartOutlined,
  'LineChartOutlined': LineChartOutlined,
  'warning': WarningOutlined,
  'WarningOutlined': WarningOutlined,
  'fund': FundOutlined,
  'FundOutlined': FundOutlined,
  'save': SaveOutlined,
  'SaveOutlined': SaveOutlined
}

const getIcon = (icon) => {
  if (!icon || typeof icon !== 'string') return HomeOutlined
  // 检查是否是有效的图标名称（不能是纯数字）
  if (/^\d+$/.test(icon)) return HomeOutlined
  return iconMap[icon] || HomeOutlined
}

// 从用户信息获取菜单
const userMenus = computed(() => {
  const menus = userStore.userInfo?.menus || []
  // 过滤掉按钮类型(type=3)，只保留目录(type=1)和菜单(type=2)
  return menus.filter(m => m.type !== 3).map(menu => ({
    ...menu,
    children: menu.children?.filter(c => c.type !== 3) || []
  }))
})

const menuMap = {
  system: '系统管理',
  base: '基础数据',
  purchase: '采购管理',
  sale: '销售管理',
  stock: '库存管理',
  report: '统计报表'
}

const currentMenu = computed(() => {
  const parts = route.path.split('/')
  if (parts.length > 1) {
    return menuMap[parts[1]] || ''
  }
  return ''
})

watch(() => route.path, (path) => {
  selectedKeys.value = [path]
  const parts = path.split('/')
  if (parts.length > 2 && !collapsed.value) {
    // 找到对应的父菜单key
    const parentMenu = userMenus.value.find(m => 
      m.children?.some(c => c.path === path)
    )
    if (parentMenu) {
      openKeys.value = [parentMenu.path || String(parentMenu.id)]
    }
  }
  logger.pageView(path, route.meta.title)
}, { immediate: true })

// 获取通知
const fetchNotifications = async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      getUnreadNotifications(10),
      getUnreadNotificationCount()
    ])
    notifications.value = listRes.data || []
    unreadCount.value = countRes.data?.count || 0
  } catch (e) {
    logger.error('Failed to fetch notifications', { error: e.message })
  }
}

// 标记单条已读
const handleNotificationClick = async (item) => {
  try {
    await markNotificationAsRead(item.id)
    if (item.type === 'STOCK_WARNING' || item.type === 'STOCK_OVERFLOW') {
      router.push('/stock/warning')
    }
    fetchNotifications()
  } catch (e) {
    logger.error('Failed to mark notification as read', { error: e.message })
  }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsAsRead()
    fetchNotifications()
  } catch (e) {
    logger.error('Failed to mark all notifications as read', { error: e.message })
  }
}

onMounted(() => {
  userStore.fetchUserInfo()
  fetchNotifications()
  notificationTimer = setInterval(fetchNotifications, 60000)
})

onUnmounted(() => {
  if (notificationTimer) {
    clearInterval(notificationTimer)
  }
})

const handleMenuClick = ({ key }) => {
  logger.action('Menu Click', { path: key })
  router.push(key)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const handleCommand = ({ key }) => {
  if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'logout') {
    Modal.confirm({
      title: '确认退出',
      content: '确定要退出登录吗？',
      okText: '确定',
      cancelText: '取消',
      centered: true,
      onOk() {
        logger.action('Logout')
        userStore.logout()
        router.push('/login')
      }
    })
  }
}
</script>


<style lang="scss" scoped>
.app-layout {
  min-height: 100vh;
}

// 侧边栏
.app-sider {
  background: #fff !important;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  border-right: 1px solid #f0f0f0;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  
  :deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
}

.sider-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  
  &.collapsed {
    justify-content: center;
    padding: 0;
    
    .logo-icon {
      width: 36px;
      height: 36px;
    }
  }
  
  .logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    
    svg {
      width: 22px;
      height: 22px;
    }
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }
}

.sider-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 8px;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
  }
  
  :deep(.ant-menu) {
    background: transparent;
    border: none;
    
    .ant-menu-item {
      height: 40px;
      line-height: 40px;
      margin: 2px 0;
      padding: 0 12px !important;
      border-radius: 8px;
      color: #64748b;
      transition: all 0.2s ease;
      font-size: 14px;
      
      &-selected {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
        color: #fff !important;
        font-weight: 500;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
        
        .anticon {
          color: #fff;
        }
        
        &::after {
          display: none;
        }
      }
      
      .anticon {
        font-size: 16px;
        color: #94a3b8;
        transition: color 0.2s ease;
      }
    }
    
    .ant-menu-submenu {
      .ant-menu-submenu-title {
        height: 40px;
        line-height: 40px;
        margin: 2px 0;
        padding: 0 12px !important;
        border-radius: 8px;
        color: #64748b;
        transition: all 0.2s ease;
        font-size: 14px;
        font-weight: 500;
        
        .anticon {
          font-size: 16px;
          color: #94a3b8;
          transition: color 0.2s ease;
        }
        
        .ant-menu-submenu-arrow {
          color: #94a3b8;
          
          &::before,
          &::after {
            background: #94a3b8;
          }
        }
      }
      
      &-open > .ant-menu-submenu-title {
        color: #667eea;
        background: #f8fafc;
        
        .anticon {
          color: #667eea;
        }
        
        .ant-menu-submenu-arrow {
          &::before,
          &::after {
            background: #667eea;
          }
        }
      }
      
      .ant-menu-sub {
        background: transparent !important;
        
        .ant-menu-item {
          padding-left: 44px !important;
          height: 36px;
          line-height: 36px;
          font-size: 13px;
          
          .anticon {
            font-size: 14px;
          }
        }
      }
    }
  }
}

.sider-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  
  .collapse-btn {
    width: 100%;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8fafc;
    border-radius: 8px;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 16px;
  }
}

// 主内容区
.app-main {
  margin-left: 240px;
  transition: margin-left 0.3s ease;
  min-height: 100vh;
  background: #f8fafc;
}

.app-sider.ant-layout-sider-collapsed + .app-main {
  margin-left: 64px;
}

// 顶部导航
.app-header {
  height: 64px;
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
  
  .header-breadcrumb {
    :deep(.ant-breadcrumb-link) {
      color: #64748b;
      font-size: 14px;
      
      a {
        color: #64748b;
        transition: color 0.2s ease;
      }
    }
    
    :deep(.ant-breadcrumb-separator) {
      color: #cbd5e1;
    }
    
    :deep(.ant-breadcrumb > span:last-child .ant-breadcrumb-link) {
      color: #1e293b;
      font-weight: 500;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  
  .action-btn {
    cursor: pointer;
    font-size: 18px;
    color: #64748b;
    transition: all 0.2s ease;
    padding: 8px;
    border-radius: 8px;
  }
  
  .header-divider {
    width: 1px;
    height: 24px;
    background: #e2e8f0;
  }
}

.user-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 10px;
  transition: all 0.2s ease;
  
  .user-avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
  }
  
  .user-name {
    font-size: 14px;
    color: #1e293b;
    font-weight: 500;
  }
  
  .dropdown-arrow {
    color: #94a3b8;
    font-size: 10px;
  }
}

.user-menu {
  min-width: 160px;
  padding: 8px;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  
  :deep(.ant-dropdown-menu-item) {
    padding: 10px 12px;
    border-radius: 6px;
    font-size: 14px;
    transition: all 0.2s ease;
    
    .anticon {
      margin-right: 10px;
      font-size: 16px;
    }
    
    &.logout-item {
      color: #ef4444;
    }
  }
}

// 内容区
.app-content {
  padding: 20px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

// 页面切换动画
.page-enter-active,
.page-leave-active {
  transition: all 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

// 文字淡入淡出
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 通知下拉框
.notification-dropdown {
  width: 360px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    font-weight: 600;
    font-size: 15px;
    color: #1e293b;
  }
  
  .notification-list {
    max-height: 400px;
    overflow-y: auto;
  }
  
  .notification-item {
    display: flex;
    gap: 12px;
    padding: 14px 20px;
    cursor: pointer;
    transition: background 0.2s ease;
    border-bottom: 1px solid #f8fafc;
    
    &:last-child {
      border-bottom: none;
    }
    
    &.warning {
      .notification-icon {
        background: #fef3c7;
        color: #f59e0b;
      }
    }
    
    .notification-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: #e0e7ff;
      color: #667eea;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
    }
    
    .notification-content {
      flex: 1;
      min-width: 0;
      
      .notification-title {
        font-weight: 500;
        font-size: 14px;
        color: #1e293b;
        margin-bottom: 4px;
      }
      
      .notification-desc {
        font-size: 13px;
        color: #64748b;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .notification-time {
        font-size: 12px;
        color: #94a3b8;
        margin-top: 6px;
      }
    }
  }
  
  .notification-empty {
    padding: 40px 20px;
  }
}
</style>
