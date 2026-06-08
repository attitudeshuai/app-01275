<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>操作人</label>
          <a-input v-model:value="searchForm.username" placeholder="输入操作人搜索" allow-clear @pressEnter="fetchData" />
        </div>
        <div class="search-item">
          <label>模块</label>
          <a-input v-model:value="searchForm.module" placeholder="输入模块搜索" allow-clear @pressEnter="fetchData" />
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
          <FileTextOutlined />
          <span>操作日志</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'params'">
            <a-tooltip :title="record.params">
              <span class="ellipsis">{{ record.params }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.key === 'execTime'">
            <a-tag :color="record.execTime > 1000 ? 'error' : record.execTime > 500 ? 'warning' : 'success'">
              {{ record.execTime }}ms
            </a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined, ReloadOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { getLogList } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({ username: '', module: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '操作人', dataIndex: 'username', key: 'username', width: 100 },
  { title: '模块', dataIndex: 'module', key: 'module', width: 100 },
  { title: '操作', dataIndex: 'operation', key: 'operation', width: 100 },
  { title: '方法', dataIndex: 'method', key: 'method', ellipsis: true },
  { title: '参数', dataIndex: 'params', key: 'params', ellipsis: true },
  { title: 'IP', dataIndex: 'ip', key: 'ip', width: 120 },
  { title: '耗时', dataIndex: 'execTime', key: 'execTime', width: 80 },
  { title: '操作时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLogList({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load log list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.module = ''
  pagination.current = 1
  fetchData()
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
.ellipsis { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; }
</style>
