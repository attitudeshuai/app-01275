<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>盘点单号</label>
          <a-input 
            v-model:value="searchForm.checkNo" 
            placeholder="输入单号搜索" 
            allow-clear
            @pressEnter="fetchData"
          >
            <template #prefix><FileTextOutlined style="color: #bfbfbf" /></template>
          </a-input>
        </div>
        <div class="search-item">
          <label>仓库</label>
          <a-select 
            v-model:value="searchForm.warehouseId" 
            placeholder="全部仓库" 
            allow-clear 
            :options="warehouseList" 
            :field-names="{ label: 'warehouseName', value: 'id' }"
          />
        </div>
        <div class="search-item">
          <label>状态</label>
          <a-select v-model:value="searchForm.status" placeholder="全部状态" allow-clear>
            <a-select-option :value="0">进行中</a-select-option>
            <a-select-option :value="1">已完成</a-select-option>
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
          <InboxOutlined />
          <span>盘点记录</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新建盘点
        </a-button>
      </div>
      
      <a-table 
        :columns="columns" 
        :data-source="tableData" 
        :loading="loading" 
        :pagination="pagination" 
        row-key="id" 
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'checkNo'">
            <span class="order-no">{{ record.checkNo }}</span>
          </template>
          <template v-if="column.key === 'warehouseName'">
            <div class="warehouse-cell">
              <HomeOutlined />
              <span>{{ record.warehouseName }}</span>
            </div>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 0 ? 'processing' : 'success'" class="status-tag">
              <template #icon>
                <SyncOutlined v-if="record.status === 0" spin />
                <CheckCircleOutlined v-else />
              </template>
              {{ record.status === 0 ? '进行中' : '已完成' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a-button type="link" size="small" @click="handleDetail(record)">
                <EyeOutlined /> 详情
              </a-button>
              <a-popconfirm v-if="record.status === 0" title="确定要完成盘点吗？" @confirm="handleFinish(record)">
                <a-button type="link" size="small" class="finish-btn">
                  <CheckOutlined /> 完成
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
        
        <template #emptyText>
          <div class="empty-state">
            <InboxOutlined />
            <p>暂无盘点记录</p>
          </div>
        </template>
      </a-table>
    </div>

    <!-- 新建盘点弹窗 -->
    <a-modal v-model:open="addVisible" title="新建盘点" width="900px" @ok="submitAdd" :confirm-loading="addLoading">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="仓库" required>
          <a-select 
            v-model:value="addForm.warehouseId" 
            placeholder="请选择仓库" 
            style="width: 300px" 
            :options="warehouseList" 
            :field-names="{ label: 'warehouseName', value: 'id' }" 
            @change="loadStockForCheck" 
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="addForm.remark" placeholder="请输入备注" style="width: 300px" />
        </a-form-item>
      </a-form>
      <a-alert v-if="addForm.warehouseId && addForm.items.length === 0" type="warning" show-icon style="margin-bottom: 16px">
        <template #message>该仓库暂无库存数据，请先通过采购入库添加商品</template>
      </a-alert>
      <a-table :columns="checkItemColumns" :data-source="addForm.items" :pagination="false" size="small" row-key="goodsId" :loading="stockLoading">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actualQty'">
            <a-input-number v-model:value="record.actualQty" :min="0" @change="calcDiff(record)" />
          </template>
          <template v-if="column.key === 'diffQty'">
            <span :class="['diff-value', { negative: record.diffQty < 0, positive: record.diffQty > 0 }]">
              {{ record.diffQty > 0 ? '+' : '' }}{{ record.diffQty }}
            </span>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="盘点详情" width="900px" :footer="null">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="盘点单号">{{ detail.checkNo }}</a-descriptions-item>
        <a-descriptions-item label="仓库">{{ detail.warehouseName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="detail.status === 0 ? 'processing' : 'success'">
            {{ detail.status === 0 ? '进行中' : '已完成' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>盘点明细</a-divider>
      <a-table :columns="detailItemColumns" :data-source="detail.items" :pagination="false" size="small" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'diffQty'">
            <span :class="['diff-value', { negative: record.diffQty < 0, positive: record.diffQty > 0 }]">
              {{ record.diffQty > 0 ? '+' : '' }}{{ record.diffQty }}
            </span>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  SearchOutlined, ReloadOutlined, PlusOutlined, FileTextOutlined,
  InboxOutlined, HomeOutlined, SyncOutlined, CheckCircleOutlined,
  EyeOutlined, CheckOutlined
} from '@ant-design/icons-vue'
import { getStockCheckPage, getStockCheckDetail, createStockCheck, confirmStockCheck, getStockPage } from '@/api/stock'
import { getWarehouseList } from '@/api/base'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const addLoading = ref(false)
const stockLoading = ref(false)
const tableData = ref([])
const warehouseList = ref([])
const addVisible = ref(false)
const detailVisible = ref(false)
const detail = ref({})

const searchForm = reactive({ checkNo: '', warehouseId: null, status: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const addForm = reactive({ warehouseId: null, remark: '', items: [] })

const columns = [
  { title: '盘点单号', dataIndex: 'checkNo', key: 'checkNo', width: 180 },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '完成时间', dataIndex: 'finishTime', key: 'finishTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const checkItemColumns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '系统数量', dataIndex: 'systemQty', key: 'systemQty', width: 100 },
  { title: '实际数量', dataIndex: 'actualQty', key: 'actualQty', width: 120 },
  { title: '差异', dataIndex: 'diffQty', key: 'diffQty', width: 100 }
]

const detailItemColumns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '系统数量', dataIndex: 'systemQty', key: 'systemQty', width: 100 },
  { title: '实际数量', dataIndex: 'actualQty', key: 'actualQty', width: 100 },
  { title: '差异', dataIndex: 'diffQty', key: 'diffQty', width: 100 }
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
    const res = await getStockCheckPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load stock check list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.checkNo = ''
  searchForm.warehouseId = null
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  addForm.warehouseId = null
  addForm.remark = ''
  addForm.items = []
  addVisible.value = true
}

const loadStockForCheck = async () => {
  if (!addForm.warehouseId) return
  stockLoading.value = true
  try {
    const res = await getStockPage({ page: 1, size: 1000, warehouseId: addForm.warehouseId })
    addForm.items = res.data.records.map(s => ({
      goodsId: s.goodsId, goodsCode: s.goodsCode, goodsName: s.goodsName,
      systemQty: s.quantity, actualQty: s.quantity, diffQty: 0
    }))
  } catch (e) { logger.error('Failed to load stock for check', { error: e.message }) }
  finally { stockLoading.value = false }
}

const calcDiff = (record) => { record.diffQty = (record.actualQty || 0) - record.systemQty }

const submitAdd = async () => {
  if (!addForm.warehouseId) { message.warning('请选择仓库'); return }
  if (addForm.items.length === 0) { message.warning('该仓库没有库存数据'); return }
  addLoading.value = true
  try {
    await createStockCheck(addForm)
    message.success('盘点单创建成功')
    addVisible.value = false
    fetchData()
  } catch (e) { logger.error('Failed to create stock check', { error: e.message }) }
  finally { addLoading.value = false }
}

const handleDetail = async (record) => {
  try {
    const res = await getStockCheckDetail(record.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { logger.error('Failed to load stock check detail', { error: e.message }) }
}

const handleFinish = async (record) => {
  try {
    await confirmStockCheck(record.id)
    message.success('盘点完成')
    fetchData()
  } catch (e) { logger.error('Failed to finish stock check', { error: e.message }) }
}

onMounted(() => { fetchWarehouseList(); fetchData() })
</script>

<style lang="scss" scoped>
.page-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.search-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  
  .search-item {
    display: flex;
    align-items: center;
    gap: 8px;
    
    label {
      font-size: 14px;
      color: #666;
      font-weight: 500;
      white-space: nowrap;
    }
    
    :deep(.ant-input-affix-wrapper),
    :deep(.ant-select) {
      width: 180px !important;
    }
    
    :deep(.ant-select-selector) {
      border-radius: 6px !important;
      height: 32px !important;
    }
    
    :deep(.ant-input-affix-wrapper) {
      border-radius: 6px;
      height: 32px !important;
    }
  }
  
  .search-actions {
    display: flex;
    gap: 8px;
    margin-left: auto;
    
    .ant-btn {
      border-radius: 6px;
      height: 32px;
      
      &.ant-btn-primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
        color: #fff !important;
      }
    }
  }
}

.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
    
    .anticon {
      color: #667eea;
    }
    
    .ant-tag {
      margin-left: 4px;
      border-radius: 10px;
      font-size: 12px;
    }
  }
  
  .ant-btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    height: 36px;
    box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
  }
}

.order-no {
  font-family: 'SF Mono', Monaco, monospace;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
}

.warehouse-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .anticon {
    color: #94a3b8;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border-radius: 6px;
  padding: 2px 10px;
}

.action-btns {
  display: flex;
  gap: 4px;
  
  .ant-btn-link {
    padding: 0 8px;
    height: 28px;
    font-size: 13px;
    
    .anticon {
      margin-right: 4px;
    }
  }
  
  .finish-btn {
    color: #52c41a;
  }
}

.diff-value {
  font-weight: 600;
  
  &.negative {
    color: #ef4444;
  }
  
  &.positive {
    color: #22c55e;
  }
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #94a3b8;
  
  .anticon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

:deep(.ant-table) {
  .ant-table-thead > tr > th {
    background: #f8fafc;
    font-weight: 600;
    color: #475569;
    border-bottom: 1px solid #e2e8f0;
  }
  
  .ant-table-tbody > tr > td {
    border-bottom: 1px solid #f1f5f9;
  }
  
  .ant-table-tbody > tr:hover > td {
    background: #f8fafc;
  }
}
</style>
