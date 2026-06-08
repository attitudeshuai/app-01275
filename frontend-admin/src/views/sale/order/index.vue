<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>销售单号</label>
          <a-input v-model:value="searchForm.orderNo" placeholder="输入单号搜索" allow-clear @pressEnter="fetchData" />
        </div>
        <div class="search-item">
          <label>状态</label>
          <a-select v-model:value="searchForm.status" placeholder="全部状态" allow-clear>
            <a-select-option :value="-1">报价中</a-select-option>
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已审核</a-select-option>
            <a-select-option :value="2">已发货</a-select-option>
            <a-select-option :value="3">已收款</a-select-option>
            <a-select-option :value="4">已取消</a-select-option>
          </a-select>
        </div>
        <div class="search-actions">
          <a-button type="primary" @click="fetchData">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
          <a-button @click="resetSearch">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <ShoppingOutlined />
          <span>销售订单</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            <span class="price">¥{{ record.totalAmount?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColors[record.status]">{{ statusTexts[record.status] }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-popconfirm v-if="record.status === -1" title="确认将此报价转为销售单？" @confirm="handleConfirmQuote(record)">
                <a-button type="link" size="small">确认报价</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="销售单详情" width="800px" :footer="null">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="销售单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="客户">{{ detail.customerName }}</a-descriptions-item>
        <a-descriptions-item label="出库仓库">{{ detail.warehouseName }}</a-descriptions-item>
        <a-descriptions-item label="销售员">{{ detail.salesmanName }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</a-descriptions-item>
        <a-descriptions-item label="状态"><a-tag :color="statusColors[detail.status]">{{ statusTexts[detail.status] }}</a-tag></a-descriptions-item>
        <a-descriptions-item label="总金额">¥{{ detail.totalAmount?.toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="已收金额">¥{{ detail.receivedAmount?.toFixed(2) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>商品明细</a-divider>
      <a-table :columns="itemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, ShoppingOutlined } from '@ant-design/icons-vue'
import { getSaleList, getSaleDetail, confirmQuote } from '@/api/sale'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detail = ref({})

const statusTexts = { '-1': '报价中', 0: '待审核', 1: '已审核', 2: '已发货', 3: '已收款', 4: '已取消' }
const statusColors = { '-1': 'orange', 0: 'processing', 1: 'success', 2: 'warning', 3: 'default', 4: 'error' }

const searchForm = reactive({ orderNo: '', status: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '销售单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '客户', dataIndex: 'customerName', key: 'customerName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const itemColumns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '单价', dataIndex: 'price', key: 'price' },
  { title: '金额', dataIndex: 'amount', key: 'amount' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSaleList({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load sale list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleDetail = async (record) => {
  try {
    const res = await getSaleDetail(record.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { logger.error('Failed to load sale detail', { error: e.message }) }
}

const handleConfirmQuote = async (record) => {
  try {
    await confirmQuote(record.id)
    message.success('报价已确认，已转为销售单')
    fetchData()
  } catch (e) { logger.error('Failed to confirm quote', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.price { font-weight: 500; color: #f5222d; }
</style>