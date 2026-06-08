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

    <a-row :gutter="16">
      <a-col :span="12">
        <div class="chart-container">
          <div class="chart-title">销售额排行 TOP10</div>
          <div ref="amountChartRef" class="chart"></div>
        </div>
      </a-col>
      <a-col :span="12">
        <div class="chart-container">
          <div class="chart-title">销售量排行 TOP10</div>
          <div ref="quantityChartRef" class="chart"></div>
        </div>
      </a-col>
    </a-row>

    <div class="table-container">
      <div class="chart-title">销售明细</div>
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="goodsId" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">¥{{ record.totalAmount?.toFixed(2) }}</template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getSaleRank } from '@/api/report'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const dateRange = ref([dayjs().subtract(30, 'day'), dayjs()])
const amountChartRef = ref()
const quantityChartRef = ref()
let amountChart = null
let quantityChart = null

const pagination = ref({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', width: 60 },
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '销售数量', dataIndex: 'totalQuantity', key: 'totalQuantity' },
  { title: '销售金额', key: 'totalAmount' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value || []
    const res = await getSaleRank({
      startDate: startDate?.format('YYYY-MM-DD'),
      endDate: endDate?.format('YYYY-MM-DD'),
      limit: 100
    })
    const data = res.data || []
    tableData.value = data.map((item, index) => ({ ...item, rank: index + 1 }))
    pagination.value.total = data.length
    initCharts(data.slice(0, 10))
  } catch (e) { logger.error('Failed to load sale rank', { error: e.message }) }
  finally { loading.value = false }
}

const initCharts = (data) => {
  if (!amountChartRef.value || !quantityChartRef.value) return
  
  if (!amountChart) amountChart = echarts.init(amountChartRef.value)
  if (!quantityChart) quantityChart = echarts.init(quantityChartRef.value)

  const names = data.map(d => d.goodsName).reverse()
  const amounts = data.map(d => d.totalAmount).reverse()
  const quantities = data.map(d => d.totalQuantity).reverse()

  amountChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names },
    series: [{ type: 'bar', data: amounts, itemStyle: { color: '#1890ff' } }]
  })

  quantityChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names },
    series: [{ type: 'bar', data: quantities, itemStyle: { color: '#52c41a' } }]
  })
}

const handleTableChange = (pag) => { pagination.value.current = pag.current; pagination.value.pageSize = pag.pageSize; fetchData() }
const handleResize = () => { amountChart?.resize(); quantityChart?.resize() }

onMounted(() => { fetchData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); amountChart?.dispose(); quantityChart?.dispose() })
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.search-form { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.chart-container { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } .chart { height: 300px; } }
.table-container { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } }
</style>
