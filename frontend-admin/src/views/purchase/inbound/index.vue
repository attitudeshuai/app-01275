<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>采购单号</label>
          <a-input v-model:value="searchForm.orderNo" placeholder="输入单号搜索" allow-clear @pressEnter="fetchData" />
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
          <ImportOutlined />
          <span>待入库采购单</span>
          <a-tag color="green">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            <span class="price">¥{{ record.totalAmount?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-popconfirm title="确定要执行入库操作吗？" @confirm="handleInbound(record)">
                <a-button type="link" size="small" style="color: #52c41a">入库</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="采购单详情" width="800px" :footer="null">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="采购单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="供应商">{{ detail.supplierName }}</a-descriptions-item>
        <a-descriptions-item label="入库仓库">{{ detail.warehouseName }}</a-descriptions-item>
        <a-descriptions-item label="审批人">{{ detail.approverName }}</a-descriptions-item>
        <a-descriptions-item label="审批时间">{{ formatDateTime(detail.approveTime) }}</a-descriptions-item>
        <a-descriptions-item label="总金额">¥{{ detail.totalAmount?.toFixed(2) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>商品明细</a-divider>
      <a-table :columns="itemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, ImportOutlined } from '@ant-design/icons-vue'
import { getPurchasePage, getPurchaseDetail, inboundPurchase } from '@/api/purchase'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detail = ref({})

const searchForm = reactive({ orderNo: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '供应商', dataIndex: 'supplierName', key: 'supplierName' },
  { title: '入库仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '审批时间', dataIndex: 'approveTime', key: 'approveTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
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
    const res = await getPurchasePage({ page: pagination.current, size: pagination.pageSize, status: 1, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load approved purchase list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.orderNo = ''
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
    const res = await getPurchaseDetail(record.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { logger.error('Failed to load purchase detail', { error: e.message }) }
}

const handleInbound = async (record) => {
  try {
    await inboundPurchase(record.id)
    message.success('入库成功')
    fetchData()
  } catch (e) { logger.error('Failed to inbound purchase', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.price { font-weight: 500; color: #f5222d; }
</style>
