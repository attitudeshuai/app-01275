<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>商品名称</label>
          <a-input v-model:value="searchForm.goodsName" placeholder="输入名称搜索" allow-clear @pressEnter="fetchData" />
        </div>
        <div class="search-item">
          <label>仓库</label>
          <a-select v-model:value="searchForm.warehouseId" placeholder="全部仓库" allow-clear :options="warehouseList" :field-names="{ label: 'warehouseName', value: 'id' }" />
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
          <DatabaseOutlined />
          <span>库存列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.quantity < record.safetyStock" color="error">库存不足</a-tag>
            <a-tag v-else-if="record.quantity > record.maxStock" color="warning">库存过高</a-tag>
            <a-tag v-else color="success">正常</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleRecord(record)">出入库记录</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 出入库记录弹窗 -->
    <a-modal v-model:open="recordVisible" title="出入库记录" width="900px" :footer="null">
      <a-table :columns="recordColumns" :data-source="recordData" :pagination="recordPagination" size="small" row-key="id" @change="handleRecordTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'recordType'">
            <a-tag :color="record.recordType === 'IN' ? 'success' : 'error'">{{ record.recordType === 'IN' ? '入库' : '出库' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined, ReloadOutlined, DatabaseOutlined } from '@ant-design/icons-vue'
import { getStockPage, getStockRecords } from '@/api/stock'
import { getWarehouseList } from '@/api/base'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const warehouseList = ref([])
const recordVisible = ref(false)
const recordData = ref([])
const currentStock = ref({})

const searchForm = reactive({ goodsName: '', warehouseId: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const recordPagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '库存数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock' },
  { title: '最大库存', dataIndex: 'maxStock', key: 'maxStock' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const recordColumns = [
  { title: '类型', dataIndex: 'recordType', key: 'recordType', width: 80 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '变动前', dataIndex: 'beforeQty', key: 'beforeQty' },
  { title: '变动后', dataIndex: 'afterQty', key: 'afterQty' },
  { title: '关联单号', dataIndex: 'refNo', key: 'refNo' },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) }
]

const fetchWarehouseList = async () => {
  try {
    const res = await getWarehouseList()
    warehouseList.value = res.data
  } catch (e) { logger.error('Failed to load warehouse list', { error: e.message }) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load stock list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.goodsName = ''
  searchForm.warehouseId = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleRecord = async (record) => {
  currentStock.value = record
  recordPagination.current = 1
  await fetchRecords()
  recordVisible.value = true
}

const fetchRecords = async () => {
  try {
    const res = await getStockRecords({
      page: recordPagination.current,
      size: recordPagination.pageSize,
      goodsId: currentStock.value.goodsId,
      warehouseId: currentStock.value.warehouseId
    })
    recordData.value = res.data.records
    recordPagination.total = res.data.total
  } catch (e) { logger.error('Failed to load stock records', { error: e.message }) }
}

const handleRecordTableChange = (pag) => {
  recordPagination.current = pag.current
  fetchRecords()
}

onMounted(() => { fetchWarehouseList(); fetchData() })
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>