<template>
  <div class="page-container">
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <ApartmentOutlined />
          <span>部门管理</span>
        </div>
        <a-button type="primary" @click="handleAdd(0)">
          <template #icon><PlusOutlined /></template>
          新增部门
        </a-button>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id" :default-expand-all-rows="true">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleAdd(record.id)">添加</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定要删除该部门吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="dialogVisible" :title="dialogTitle" @ok="handleSubmit" :confirm-loading="submitLoading" width="480px">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="padding: 20px 0;">
        <a-form-item label="上级部门">
          <a-tree-select v-model:value="form.parentId" :tree-data="deptTreeData" placeholder="请选择上级部门" allow-clear :field-names="{ label: 'deptName', value: 'id', children: 'children' }" />
        </a-form-item>
        <a-form-item label="部门名称" name="deptName">
          <a-input v-model:value="form.deptName" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.leader" placeholder="请输入负责人" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.phone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sort" :min="0" style="width: 100%" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ApartmentOutlined } from '@ant-design/icons-vue'
import { getDeptTree, saveDept, updateDept, deleteDept } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = reactive({ id: null, parentId: 0, deptName: '', leader: '', phone: '', sort: 0, status: 1 })
const rules = { deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

const deptTreeData = computed(() => [{ id: 0, deptName: '根部门', children: tableData.value }])

const columns = [
  { title: '部门名称', dataIndex: 'deptName', key: 'deptName', width: 200 },
  { title: '负责人', dataIndex: 'leader', key: 'leader' },
  { title: '联系电话', dataIndex: 'phone', key: 'phone' },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 60 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, customRender: ({ text }) => formatDateTime(text) },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    tableData.value = res.data
  } catch (e) { logger.error('Failed to load dept tree', { error: e.message }) }
  finally { loading.value = false }
}

const handleAdd = (parentId) => {
  dialogTitle.value = '新增部门'
  Object.assign(form, { id: null, parentId, deptName: '', leader: '', phone: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑部门'
  Object.assign(form, record)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateDept(form)
      message.success('更新成功')
    } else {
      await saveDept(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save dept', { error: e.message })
  } finally { submitLoading.value = false }
}

const handleDelete = async (record) => {
  try {
    await deleteDept(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete dept', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
</style>