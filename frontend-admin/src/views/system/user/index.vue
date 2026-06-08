<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>用户名</label>
          <a-input v-model:value="searchForm.username" placeholder="输入用户名搜索" allow-clear @pressEnter="fetchData" />
        </div>
        <div class="search-item">
          <label>部门</label>
          <DeptTreeSelect v-model="searchForm.deptId" placeholder="全部部门" />
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
          <UserOutlined />
          <span>用户列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleResetPwd(record)">重置密码</a-button>
              <a-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="dialogVisible" :title="dialogTitle" :confirm-loading="submitLoading" @ok="handleSubmit" width="520px">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="padding: 20px 0;">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </a-form-item>
        <a-form-item v-if="!form.id" label="密码" name="password">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="form.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="form.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="部门" name="deptId">
          <DeptTreeSelect v-model="form.deptId" placeholder="请选择部门" />
        </a-form-item>
        <a-form-item label="角色" name="roleIds">
          <a-select v-model:value="form.roleIds" mode="multiple" placeholder="请选择角色" :options="roleList" :field-names="{ label: 'roleName', value: 'id' }" />
        </a-form-item>
        <a-form-item label="状态" name="status">
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
import { SearchOutlined, ReloadOutlined, PlusOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getUserList, saveUser, updateUser, deleteUser, resetPassword, getRoleList } from '@/api/system'
import DeptTreeSelect from '@/components/DeptTreeSelect.vue'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const roleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({ username: '', deptId: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], status: 1 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load user list', { error: e.message }) }
  finally { loading.value = false }
}

const fetchRoleList = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (e) { logger.error('Failed to load role list', { error: e.message }) }
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.deptId = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], status: 1 })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...record, roleIds: record.roleIds || [] })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateUser(form)
      message.success('更新成功')
    } else {
      await saveUser(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save user', { error: e.message })
  } finally { submitLoading.value = false }
}

const handleDelete = async (record) => {
  try {
    await deleteUser(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete user', { error: e.message }) }
}

const handleResetPwd = async (record) => {
  try {
    await resetPassword(record.id)
    message.success('密码已重置为 123456')
  } catch (e) { logger.error('Failed to reset password', { error: e.message }) }
}

onMounted(() => { fetchData(); fetchRoleList() })
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>