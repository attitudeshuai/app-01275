<template>
  <div class="page-container">
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <AlertOutlined />
          <span>库存预警</span>
          <a-tag color="orange">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>
      
      <a-tabs v-model:activeKey="activeTab" @change="fetchData">
        <a-tab-pane key="low" tab="库存不足" />
        <a-tab-pane key="high" tab="库存过高" />
      </a-tabs>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="activeTab === 'low' ? 'error' : 'warning'">
              {{ activeTab === 'low' ? '库存不足' : '库存过高' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'diff'">
            <span :style="{ color: activeTab === 'low' ? '#f5222d' : '#fa8c16', fontWeight: 500 }">
              {{ activeTab === 'low' ? record.quantity - record.safetyStock : record.quantity - record.maxStock }}
            </span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { AlertOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getStockWarningList } from '@/api/stock'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('low')

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '当前库存', dataIndex: 'quantity', key: 'quantity' },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock' },
  { title: '最大库存', dataIndex: 'maxStock', key: 'maxStock' },
  { title: '差异', dataIndex: 'diff', key: 'diff', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockWarningList({ page: pagination.current, size: pagination.pageSize, type: activeTab.value })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { 
    message.error('加载数据失败')
    logger.error('Failed to load stock warning list', { error: e.message }) 
  }
  finally { loading.value = false }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>
