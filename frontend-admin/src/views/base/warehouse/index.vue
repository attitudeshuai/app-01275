<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>仓库名称</label>
          <a-input 
            v-model:value="searchForm.warehouseName" 
            placeholder="输入名称搜索" 
            allow-clear
            @pressEnter="fetchData"
          />
        </div>
        <div class="search-item">
          <label>状态</label>
          <a-select v-model:value="searchForm.status" placeholder="全部状态" allow-clear>
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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
          <HomeOutlined />
          <span>仓库列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增仓库
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
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定要删除该仓库吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="dialogVisible" :title="dialogTitle" @ok="handleSubmit" :confirm-loading="submitLoading" width="520px">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="padding: 20px 0;">
        <a-form-item label="仓库编码" name="warehouseCode">
          <a-input v-model:value="form.warehouseCode" placeholder="请输入仓库编码" />
        </a-form-item>
        <a-form-item label="仓库名称" name="warehouseName">
          <a-input v-model:value="form.warehouseName" placeholder="请输入仓库名称" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.manager" placeholder="请输入负责人" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.phone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="地址">
          <a-input v-model:value="form.address" placeholder="请输入地址" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined, HomeOutlined } from '@ant-design/icons-vue'
import { getWarehousePage, saveWarehouse, updateWarehouse, deleteWarehouse } from '@/api/base'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({ warehouseName: '', status: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const form = reactive({ id: null, warehouseCode: '', warehouseName: '', manager: '', phone: '', address: '', status: 1 })

const rules = {
  warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
}

const columns = [
  { title: '仓库编码', dataIndex: 'warehouseCode', key: 'warehouseCode', width: 120 },
  { title: '仓库名称', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: '负责人', dataIndex: 'manager', key: 'manager', width: 100 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getWarehousePage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load warehouse list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.warehouseName = ''
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
  dialogTitle.value = '新增仓库'
  Object.assign(form, { id: null, warehouseCode: '', warehouseName: '', manager: '', phone: '', address: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑仓库'
  Object.assign(form, record)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateWarehouse(form)
      message.success('更新成功')
    } else {
      await saveWarehouse(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save warehouse', { error: e.message })
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    await deleteWarehouse(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete warehouse', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>
