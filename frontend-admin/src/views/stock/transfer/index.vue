<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>调拨单号</label>
          <a-input v-model:value="searchForm.transferNo" placeholder="输入单号搜索" allow-clear @pressEnter="fetchData" />
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
          <SwapOutlined />
          <span>调拨记录</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新建调拨
        </a-button>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange" />
    </div>

    <!-- 新建调拨弹窗 -->
    <a-modal v-model:open="addVisible" title="新建调拨" @ok="submitAdd" :confirm-loading="addLoading">
      <a-form ref="formRef" :model="addForm" :rules="rules" :label-col="{ span: 6 }">
        <a-form-item label="调出仓库" name="fromWarehouseId">
          <a-select v-model:value="addForm.fromWarehouseId" placeholder="请选择调出仓库" :options="warehouseList" :field-names="{ label: 'warehouseName', value: 'id' }" @change="loadStockForTransfer" />
        </a-form-item>
        <a-form-item label="调入仓库" name="toWarehouseId">
          <a-select v-model:value="addForm.toWarehouseId" placeholder="请选择调入仓库" :options="warehouseList.filter(w => w.id !== addForm.fromWarehouseId)" :field-names="{ label: 'warehouseName', value: 'id' }" />
        </a-form-item>
        <a-form-item label="商品" name="goodsId">
          <a-select v-model:value="addForm.goodsId" placeholder="请选择商品" :options="stockList" :field-names="{ label: 'goodsName', value: 'goodsId' }" @change="onGoodsChange" />
        </a-form-item>
        <a-form-item label="可调数量">
          <span style="font-weight: 500; color: #1890ff">{{ currentStock?.quantity || 0 }}</span>
        </a-form-item>
        <a-form-item label="调拨数量" name="quantity">
          <a-input-number v-model:value="addForm.quantity" :min="1" :max="currentStock?.quantity || 1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="addForm.remark" :rows="2" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined, SwapOutlined } from '@ant-design/icons-vue'
import { getStockTransferPage, createStockTransfer, getStockPage } from '@/api/stock'
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

const searchForm = reactive({ transferNo: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const addForm = reactive({ fromWarehouseId: null, toWarehouseId: null, goodsId: null, quantity: 1, remark: '' })

const rules = {
  fromWarehouseId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toWarehouseId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入调拨数量', trigger: 'blur' }]
}

const currentStock = computed(() => stockList.value.find(s => s.goodsId === addForm.goodsId))

const columns = [
  { title: '调拨单号', dataIndex: 'transferNo', key: 'transferNo' },
  { title: '调出仓库', dataIndex: 'fromWarehouseName', key: 'fromWarehouseName' },
  { title: '调入仓库', dataIndex: 'toWarehouseName', key: 'toWarehouseName' },
  { title: '商品', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
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
    const res = await getStockTransferPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load stock transfer list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.transferNo = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  Object.assign(addForm, { fromWarehouseId: null, toWarehouseId: null, goodsId: null, quantity: 1, remark: '' })
  stockList.value = []
  addVisible.value = true
}

const loadStockForTransfer = async () => {
  if (!addForm.fromWarehouseId) return
  addForm.goodsId = null
  try {
    const res = await getStockPage({ page: 1, size: 1000, warehouseId: addForm.fromWarehouseId })
    stockList.value = res.data.records.filter(s => s.quantity > 0)
  } catch (e) { logger.error('Failed to load stock for transfer', { error: e.message }) }
}

const onGoodsChange = () => { addForm.quantity = 1 }

const submitAdd = async () => {
  try {
    await formRef.value.validate()
    addLoading.value = true
    await createStockTransfer(addForm)
    message.success('调拨成功')
    addVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to create stock transfer', { error: e.message })
  } finally { addLoading.value = false }
}

onMounted(() => { fetchWarehouseList(); fetchData() })
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>