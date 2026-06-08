<template>
  <div class="page-container">
    <!-- 操作区域 -->
    <div class="search-card">
      <div class="search-form">
        <a-button type="primary" @click="handleBackup" :loading="backupLoading">
          <template #icon><CloudUploadOutlined /></template>
          立即备份
        </a-button>
        <a-alert message="系统每天凌晨2点自动备份数据库" type="info" show-icon style="margin-left: 16px" />
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <DatabaseOutlined />
          <span>备份列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fileSize'">{{ formatFileSize(record.fileSize) }}</template>
          <template v-if="column.key === 'backupType'">
            <a-tag :color="record.backupType === 1 ? 'blue' : 'green'">{{ record.backupType === 1 ? '自动备份' : '手动备份' }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">{{ record.status === 1 ? '成功' : '失败' }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-popconfirm title="确定要恢复到此备份吗？此操作将覆盖当前数据！" @confirm="handleRestore(record)">
                <a-button type="link" size="small">恢复</a-button>
              </a-popconfirm>
              <a-popconfirm title="确定要删除此备份吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { CloudUploadOutlined, DatabaseOutlined } from '@ant-design/icons-vue'
import { getBackupList, createBackup, restoreBackup, deleteBackup } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const backupLoading = ref(false)
const tableData = ref([])

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const columns = [
  { title: '备份名称', dataIndex: 'fileName', key: 'fileName' },
  { title: '文件大小', dataIndex: 'fileSize', key: 'fileSize', width: 120 },
  { title: '备份类型', dataIndex: 'backupType', key: 'backupType', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBackupList({ page: pagination.current, size: pagination.pageSize })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { 
    message.error('加载备份列表失败')
    logger.error('Failed to load backup list', { error: e.message }) 
  }
  finally { loading.value = false }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleBackup = async () => {
  backupLoading.value = true
  try {
    await createBackup()
    message.success('备份成功')
    fetchData()
  } catch (e) { logger.error('Failed to create backup', { error: e.message }) }
  finally { backupLoading.value = false }
}

const handleRestore = async (record) => {
  try {
    await restoreBackup(record.id)
    message.success('恢复成功')
  } catch (e) { logger.error('Failed to restore backup', { error: e.message }) }
}

const handleDelete = async (record) => {
  try {
    await deleteBackup(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete backup', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>
