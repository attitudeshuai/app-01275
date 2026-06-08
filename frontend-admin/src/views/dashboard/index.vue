<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">{{ greeting }}，{{ userStore.userInfo?.realName || '用户' }}。这是您的业务概览。</p>
      </div>
      <div class="header-right">
        <a-button @click="fetchData" :loading="loading">
          <template #icon><reload-outlined /></template>
          刷新数据
        </a-button>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card has-chart sales">
        <div class="stat-content">
          <div class="stat-header">
            <span class="stat-label">今日销售</span>
            <div class="stat-badge green">
              <arrow-up-outlined />
              <span>12%</span>
            </div>
          </div>
          <div class="stat-value">¥{{ formatNumber(dashboard.todaySales) }}</div>
          <div class="stat-footer">
            <span class="stat-compare">本月累计</span>
            <span class="stat-month">¥{{ formatShort(dashboard.monthSales) }}</span>
          </div>
        </div>
        <div class="stat-chart-wrap">
          <div class="stat-chart" ref="salesSparkRef"></div>
        </div>
      </div>

      <div class="stat-card has-chart purchase">
        <div class="stat-content">
          <div class="stat-header">
            <span class="stat-label">今日采购</span>
            <div class="stat-badge blue">
              <arrow-up-outlined />
              <span>8%</span>
            </div>
          </div>
          <div class="stat-value">¥{{ formatNumber(dashboard.todayPurchase) }}</div>
          <div class="stat-footer">
            <span class="stat-compare">本月累计</span>
            <span class="stat-month">¥{{ formatShort(dashboard.monthPurchase) }}</span>
          </div>
        </div>
        <div class="stat-chart-wrap">
          <div class="stat-chart" ref="purchaseSparkRef"></div>
        </div>
      </div>

      <div class="stat-card compact">
        <div class="stat-icon-wrap orange">
          <inbox-outlined />
        </div>
        <div class="stat-body">
          <div class="stat-label-row">
            <span class="stat-label">库存总量</span>
          </div>
          <div class="stat-main">
            <span class="stat-value">{{ formatNumber(dashboard.totalStock, false) }}</span>
            <span class="stat-suffix">件</span>
          </div>
          <div class="stat-sub">
            <span class="stat-dot"></span>
            <span>{{ dashboard.skuCount || 0 }} 个 SKU 在库</span>
          </div>
        </div>
      </div>

      <div class="stat-card compact">
        <div class="stat-icon-wrap purple">
          <dollar-outlined />
        </div>
        <div class="stat-body">
          <div class="stat-label-row">
            <span class="stat-label">待收款</span>
            <span class="stat-count">{{ dashboard.pendingOrders || 0 }} 笔</span>
          </div>
          <div class="stat-main">
            <span class="stat-value money">¥{{ formatNumber(dashboard.pendingReceive) }}</span>
          </div>
          <a class="stat-action" @click="$router.push('/sale/receive')">
            <span>查看详情</span>
            <right-outlined />
          </a>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-grid">
      <!-- 销售趋势 -->
      <div class="card chart-card">
        <div class="card-header">
          <div class="card-title">
            <h3>销售趋势</h3>
            <p>近期销售与采购数据对比</p>
          </div>
          <div class="card-actions">
            <div class="tab-group">
              <button 
                :class="['tab-btn', { active: chartRange === '7d' }]"
                @click="chartRange = '7d'"
              >7天</button>
              <button 
                :class="['tab-btn', { active: chartRange === '30d' }]"
                @click="chartRange = '30d'"
              >30天</button>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div ref="mainChartRef" class="main-chart"></div>
        </div>
      </div>

      <!-- 商品分类 -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>商品分类</h3>
            <p>库存分布情况</p>
          </div>
        </div>
        <div class="card-body">
          <div ref="pieChartRef" class="pie-chart"></div>
          <div class="category-list">
            <div 
              v-for="(item, index) in categoryData" 
              :key="item.name"
              class="category-item"
            >
              <div class="category-dot" :style="{ background: colors[index] }"></div>
              <span class="category-name">{{ item.name }}</span>
              <span class="category-value">{{ item.value }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="bottom-grid">
      <!-- 待办事项 -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>待办事项</h3>
            <p>需要您处理的任务</p>
          </div>
        </div>
        <div class="card-body no-padding">
          <div class="todo-list">
            <div 
              v-for="item in todoList" 
              :key="item.key"
              class="todo-item"
              @click="$router.push(item.path)"
            >
              <div class="todo-icon" :class="item.type">
                <component :is="item.icon" />
              </div>
              <div class="todo-content">
                <span class="todo-title">{{ item.title }}</span>
                <span class="todo-desc">{{ item.desc }}</span>
              </div>
              <div class="todo-count" v-if="item.count > 0">
                <span class="count-badge">{{ item.count }}</span>
              </div>
              <right-outlined class="todo-arrow" />
            </div>
          </div>
        </div>
      </div>

      <!-- 库存预警 -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>库存预警</h3>
            <p>需要关注的商品</p>
          </div>
          <a class="card-link" @click="$router.push('/stock/warning')">
            查看全部 <right-outlined />
          </a>
        </div>
        <div class="card-body no-padding">
          <div class="warning-list" v-if="stockWarnings.length">
            <div 
              v-for="item in stockWarnings" 
              :key="item.id"
              class="warning-item"
            >
              <div class="warning-info">
                <span class="warning-name">{{ item.goodsName }}</span>
                <span class="warning-code">{{ item.goodsCode }}</span>
              </div>
              <div class="warning-stock">
                <div class="stock-bar-wrap">
                  <div 
                    class="stock-bar-fill" 
                    :class="getStockLevel(item)"
                    :style="{ width: getStockPercent(item) + '%' }"
                  ></div>
                </div>
                <span class="stock-text">
                  {{ item.quantity }} / {{ item.safetyStock }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <check-circle-outlined />
            <span>库存状态良好</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>快捷操作</h3>
            <p>常用功能入口</p>
          </div>
        </div>
        <div class="card-body">
          <div class="quick-grid">
            <div class="quick-item" @click="$router.push('/purchase/apply')">
              <div class="quick-icon blue">
                <shopping-cart-outlined />
              </div>
              <span>新建采购</span>
            </div>
            <div class="quick-item" @click="$router.push('/sale/order')">
              <div class="quick-icon green">
                <file-text-outlined />
              </div>
              <span>销售开单</span>
            </div>
            <div class="quick-item" @click="$router.push('/stock/query')">
              <div class="quick-icon purple">
                <search-outlined />
              </div>
              <span>库存查询</span>
            </div>
            <div class="quick-item" @click="$router.push('/stock/check')">
              <div class="quick-icon orange">
                <audit-outlined />
              </div>
              <span>库存盘点</span>
            </div>
            <div class="quick-item" @click="$router.push('/base/goods')">
              <div class="quick-icon cyan">
                <appstore-outlined />
              </div>
              <span>商品管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/report/profit')">
              <div class="quick-icon pink">
                <line-chart-outlined />
              </div>
              <span>利润报表</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick, h } from 'vue'
import {
  ReloadOutlined, ArrowUpOutlined, RightOutlined, CheckCircleOutlined,
  ShoppingCartOutlined, FileTextOutlined, SearchOutlined, AuditOutlined,
  AppstoreOutlined, LineChartOutlined, FileSyncOutlined, AlertOutlined,
  DollarOutlined, InboxOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/report'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const dashboard = ref({})
const loading = ref(false)
const chartRange = ref('7d')
const stockWarnings = ref([])
const categoryData = ref([])

const mainChartRef = ref()
const pieChartRef = ref()
const salesSparkRef = ref()
const purchaseSparkRef = ref()

let mainChart = null
let pieChart = null
let salesSpark = null
let purchaseSpark = null

const colors = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4']

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todoList = computed(() => [
  {
    key: 'purchase',
    title: '待审批采购单',
    desc: '有新的采购申请等待审批',
    count: dashboard.value.pendingPurchaseCount || 0,
    icon: FileSyncOutlined,
    type: 'blue',
    path: '/purchase/approve'
  },
  {
    key: 'sale',
    title: '待发货订单',
    desc: '已审批的销售单等待发货',
    count: dashboard.value.pendingShipCount || 0,
    icon: InboxOutlined,
    type: 'green',
    path: '/sale/ship'
  },
  {
    key: 'receive',
    title: '待收款订单',
    desc: '已发货的订单等待收款',
    count: dashboard.value.pendingOrders || 0,
    icon: DollarOutlined,
    type: 'orange',
    path: '/sale/receive'
  },
  {
    key: 'warning',
    title: '库存预警',
    desc: '部分商品库存异常',
    count: dashboard.value.warningCount || 0,
    icon: AlertOutlined,
    type: 'red',
    path: '/stock/warning'
  }
])

const formatNumber = (value, isMoney = true) => {
  if (!value) return isMoney ? '0.00' : '0'
  const num = Number(value)
  if (isMoney) {
    return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  return num.toLocaleString('zh-CN')
}

const formatShort = (value) => {
  if (!value) return '0'
  const num = Number(value)
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const getStockPercent = (item) => {
  if (!item.safetyStock) return 0
  return Math.min(100, Math.max(5, (item.quantity / item.safetyStock) * 100))
}

const getStockLevel = (item) => {
  const ratio = item.quantity / item.safetyStock
  if (ratio < 0.3) return 'critical'
  if (ratio < 0.6) return 'warning'
  return 'normal'
}

const initMainChart = () => {
  if (!mainChartRef.value) return
  if (mainChart) mainChart.dispose()
  mainChart = echarts.init(mainChartRef.value)
  
  const trend = dashboard.value.salesTrend || []
  const dates = trend.length ? trend.map(i => i.date?.substring(5)) : 
    Array.from({length: 7}, (_, i) => `0${i+1}-0${i+1}`.slice(-5))
  const sales = trend.length ? trend.map(i => Number(i.sales) || 0) :
    [12400, 15600, 11200, 18900, 14300, 21000, 16800]
  const purchase = trend.length ? trend.map(i => Number(i.purchase) || 0) :
    [8200, 9800, 7600, 12400, 9100, 14200, 11300]

  mainChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#374151', fontSize: 13 },
      axisPointer: { type: 'cross', crossStyle: { color: '#999' } }
    },
    legend: {
      data: ['销售额', '采购额'],
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#6b7280', fontSize: 12 }
    },
    grid: { left: 0, right: 0, bottom: 36, top: 12, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { 
        color: '#9ca3af', 
        fontSize: 11,
        formatter: v => v >= 10000 ? (v/10000) + 'w' : v
      }
    },
    series: [
      {
        name: '销售额',
        type: 'bar',
        barWidth: '35%',
        itemStyle: { color: '#22c55e', borderRadius: [4, 4, 0, 0] },
        data: sales
      },
      {
        name: '采购额',
        type: 'bar',
        barWidth: '35%',
        itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] },
        data: purchase
      }
    ]
  })
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)
  
  let data = (dashboard.value.categoryRatio || []).map(i => ({
    name: i.name || '未分类',
    value: Number(i.value) || 0
  }))
  
  if (!data.length || data.every(d => d.value === 0)) {
    data = [
      { name: '电子产品', value: 35 },
      { name: '办公用品', value: 28 },
      { name: '生活用品', value: 20 },
      { name: '其他', value: 17 }
    ]
  }
  
  categoryData.value = data

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['55%', '80%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      label: { show: false },
      data
    }]
  })
}

const initSparkline = (el, data, color, isGreen = false) => {
  if (!el) return null
  const chart = echarts.init(el)
  const gradientColor = isGreen ? 
    ['rgba(34, 197, 94, 0.4)', 'rgba(34, 197, 94, 0.02)'] :
    ['rgba(99, 102, 241, 0.4)', 'rgba(99, 102, 241, 0.02)']
  
  chart.setOption({
    grid: { left: 0, right: 0, top: 8, bottom: 0 },
    xAxis: { type: 'category', show: false, data: data.map((_, i) => i) },
    yAxis: { type: 'value', show: false, min: 'dataMin' },
    series: [{
      type: 'line',
      data,
      smooth: 0.6,
      symbol: 'none',
      lineStyle: { width: 2.5, color, cap: 'round' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: gradientColor[0] },
          { offset: 1, color: gradientColor[1] }
        ])
      }
    }]
  })
  return chart
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    dashboard.value = res.data || {}
    stockWarnings.value = (res.data?.stockWarnings || []).slice(0, 5)
    await nextTick()
    initMainChart()
    initPieChart()
    salesSpark = initSparkline(salesSparkRef.value, [3,5,4,7,6,8,7,9,8,10], '#22c55e', true)
    purchaseSpark = initSparkline(purchaseSparkRef.value, [2,4,3,5,4,6,5,7,6,8], '#6366f1', false)
  } catch (e) {
    await nextTick()
    initMainChart()
    initPieChart()
    salesSpark = initSparkline(salesSparkRef.value, [3,5,4,7,6,8,7,9,8,10], '#22c55e', true)
    purchaseSpark = initSparkline(purchaseSparkRef.value, [2,4,3,5,4,6,5,7,6,8], '#6366f1', false)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  mainChart?.resize()
  pieChart?.resize()
  salesSpark?.resize()
  purchaseSpark?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  mainChart?.dispose()
  pieChart?.dispose()
  salesSpark?.dispose()
  purchaseSpark?.dispose()
})

watch(chartRange, fetchData)
</script>


<style lang="scss" scoped>
.dashboard {
  max-width: 1440px;
  margin: 0 auto;
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  
  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #111827;
    margin: 0 0 6px;
    letter-spacing: -0.5px;
  }
  
  .page-desc {
    font-size: 15px;
    color: #6b7280;
    margin: 0;
  }
}

// 统计卡片行
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
  display: flex;
  gap: 16px;
  transition: all 0.2s ease;
  min-height: 180px;
  
  &:hover {
    border-color: #d1d5db;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  &.has-chart {
    flex-direction: column;
    padding-bottom: 0;
    
    .stat-content {
      flex: 1;
    }
    
    .stat-chart-wrap {
      margin: 0 -24px;
      height: 80px;
      position: relative;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 30px;
        background: linear-gradient(to bottom, #fff, transparent);
        z-index: 1;
        pointer-events: none;
      }
    }
    
    .stat-chart {
      width: 100%;
      height: 100%;
    }
  }
  
  &.sales {
    background: linear-gradient(135deg, #f0fdf4 0%, #fff 100%);
    border-color: #bbf7d0;
    
    .stat-value {
      color: #15803d;
    }
    
    .stat-chart-wrap::before {
      background: linear-gradient(to bottom, #f0fdf4, transparent);
    }
  }
  
  &.purchase {
    background: linear-gradient(135deg, #eff6ff 0%, #fff 100%);
    border-color: #bfdbfe;
    
    .stat-value {
      color: #1d4ed8;
    }
    
    .stat-chart-wrap::before {
      background: linear-gradient(to bottom, #eff6ff, transparent);
    }
  }
  
  // 紧凑型卡片（库存、待收款）
  &.compact {
    align-items: flex-start;
    min-height: 140px;
    
    .stat-body {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      height: 100%;
    }
    
    .stat-label-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
      min-height: 24px;
    }
    
    .stat-label {
      margin-bottom: 0;
    }
    
    .stat-main {
      display: flex;
      align-items: baseline;
      gap: 6px;
      margin-bottom: 12px;
      flex: 1;
    }
    
    .stat-value {
      font-size: 26px;
      margin-bottom: 0;
      
      &.money {
        color: #7c3aed;
      }
    }
    
    .stat-suffix {
      font-size: 14px;
      color: #9ca3af;
      font-weight: 500;
    }
    
    .stat-count {
      font-size: 12px;
      color: #6b7280;
      background: #f3f4f6;
      padding: 4px 10px;
      border-radius: 12px;
      font-weight: 500;
    }
    
    .stat-sub {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #6b7280;
      min-height: 20px;
      
      .stat-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #22c55e;
      }
    }
    
    .stat-action {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #6366f1;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.15s;
      min-height: 20px;
      
      &:hover {
        color: #4f46e5;
        gap: 8px;
      }
      
      :deep(.anticon) {
        font-size: 11px;
      }
    }
  }
  
  .stat-icon-wrap {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
    
    &.orange {
      background: linear-gradient(135deg, #fed7aa 0%, #fdba74 100%);
      color: #c2410c;
      box-shadow: 0 4px 12px rgba(251, 146, 60, 0.3);
    }
    
    &.purple {
      background: linear-gradient(135deg, #e9d5ff 0%, #d8b4fe 100%);
      color: #7c3aed;
      box-shadow: 0 4px 12px rgba(167, 139, 250, 0.3);
    }
  }
  
  .stat-content {
    flex: 1;
    min-width: 0;
  }
  
  .stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .stat-label {
    font-size: 13px;
    font-weight: 600;
    color: #6b7280;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .stat-badge {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    font-weight: 600;
    padding: 4px 10px;
    border-radius: 20px;
    
    :deep(.anticon) {
      font-size: 10px;
    }
    
    &.green {
      background: #dcfce7;
      color: #15803d;
    }
    
    &.blue {
      background: #dbeafe;
      color: #1d4ed8;
    }
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #111827;
    letter-spacing: -0.5px;
    line-height: 1.2;
    margin-bottom: 8px;
  }
  
  .stat-footer {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  .stat-compare {
    font-size: 13px;
    color: #9ca3af;
  }
  
  .stat-month {
    font-size: 13px;
    font-weight: 600;
    color: #374151;
  }
}

// 主内容网格
.main-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  margin-bottom: 24px;
}

// 底部网格
.bottom-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

// 卡片通用样式
.card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20px 24px 16px;
    
    .card-title {
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #111827;
        margin: 0 0 4px;
      }
      
      p {
        font-size: 13px;
        color: #9ca3af;
        margin: 0;
      }
    }
    
    .card-link {
      font-size: 13px;
      color: #6b7280;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
      
      &:hover {
        color: #6366f1;
      }
    }
  }
  
  .card-body {
    padding: 0 24px 24px;
    
    &.no-padding {
      padding: 0;
    }
  }
  
  .card-actions {
    .tab-group {
      display: flex;
      background: #f3f4f6;
      border-radius: 8px;
      padding: 3px;
      
      .tab-btn {
        padding: 6px 14px;
        font-size: 13px;
        font-weight: 500;
        color: #6b7280;
        background: transparent;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        
        &.active {
          background: #fff;
          color: #111827;
          box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        
        &:hover:not(.active) {
          color: #374151;
        }
      }
    }
  }
}

// 图表
.main-chart {
  height: 320px;
}

.pie-chart {
  height: 180px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  
  .category-item {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .category-dot {
      width: 10px;
      height: 10px;
      border-radius: 3px;
    }
    
    .category-name {
      flex: 1;
      font-size: 13px;
      color: #374151;
    }
    
    .category-value {
      font-size: 13px;
      font-weight: 600;
      color: #111827;
    }
  }
}

// 待办列表
.todo-list {
  .todo-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 24px;
    cursor: pointer;
    transition: background 0.15s;
    border-bottom: 1px solid #f3f4f6;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: #f9fafb;
    }
    
    .todo-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      
      &.blue { background: #eff6ff; color: #3b82f6; }
      &.green { background: #f0fdf4; color: #22c55e; }
      &.orange { background: #fff7ed; color: #f97316; }
      &.red { background: #fef2f2; color: #ef4444; }
    }
    
    .todo-content {
      flex: 1;
      min-width: 0;
      
      .todo-title {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #111827;
      }
      
      .todo-desc {
        display: block;
        font-size: 12px;
        color: #9ca3af;
        margin-top: 2px;
      }
    }
    
    .todo-count {
      .count-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 22px;
        height: 22px;
        padding: 0 8px;
        background: #ef4444;
        color: #fff;
        font-size: 12px;
        font-weight: 600;
        border-radius: 11px;
      }
    }
    
    .todo-arrow {
      color: #d1d5db;
      font-size: 12px;
    }
  }
}

// 库存预警列表
.warning-list {
  .warning-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 24px;
    border-bottom: 1px solid #f3f4f6;
    
    &:last-child {
      border-bottom: none;
    }
    
    .warning-info {
      .warning-name {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #111827;
      }
      
      .warning-code {
        display: block;
        font-size: 12px;
        color: #9ca3af;
        margin-top: 2px;
      }
    }
    
    .warning-stock {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .stock-bar-wrap {
        width: 80px;
        height: 6px;
        background: #e5e7eb;
        border-radius: 3px;
        overflow: hidden;
        
        .stock-bar-fill {
          height: 100%;
          border-radius: 3px;
          transition: width 0.3s;
          
          &.critical { background: #ef4444; }
          &.warning { background: #f59e0b; }
          &.normal { background: #22c55e; }
        }
      }
      
      .stock-text {
        font-size: 13px;
        color: #6b7280;
        min-width: 60px;
        text-align: right;
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
  gap: 12px;
  min-height: 200px;
  
  :deep(.anticon) {
    font-size: 48px;
    color: #22c55e;
  }
  
  span {
    font-size: 14px;
    color: #6b7280;
  }
}

// 快捷操作
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px 12px;
    background: #f9fafb;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f3f4f6;
      transform: translateY(-2px);
      
      .quick-icon {
        transform: scale(1.1);
      }
    }
    
    .quick-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      transition: transform 0.2s;
      
      &.blue { background: #dbeafe; color: #2563eb; }
      &.green { background: #dcfce7; color: #16a34a; }
      &.purple { background: #f3e8ff; color: #9333ea; }
      &.orange { background: #ffedd5; color: #ea580c; }
      &.cyan { background: #cffafe; color: #0891b2; }
      &.pink { background: #fce7f3; color: #db2777; }
    }
    
    span {
      font-size: 13px;
      font-weight: 500;
      color: #374151;
    }
  }
}

// 响应式
@media (max-width: 1200px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
  
  .bottom-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 992px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    
    .header-right {
      width: 100%;
      
      button {
        width: 100%;
      }
    }
  }
  
  .stat-card .stat-value {
    font-size: 26px;
  }
  
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
