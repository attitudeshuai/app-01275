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
          <AuditOutlined />
          <span>待审核销售单</span>
          <a-tag color="orange">{{ pagination.total }} 条</a-tag>
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
              <a-button type="link" size="small" style="color: #52c41a" @click="handleApprove(record, true)">通过</a-button>
              <a-button type="link" size="small" danger @click="handleApprove(record, false)">拒绝</a-button>
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
        <a-descriptions-item label="总金额">¥{{ detail.totalAmount?.toFixed(2) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>商品明细</a-divider>
      <a-table :columns="itemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id" />
    </a-modal>

    <!-- 审核弹窗 -->
    <a-modal v-model:open="approveVisible" :title="approvePass ? '审核通过' : '审核拒绝'" @ok="submitApprove" :confirm-loading="approveLoading">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="审核备注">
          <a-textarea v-model:value="approveRemark" :rows="3" placeholder="请输入审核备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, AuditOutlined } from '@ant-design/icons-vue'
import { getSaleList, getSaleDetail, approveSale } from '@/api/sale'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const approveLoading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const approveVisible = ref(false)
const detail = ref({})
const currentId = ref(null)
const approvePass = ref(true)
const approveRemark = ref('')

const searchForm = reactive({ orderNo: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '销售单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '客户', dataIndex: 'customerName', key: 'customerName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '销售员', dataIndex: 'salesmanName', key: 'salesmanName' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
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
    const res = await getSaleList({ page: pagination.current, size: pagination.pageSize, status: 0, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load pending sale list', { error: e.message }) }
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
    const res = await getSaleDetail(record.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { logger.error('Failed to load sale detail', { error: e.message }) }
}

const handleApprove = (record, pass) => {
  currentId.value = record.id
  approvePass.value = pass
  approveRemark.value = ''
  approveVisible.value = true
}

const submitApprove = async () => {
  approveLoading.value = true
  try {
    await approveSale(currentId.value, approvePass.value, approveRemark.value)
    message.success(approvePass.value ? '审核通过' : '审核拒绝')
    approveVisible.value = false
    fetchData()
  } catch (e) { logger.error('Failed to approve sale', { error: e.message }) }
  finally { approveLoading.value = false }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.price { font-weight: 500; color: #f5222d; }
</style>