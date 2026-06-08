<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>客户名称</label>
          <a-input 
            v-model:value="searchForm.customerName" 
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
          <SolutionOutlined />
          <span>客户列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增客户
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
              <a-popconfirm title="确定要删除该客户吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="dialogVisible" :title="dialogTitle" @ok="handleSubmit" :confirm-loading="submitLoading" width="700px">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }" style="padding: 16px 0;">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="客户编码" name="customerCode">
              <a-input v-model:value="form.customerCode" placeholder="请输入客户编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="客户名称" name="customerName">
              <a-input v-model:value="form.customerName" placeholder="请输入客户名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="联系人">
              <a-input v-model:value="form.contact" placeholder="请输入联系人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话">
              <a-input v-model:value="form.phone" placeholder="请输入联系电话" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="地址" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
          <a-input v-model:value="form.address" placeholder="请输入地址" />
        </a-form-item>
        <a-form-item label="备注" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
          <a-textarea v-model:value="form.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
        <a-form-item label="状态" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
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
import { SearchOutlined, ReloadOutlined, PlusOutlined, SolutionOutlined } from '@ant-design/icons-vue'
import { getCustomerPage, saveCustomer, updateCustomer, deleteCustomer } from '@/api/base'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({ customerName: '', status: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const form = reactive({ id: null, customerCode: '', customerName: '', contact: '', phone: '', address: '', remark: '', status: 1 })

const rules = {
  customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
}

const columns = [
  { title: '客户编码', dataIndex: 'customerCode', key: 'customerCode', width: 120 },
  { title: '客户名称', dataIndex: 'customerName', key: 'customerName' },
  { title: '联系人', dataIndex: 'contact', key: 'contact', width: 100 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCustomerPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load customer list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.customerName = ''
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
  dialogTitle.value = '新增客户'
  Object.assign(form, { id: null, customerCode: '', customerName: '', contact: '', phone: '', address: '', remark: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑客户'
  Object.assign(form, record)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateCustomer(form)
      message.success('更新成功')
    } else {
      await saveCustomer(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save customer', { error: e.message })
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    await deleteCustomer(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    logger.error('Failed to delete customer', { error: e.message })
  }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>
