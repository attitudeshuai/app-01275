<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>销售单号</label>
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
          <SendOutlined />
          <span>待发货销售单</span>
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
              <a-button type="link" size="small" style="color: #52c41a" @click="openShipModal(record)">发货</a-button>
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
        <a-descriptions-item label="出库仓库">{{ detail.warehouseName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="审核人">{{ detail.approverName }}</a-descriptions-item>
        <a-descriptions-item label="审核时间">{{ detail.approveTime }}</a-descriptions-item>
        <a-descriptions-item label="总金额">¥{{ detail.totalAmount?.toFixed(2) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>商品明细</a-divider>
      <a-table :columns="itemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id" />
    </a-modal>

    <!-- 发货弹窗 -->
    <a-modal v-model:open="shipVisible" title="发货出库" @ok="handleShip" :confirmLoading="shipLoading">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="销售单号">
          <span>{{ currentOrder.orderNo }}</span>
        </a-form-item>
        <a-form-item label="客户">
          <span>{{ currentOrder.customerName }}</span>
        </a-form-item>
        <a-form-item label="出库仓库" required>
          <a-select v-model:value="shipForm.warehouseId" placeholder="请选择出库仓库" style="width: 100%">
            <a-select-option v-for="w in warehouseList" :key="w.id" :value="w.id">{{ w.warehouseName }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, SendOutlined } from '@ant-design/icons-vue'
import { getSaleList, getSaleDetail, shipSale } from '@/api/sale'
import { getWarehouseList } from '@/api/base'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detail = ref({})
const shipVisible = ref(false)
const shipLoading = ref(false)
const currentOrder = ref({})
const warehouseList = ref([])
const shipForm = reactive({ warehouseId: null })

const searchForm = reactive({ orderNo: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '销售单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '客户', dataIndex: 'customerName', key: 'customerName' },
  { title: '出库仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '审核时间', dataIndex: 'approveTime', key: 'approveTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
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
    const res = await getSaleList({ page: pagination.current, size: pagination.pageSize, status: 1, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load approved sale list', { error: e.message }) }
  finally { loading.value = false }
}

const fetchWarehouseList = async () => {
  try {
    const res = await getWarehouseList()
    warehouseList.value = res.data
  } catch (e) { logger.error('Failed to load warehouse list', { error: e.message }) }
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
    const res = await getSaleDetail(record.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { logger.error('Failed to load sale detail', { error: e.message }) }
}

const openShipModal = (record) => {
  currentOrder.value = record
  shipForm.warehouseId = record.warehouseId || (warehouseList.value.length > 0 ? warehouseList.value[0].id : null)
  shipVisible.value = true
}

const handleShip = async () => {
  if (!shipForm.warehouseId) {
    message.warning('请选择出库仓库')
    return
  }
  shipLoading.value = true
  try {
    await shipSale(currentOrder.value.id, shipForm.warehouseId)
    message.success('发货成功')
    shipVisible.value = false
    fetchData()
  } catch (e) { 
    message.error(e.response?.data?.message || '发货失败')
    logger.error('Failed to ship sale', { error: e.message }) 
  }
  finally { shipLoading.value = false }
}

onMounted(() => {
  fetchData()
  fetchWarehouseList()
})
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.price { font-weight: 500; color: #f5222d; }
</style>