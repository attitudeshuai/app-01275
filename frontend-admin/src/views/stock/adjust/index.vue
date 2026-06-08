<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>调整单号</label>
          <a-input v-model:value="searchForm.adjustNo" placeholder="输入单号搜索" allow-clear @pressEnter="fetchData" />
        </div>
        <div class="search-item">
          <label>类型</label>
          <a-select v-model:value="searchForm.adjustType" placeholder="全部类型" allow-clear>
            <a-select-option value="LOSS">报损</a-select-option>
            <a-select-option value="OVERFLOW">报溢</a-select-option>
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
          <ToolOutlined />
          <span>库存调整</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-space>
          <a-button type="primary" danger @click="handleAdd('LOSS')">
            <template #icon><MinusCircleOutlined /></template>
            报损
          </a-button>
          <a-button type="primary" @click="handleAdd('OVERFLOW')">
            <template #icon><PlusCircleOutlined /></template>
            报溢
          </a-button>
        </a-space>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'adjustType'">
            <a-tag :color="record.adjustType === 'LOSS' ? 'error' : 'success'">{{ record.adjustType === 'LOSS' ? '报损' : '报溢' }}</a-tag>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 调整弹窗 -->
    <a-modal v-model:open="addVisible" :title="addForm.adjustType === 'LOSS' ? '报损' : '报溢'" @ok="submitAdd" :confirm-loading="addLoading">
      <a-form ref="formRef" :model="addForm" :rules="rules" :label-col="{ span: 6 }">
        <a-form-item label="仓库" name="warehouseId">
          <a-select v-model:value="addForm.warehouseId" placeholder="请选择仓库" :options="warehouseList" :field-names="{ label: 'warehouseName', value: 'id' }" @change="loadStockForAdjust" />
        </a-form-item>
        <a-form-item label="商品" name="goodsId">
          <a-select v-model:value="addForm.goodsId" placeholder="请选择商品" :options="stockList" :field-names="{ label: 'goodsName', value: 'goodsId' }" @change="onGoodsChange" />
        </a-form-item>
        <a-form-item label="当前库存">
          <span style="font-weight: 500; color: #1890ff">{{ currentStock?.quantity || 0 }}</span>
        </a-form-item>
        <a-form-item label="调整数量" name="quantity">
          <a-input-number v-model:value="addForm.quantity" :min="1" :max="addForm.adjustType === 'LOSS' ? (currentStock?.quantity || 1) : 99999" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因" name="reason">
          <a-textarea v-model:value="addForm.reason" :rows="3" placeholder="请输入原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, MinusCircleOutlined, PlusCircleOutlined, ToolOutlined } from '@ant-design/icons-vue'
import { getStockAdjustPage, createStockAdjust, getStockPage } from '@/api/stock'
import { getWarehouseList } from '@/api/base'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const addLoading = ref(false)
const tableData = ref([])
const warehouseList = ref([])
const stockList = ref([])
const addVisible = ref(false)
const formRef = ref()

const searchForm = reactive({ adjustNo: '', adjustType: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const addForm = reactive({ warehouseId: null, goodsId: null, adjustType: 'LOSS', quantity: 1, reason: '' })

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

const currentStock = computed(() => stockList.value.find(s => s.goodsId === addForm.goodsId))

const columns = [
  { title: '调整单号', dataIndex: 'adjustNo', key: 'adjustNo' },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '商品', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '类型', dataIndex: 'adjustType', key: 'adjustType', width: 80 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) }
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
    const res = await getStockAdjustPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load stock adjust list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.adjustNo = ''
  searchForm.adjustType = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = (type) => {
  Object.assign(addForm, { warehouseId: null, goodsId: null, adjustType: type, quantity: 1, reason: '' })
  stockList.value = []
  addVisible.value = true
}

const loadStockForAdjust = async () => {
  if (!addForm.warehouseId) return
  addForm.goodsId = null
  try {
    const res = await getStockPage({ page: 1, size: 1000, warehouseId: addForm.warehouseId })
    stockList.value = addForm.adjustType === 'LOSS' ? res.data.records.filter(s => s.quantity > 0) : res.data.records
  } catch (e) { logger.error('Failed to load stock for adjust', { error: e.message }) }
}

const onGoodsChange = () => { addForm.quantity = 1 }

const submitAdd = async () => {
  try {
    await formRef.value.validate()
    addLoading.value = true
    await createStockAdjust(addForm)
    message.success(addForm.adjustType === 'LOSS' ? '报损成功' : '报溢成功')
    addVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to create stock adjust', { error: e.message })
  } finally { addLoading.value = false }
}

onMounted(() => { fetchWarehouseList(); fetchData() })
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>