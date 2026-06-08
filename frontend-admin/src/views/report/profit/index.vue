<template>
  <div class="page-container">
    <div class="search-form">
      <a-form layout="inline">
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="dateRange" @change="fetchData" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="fetchData"><search-outlined />查询</a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-row :gutter="16" class="stat-row">
      <a-col :span="6">
        <div class="stat-card">
          <div class="stat-title">销售总额</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalSale) }}</div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card">
          <div class="stat-title">采购成本</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalCost) }}</div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card">
          <div class="stat-title">毛利润</div>
          <div class="stat-value" style="color: #52c41a">¥{{ formatMoney(stats.totalProfit) }}</div>
        </div>
      </a-col>
      <a-col :span="6">
        <div class="stat-card">
          <div class="stat-title">毛利率</div>
          <div class="stat-value" style="color: #1890ff">{{ stats.profitRate?.toFixed(2) || 0 }}%</div>
        </div>
      </a-col>
    </a-row>

    <div class="chart-container">
      <div class="chart-title">毛利趋势</div>
      <a-spin :spinning="loading">
        <div v-if="loadError" class="error-state">
          <warning-outlined class="error-icon" />
          <p>数据加载失败</p>
          <a-button type="primary" size="small" @click="fetchData">重新加载</a-button>
        </div>
        <a-empty v-else-if="!loading && trendData.length === 0" description="暂无趋势数据" />
        <div v-else ref="chartRef" class="chart"></div>
      </a-spin>
    </div>

    <div class="table-container">
      <div class="chart-title">商品毛利明细</div>
      <a-alert v-if="loadError" type="error" message="数据加载失败，请重试" show-icon style="margin-bottom: 16px" />
      <a-table 
        :columns="columns" 
        :data-source="tableData" 
        :loading="loading" 
        :pagination="pagination" 
        row-key="goodsId" 
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'saleAmount'">¥{{ record.saleAmount?.toFixed(2) }}</template>
          <template v-if="column.key === 'costAmount'">¥{{ record.costAmount?.toFixed(2) }}</template>
          <template v-if="column.key === 'profit'">
            <span :style="{ color: record.profit >= 0 ? '#52c41a' : '#f5222d' }">¥{{ record.profit?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'profitRate'">{{ record.profitRate?.toFixed(2) }}%</template>
        </template>
        <template #emptyText>
          <a-empty description="暂无毛利数据，请调整查询条件" />
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getProfitReport } from '@/api/report'
import logger from '@/utils/logger'

const loading = ref(false)
const loadError = ref(false)
const tableData = ref([])
const trendData = ref([])
const dateRange = ref([dayjs().subtract(30, 'day'), dayjs()])
const stats = reactive({ totalSale: 0, totalCost: 0, totalProfit: 0, profitRate: 0 })
const chartRef = ref()
let chart = null

const pagination = ref({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '销售数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '销售金额', key: 'saleAmount' },
  { title: '成本金额', key: 'costAmount' },
  { title: '毛利润', key: 'profit' },
  { title: '毛利率', key: 'profitRate' }
]

const formatMoney = (value) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchData = async () => {
  loading.value = true
  loadError.value = false
  try {
    const [startDate, endDate] = dateRange.value || []
    const res = await getProfitReport({
      startDate: startDate?.format('YYYY-MM-DD'),
      endDate: endDate?.format('YYYY-MM-DD'),
      page: pagination.value.current,
      size: pagination.value.pageSize
    })
    tableData.value = res.data.records || []
    pagination.value.total = res.data.total || 0
    Object.assign(stats, res.data.stats || { totalSale: 0, totalCost: 0, totalProfit: 0, profitRate: 0 })
    trendData.value = res.data.trend || []
    initChart(trendData.value)
  } catch (e) { 
    loadError.value = true
    tableData.value = []
    trendData.value = []
    Object.assign(stats, { totalSale: 0, totalCost: 0, totalProfit: 0, profitRate: 0 })
    logger.error('Failed to load profit report', { error: e.message }) 
  }
  finally { loading.value = false }
}

const initChart = (data) => {
  if (!chartRef.value || data.length === 0) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '成本', '毛利'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [
      { name: '销售额', type: 'line', smooth: true, data: data.map(d => d.saleAmount) },
      { name: '成本', type: 'line', smooth: true, data: data.map(d => d.costAmount) },
      { name: '毛利', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: data.map(d => d.profit) }
    ]
  })
}

const handleTableChange = (pag) => { pagination.value.current = pag.current; pagination.value.pageSize = pag.pageSize; fetchData() }
const handleResize = () => { chart?.resize() }

onMounted(() => { fetchData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); chart?.dispose() })
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.search-form { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-row { margin-bottom: 16px; }
.stat-card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); text-align: center; .stat-title { font-size: 14px; color: #8c8c8c; } .stat-value { font-size: 28px; font-weight: 600; margin-top: 8px; } }
.chart-container { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } .chart { height: 350px; } }
.table-container { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } }
.error-state { text-align: center; padding: 60px 0; color: #999; .error-icon { font-size: 48px; color: #faad14; margin-bottom: 16px; } p { margin-bottom: 16px; } }
</style>
