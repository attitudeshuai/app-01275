<template>
  <div class="page-container">
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <MenuOutlined />
          <span>菜单管理</span>
        </div>
        <a-button type="primary" @click="handleAdd(0)">
          <template #icon><PlusOutlined /></template>
          新增菜单
        </a-button>
      </div>
      
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id" :default-expand-all-rows="true">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'icon'">
            <span class="icon-cell">{{ record.icon || '-' }}</span>
          </template>
          <template v-if="column.key === 'type'">
            <a-tag :color="typeColors[record.type]">{{ typeTexts[record.type] }}</a-tag>
          </template>
          <template v-if="column.key === 'visible'">
            <a-tag :color="record.visible === 1 ? 'success' : 'default'">{{ record.visible === 1 ? '显示' : '隐藏' }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleAdd(record.id)">添加</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定要删除该菜单吗？" @confirm="handleDelete(record)">
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
        <a-form-item label="上级菜单">
          <a-tree-select v-model:value="form.parentId" :tree-data="menuTreeData" placeholder="请选择上级菜单" allow-clear :field-names="{ label: 'menuName', value: 'id', children: 'children' }" />
        </a-form-item>
        <a-form-item label="菜单类型" name="type">
          <a-radio-group v-model:value="form.type">
            <a-radio :value="1">目录</a-radio>
            <a-radio :value="2">菜单</a-radio>
            <a-radio :value="3">按钮</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="菜单名称" name="menuName">
          <a-input v-model:value="form.menuName" placeholder="请输入菜单名称" />
        </a-form-item>
        <a-form-item v-if="form.type !== 3" label="路由路径">
          <a-input v-model:value="form.path" placeholder="请输入路由路径" />
        </a-form-item>
        <a-form-item v-if="form.type === 2" label="组件路径">
          <a-input v-model:value="form.component" placeholder="请输入组件路径" />
        </a-form-item>
        <a-form-item label="权限标识">
          <a-input v-model:value="form.perms" placeholder="请输入权限标识" />
        </a-form-item>
        <a-form-item v-if="form.type !== 3" label="图标">
          <a-input v-model:value="form.icon" placeholder="如: home, setting, user" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item v-if="form.type !== 3" label="是否显示">
          <a-radio-group v-model:value="form.visible">
            <a-radio :value="1">显示</a-radio>
            <a-radio :value="0">隐藏</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, MenuOutlined } from '@ant-design/icons-vue'
import { getMenuTree, saveMenu, updateMenu, deleteMenu } from '@/api/system'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const typeTexts = { 1: '目录', 2: '菜单', 3: '按钮' }
const typeColors = { 1: 'blue', 2: 'green', 3: 'orange' }

const form = reactive({ id: null, parentId: 0, menuName: '', path: '', component: '', perms: '', icon: '', type: 1, sort: 0, visible: 1 })
const rules = { menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }

const menuTreeData = computed(() => [{ id: 0, menuName: '根目录', children: tableData.value }])

const columns = [
  { title: '菜单名称', dataIndex: 'menuName', key: 'menuName', width: 200 },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 60 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 80 },
  { title: '路由路径', dataIndex: 'path', key: 'path' },
  { title: '权限标识', dataIndex: 'perms', key: 'perms' },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 60 },
  { title: '显示', dataIndex: 'visible', key: 'visible', width: 80 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data
  } catch (e) { logger.error('Failed to load menu tree', { error: e.message }) }
  finally { loading.value = false }
}

const handleAdd = (parentId) => {
  dialogTitle.value = '新增菜单'
  Object.assign(form, { id: null, parentId, menuName: '', path: '', component: '', perms: '', icon: '', type: 1, sort: 0, visible: 1 })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, record)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateMenu(form)
      message.success('更新成功')
    } else {
      await saveMenu(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save menu', { error: e.message })
  } finally { submitLoading.value = false }
}

const handleDelete = async (record) => {
  try {
    await deleteMenu(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete menu', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';
.icon-cell { color: #666; font-size: 13px; }
</style>