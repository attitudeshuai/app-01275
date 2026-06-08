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

    <div class="chart-container">
      <div class="chart-title">采购趋势</div>
      <div ref="chartRef" class="chart"></div>
    </div>

    <div class="table-container">
      <div class="chart-title">采购明细</div>
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="date" @change="handleTableChange">
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
import { getPurchaseTrend } from '@/api/report'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const dateRange = ref([dayjs().subtract(30, 'day'), dayjs()])
const chartRef = ref()
let chart = null

const pagination = ref({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '日期', dataIndex: 'date', key: 'date' },
  { title: '采购单数', dataIndex: 'orderCount', key: 'orderCount' },
  { title: '采购金额', key: 'totalAmount' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value || []
    const res = await getPurchaseTrend({
      startDate: startDate?.format('YYYY-MM-DD'),
      endDate: endDate?.format('YYYY-MM-DD')
    })
    tableData.value = res.data
    pagination.value.total = res.data.length
    initChart(res.data)
  } catch (e) { logger.error('Failed to load purchase trend', { error: e.message }) }
  finally { loading.value = false }
}

const initChart = (data) => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['采购金额', '采购单数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.map(d => d.date) },
    yAxis: [{ type: 'value', name: '金额' }, { type: 'value', name: '单数' }],
    series: [
      { name: '采购金额', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: data.map(d => d.totalAmount) },
      { name: '采购单数', type: 'bar', yAxisIndex: 1, data: data.map(d => d.orderCount) }
    ]
  })
}

const handleTableChange = (pag) => { pagination.value.current = pag.current; pagination.value.pageSize = pag.pageSize }
const handleResize = () => { chart?.resize() }

onMounted(() => { fetchData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); chart?.dispose() })
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.search-form { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.chart-container { background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } .chart { height: 350px; } }
.table-container { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; } }
</style>
