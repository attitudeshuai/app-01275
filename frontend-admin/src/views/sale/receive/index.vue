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
          <DollarOutlined />
          <span>待收款销售单</span>
          <a-tag color="cyan">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            <span class="price">¥{{ record.totalAmount?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'receivedAmount'">
            <span style="color: #52c41a; font-weight: 500">¥{{ record.receivedAmount?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'unpaidAmount'">
            <span :style="{ color: (record.totalAmount - record.receivedAmount) > 0 ? '#f5222d' : '#52c41a', fontWeight: 500 }">
              ¥{{ (record.totalAmount - record.receivedAmount)?.toFixed(2) }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-button type="link" size="small" style="color: #52c41a" @click="handleReceive(record)">收款</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="销售单详情" width="900px" :footer="null">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="销售单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="客户">{{ detail.customerName }}</a-descriptions-item>
        <a-descriptions-item label="所属部门">{{ detail.deptName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="发货时间">{{ formatDateTime(detail.shipTime) }}</a-descriptions-item>
        <a-descriptions-item label="总金额"><span style="font-weight: 600">¥{{ detail.totalAmount?.toFixed(2) }}</span></a-descriptions-item>
        <a-descriptions-item label="已收金额"><span style="color: #52c41a">¥{{ detail.receivedAmount?.toFixed(2) }}</span></a-descriptions-item>
      </a-descriptions>
      <a-divider>商品明细</a-divider>
      <a-table :columns="itemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id" />
      <a-divider>收款记录</a-divider>
      <a-table :columns="paymentColumns" :data-source="detail.paymentRecords" :pagination="false" size="small" row-key="id" :locale="{ emptyText: '暂无收款记录' }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'"><span style="color: #52c41a; font-weight: 500">¥{{ record.amount?.toFixed(2) }}</span></template>
          <template v-if="column.key === 'paymentMethod'"><a-tag :color="paymentMethodColors[record.paymentMethod]">{{ paymentMethodTexts[record.paymentMethod] }}</a-tag></template>
        </template>
      </a-table>
    </a-modal>

    <!-- 收款弹窗 -->
    <a-modal v-model:open="receiveVisible" title="收款" @ok="submitReceive" :confirm-loading="receiveLoading">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="应收金额"><span style="font-size: 18px; font-weight: 600; color: #f5222d">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span></a-form-item>
        <a-form-item label="已收金额"><span style="color: #52c41a">¥{{ currentOrder.receivedAmount?.toFixed(2) }}</span></a-form-item>
        <a-form-item label="待收金额"><span style="color: #faad14; font-weight: 500">¥{{ (currentOrder.totalAmount - currentOrder.receivedAmount)?.toFixed(2) }}</span></a-form-item>
        <a-form-item label="本次收款" required>
          <a-input-number v-model:value="receiveForm.amount" :min="0.01" :max="currentOrder.totalAmount - currentOrder.receivedAmount" :precision="2" style="width: 200px" />
        </a-form-item>
        <a-form-item label="收款方式" required>
          <a-select v-model:value="receiveForm.paymentMethod" style="width: 200px">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="BANK">银行转账</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信支付</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注"><a-textarea v-model:value="receiveForm.remark" :rows="2" placeholder="请输入收款备注" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, DollarOutlined } from '@ant-design/icons-vue'
import { getSalePage, getSaleDetail, receiveSale } from '@/api/sale'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const receiveLoading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const receiveVisible = ref(false)
const detail = ref({})
const currentOrder = ref({})

const searchForm = reactive({ orderNo: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const receiveForm = reactive({ amount: 0, paymentMethod: 'BANK', remark: '' })

const paymentMethodTexts = { CASH: '现金', BANK: '银行转账', ALIPAY: '支付宝', WECHAT: '微信支付' }
const paymentMethodColors = { CASH: 'green', BANK: 'blue', ALIPAY: 'cyan', WECHAT: 'lime' }

const columns = [
  { title: '销售单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '客户', dataIndex: 'customerName', key: 'customerName' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '已收金额', dataIndex: 'receivedAmount', key: 'receivedAmount', width: 120 },
  { title: '待收金额', key: 'unpaidAmount', width: 120 },
  { title: '发货时间', dataIndex: 'shipTime', key: 'shipTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const itemColumns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '单价', dataIndex: 'price', key: 'price' },
  { title: '金额', dataIndex: 'amount', key: 'amount' }
]

const paymentColumns = [
  { title: '收款单号', dataIndex: 'paymentNo', key: 'paymentNo' },
  { title: '收款金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '收款方式', dataIndex: 'paymentMethod', key: 'paymentMethod', width: 100 },
  { title: '操作人', dataIndex: 'operatorName', key: 'operatorName', width: 100 },
  { title: '收款时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSalePage({ page: pagination.current, size: pagination.pageSize, status: 2, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load shipped sale list', { error: e.message }) }
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

const handleReceive = (record) => {
  currentOrder.value = record
  receiveForm.amount = record.totalAmount - record.receivedAmount
  receiveForm.paymentMethod = 'BANK'
  receiveForm.remark = ''
  receiveVisible.value = true
}

const submitReceive = async () => {
  if (receiveForm.amount <= 0) { message.warning('收款金额必须大于0'); return }
  receiveLoading.value = true
  try {
    await receiveSale(currentOrder.value.id, receiveForm.amount, receiveForm.paymentMethod, receiveForm.remark)
    message.success('收款成功')
    receiveVisible.value = false
    fetchData()
  } catch (e) { logger.error('Failed to receive sale', { error: e.message }) }
  finally { receiveLoading.value = false }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.price { font-weight: 500; color: #f5222d; }
</style>