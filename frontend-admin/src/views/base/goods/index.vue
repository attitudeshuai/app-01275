<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-form">
        <div class="search-item">
          <label>商品名称</label>
          <a-input 
            v-model:value="searchForm.goodsName" 
            placeholder="输入名称搜索" 
            allow-clear
            @pressEnter="fetchData"
          />
        </div>
        <div class="search-item">
          <label>商品编码</label>
          <a-input 
            v-model:value="searchForm.goodsCode" 
            placeholder="输入编码搜索" 
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
          <ShoppingOutlined />
          <span>商品列表</span>
          <a-tag color="blue">{{ pagination.total }} 条</a-tag>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增商品
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
          <template v-if="column.key === 'purchasePrice'">
            <span class="price">¥{{ record.purchasePrice?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'salePrice'">
            <span class="price sale">¥{{ record.salePrice?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定要删除该商品吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      width="720px"
    >
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }" style="padding: 20px 0;">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="商品编码" name="goodsCode">
              <a-input v-model:value="form.goodsCode" placeholder="请输入商品编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="商品名称" name="goodsName">
              <a-input v-model:value="form.goodsName" placeholder="请输入商品名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="单位" name="unit">
              <a-input v-model:value="form.unit" placeholder="如：个、件、箱" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规格" name="spec">
              <a-input v-model:value="form.spec" placeholder="请输入规格" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="采购价" name="purchasePrice">
              <a-input-number v-model:value="form.purchasePrice" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="销售价" name="salePrice">
              <a-input-number v-model:value="form.salePrice" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="安全库存" name="safetyStock">
              <a-input-number v-model:value="form.safetyStock" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大库存" name="maxStock">
              <a-input-number v-model:value="form.maxStock" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注" name="remark" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
          <a-textarea v-model:value="form.remark" placeholder="请输入备注" :rows="3" />
        </a-form-item>
        <a-form-item label="状态" name="status" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
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
import { SearchOutlined, ReloadOutlined, PlusOutlined, ShoppingOutlined } from '@ant-design/icons-vue'
import { getGoodsPage, saveGoods, updateGoods, deleteGoods } from '@/api/base'
import logger from '@/utils/logger'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({ goodsName: '', goodsCode: '', status: null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showTotal: t => `共 ${t} 条` })

const form = reactive({
  id: null, goodsCode: '', goodsName: '', unit: '', spec: '',
  purchasePrice: 0, salePrice: 0, safetyStock: 0, maxStock: 99999, remark: '', status: 1
})

const rules = {
  goodsCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
}

const columns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode', width: 120 },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '规格', dataIndex: 'spec', key: 'spec', width: 100 },
  { title: '采购价', dataIndex: 'purchasePrice', key: 'purchasePrice', width: 100 },
  { title: '销售价', dataIndex: 'salePrice', key: 'salePrice', width: 100 },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getGoodsPage({ page: pagination.current, size: pagination.pageSize, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) { logger.error('Failed to load goods list', { error: e.message }) }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.goodsName = ''
  searchForm.goodsCode = ''
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
  dialogTitle.value = '新增商品'
  Object.assign(form, {
    id: null, goodsCode: '', goodsName: '', unit: '', spec: '',
    purchasePrice: 0, salePrice: 0, safetyStock: 0, maxStock: 99999, remark: '', status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (record) => {
  dialogTitle.value = '编辑商品'
  Object.assign(form, record)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (form.id) {
      await updateGoods(form)
      message.success('更新成功')
    } else {
      await saveGoods(form)
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save goods', { error: e.message })
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    await deleteGoods(record.id)
    message.success('删除成功')
    fetchData()
  } catch (e) { logger.error('Failed to delete goods', { error: e.message }) }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
@import '@/styles/search-form.scss';

.price {
  font-weight: 500;
  color: #666;
  &.sale { color: #f5222d; }
}
</style>
