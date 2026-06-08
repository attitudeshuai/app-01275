<template>
  <div class="page-container">
    <a-row :gutter="16" class="stat-row">
      <a-col :span="8">
        <div class="stat-card" style="border-left: 4px solid #f5222d">
          <div class="stat-title">库存不足</div>
          <div class="stat-value">{{ stats.lowCount || 0 }}</div>
          <div class="stat-desc">需要补货的商品数量</div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="stat-card" style="border-left: 4px solid #fa8c16">
          <div class="stat-title">库存过高</div>
          <div class="stat-value">{{ stats.highCount || 0 }}</div>
          <div class="stat-desc">需要处理的呆滞库存</div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="stat-card" style="border-left: 4px solid #52c41a">
          <div class="stat-title">库存正常</div>
          <div class="stat-value">{{ stats.normalCount || 0 }}</div>
          <div class="stat-desc">库存状态正常的商品</div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :span="12">
        <div class="chart-container">
          <div class="chart-title">库存状态分布</div>
          <div ref="pieChartRef" class="chart"></div>
        </div>
      </a-col>
      <a-col :span="12">
        <div class="chart-container">
          <div class="chart-title">预警商品TOP10</div>
          <div ref="barChartRef" class="chart"></div>
        </div>
      </a-col>
    </a-row>

    <div class="table-container">
      <div class="chart-title">预警商品列表</div>
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.quantity < record.safetyStock ? 'error' : 'warning'">
              {{ record.quantity < record.safetyStock ? '库存不足' : '库存过高' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getStockWarningReport } from '@/api/report'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const stats = reactive({ lowCount: 0, highCount: 0, normalCount: 0 })
const pieChartRef = ref()
const barChartRef = ref()
let pieChart = null
let barChart = null

const pagination = ref({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '当前库存', dataIndex: 'quantity', key: 'quantity' },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock' },
  { title: '最大库存', dataIndex: 'maxStock', key: 'maxStock' },
  { title: '状态', key: 'status', width: 100 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockWarningReport({ page: pagination.value.current, size: pagination.value.pageSize })
    tableData.value = res.data.records
    pagination.value.total = res.data.total
    Object.assign(stats, res.data.stats || {})
    initCharts(res.data)
  } catch (e) { logger.error('Failed to load stock warning report', { error: e.message }) }
  finally { loading.value = false }
}

const initCharts = (data) => {
  if (!pieChartRef.value || !barChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  if (!barChart) barChart = echarts.init(barChartRef.value)

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: [
        { value: stats.lowCount, name: '库存不足', itemStyle: { color: '#f5222d' } },
        { value: stats.highCount, name: '库存过高', itemStyle: { color: '#fa8c16' } },
        { value: stats.normalCount, name: '库存正常', itemStyle: { color: '#52c41a' } }
      ]
    }]
  })

  const top10 = data.records.slice(0, 10)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: top10.map(d => d.goodsName) },
    yAxis: { type: 'value' },
    series: [
      { name: '当前库存', type: 'bar', data: top10.map(d => d.quantity), itemStyle: { color: '#1890ff' } },
      { name: '安全库存', type: 'line', data: top10.map(d => d.safetyStock), itemStyle: { color: '#f5222d' } }
    ]
  })
}

const handleTableChange = (pag) => { pagination.value.current = pag.current; pagination.value.pageSize = pag.pageSize; fetchData() }
const handleResize = () => { pieChart?.resize(); barChart?.resize() }

onMounted(() => { fetchData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); pieChart?.dispose(); barChart?.dispose() })
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.stat-row { margin-bottom: 16px; }
.stat-card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .stat-title { font-size: 14px; color: #8c8c8c; } .stat-value { font-size: 32px; font-weight: 600; margin: 8px 0; } .stat-desc { font-size: 12px; color: #bfbfbf; } }
.chart-container { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } .chart { height: 300px; } }
.table-container { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } }
</style>
