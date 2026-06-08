<template>
  <a-modal
    v-model:open="visible"
    title="选择商品"
    width="900px"
    :bodyStyle="{ padding: '24px' }"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="goods-modal-content">
      <!-- 搜索区域 -->
      <div class="search-area">
        <a-input
          v-model:value="searchKeyword"
          placeholder="搜索商品名称/编码"
          allow-clear
          @pressEnter="fetchGoodsList"
        >
          <template #prefix>
            <search-outlined class="search-icon" />
          </template>
          <template #suffix>
            <a-button type="primary" class="search-btn" @click="fetchGoodsList">
              搜索
            </a-button>
          </template>
        </a-input>
      </div>

      <!-- 已选商品标签 -->
      <div v-if="selectedRows.length > 0" class="selected-tags">
        <span class="selected-label">已选 {{ selectedRows.length }} 件商品：</span>
        <a-tag 
          v-for="item in selectedRows.slice(0, 5)" 
          :key="item.id" 
          closable 
          color="blue"
          @close="removeSelected(item.id)"
        >
          {{ item.goodsName }}
        </a-tag>
        <a-tag v-if="selectedRows.length > 5" color="default">
          +{{ selectedRows.length - 5 }} 更多
        </a-tag>
        <a-button type="link" size="small" danger @click="clearSelected">清空</a-button>
      </div>

      <!-- 商品列表 -->
      <a-table
        :columns="columns"
        :data-source="goodsList"
        :row-selection="{ 
          selectedRowKeys: selectedKeys, 
          onChange: handleSelect,
          getCheckboxProps: getCheckboxProps
        }"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        size="middle"
        :scroll="{ y: 400 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'goodsInfo'">
            <div class="goods-info">
              <div class="goods-name">{{ record.goodsName }}</div>
              <div class="goods-code">{{ record.goodsCode }}</div>
            </div>
          </template>
          <template v-if="column.key === 'spec'">
            <span class="spec-text">{{ record.spec || '-' }}</span>
          </template>
          <template v-if="column.key === 'price'">
            <span class="price-text">¥{{ getPriceValue(record)?.toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'stock'">
            <a-tag :color="record.totalStock > 0 ? 'green' : 'red'">
              {{ record.totalStock || 0 }} {{ record.unit }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getGoodsPage, getGoodsByIds } from '@/api/base'
import { getGoodsHistory } from '@/api/system'
import logger from '@/utils/logger'

const props = defineProps({
  open: { type: Boolean, default: false },
  businessType: { type: String, required: true, validator: v => ['PURCHASE', 'SALE'].includes(v) },
  existingGoodsIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:open', 'confirm'])

const visible = ref(false)
const loading = ref(false)
const searchKeyword = ref('')
const goodsList = ref([])
const selectedKeys = ref([])
const selectedRows = ref([])
const pagination = reactive({ 
  current: 1, 
  pageSize: 10, 
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: total => `共 ${total} 件商品`
})

const priceField = computed(() => props.businessType === 'PURCHASE' ? 'purchasePrice' : 'salePrice')
const priceLabel = computed(() => props.businessType === 'PURCHASE' ? '采购价' : '销售价')

const columns = computed(() => [
  { title: '商品信息', key: 'goodsInfo', width: 280 },
  { title: '规格', key: 'spec', width: 120 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80, align: 'center' },
  { title: priceLabel.value, key: 'price', width: 120, align: 'right' },
  { title: '库存', key: 'stock', width: 100, align: 'center' }
])

const getPriceValue = (record) => record[priceField.value]

const getCheckboxProps = (record) => ({
  disabled: props.existingGoodsIds.includes(record.id)
})

watch(() => props.open, async (val) => {
  visible.value = val
  if (val) {
    await initModal()
  }
})

watch(visible, (val) => {
  emit('update:open', val)
})

const initModal = async () => {
  searchKeyword.value = ''
  pagination.current = 1
  
  const historyIds = await fetchGoodsHistory()
  const allIds = [...new Set([...props.existingGoodsIds, ...historyIds])]
  selectedKeys.value = allIds
  
  if (allIds.length > 0) {
    selectedRows.value = await fetchGoodsById(allIds)
  } else {
    selectedRows.value = []
  }
  
  await fetchGoodsList()
}

const fetchGoodsHistory = async () => {
  try {
    const res = await getGoodsHistory(props.businessType)
    return res.data || []
  } catch (e) {
    logger.error('GoodsSelectModal: failed to fetch history', { error: e.message })
    return []
  }
}

const fetchGoodsById = async (ids) => {
  if (!ids?.length) return []
  try {
    const res = await getGoodsByIds(ids)
    return res.data || []
  } catch (e) {
    logger.error('GoodsSelectModal: failed to fetch goods by ids', { error: e.message })
    return []
  }
}

const fetchGoodsList = async () => {
  loading.value = true
  try {
    const res = await getGoodsPage({
      page: pagination.current,
      size: pagination.pageSize,
      goodsName: searchKeyword.value
    })
    goodsList.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    logger.error('GoodsSelectModal: failed to fetch goods list', { error: e.message })
  } finally {
    loading.value = false
  }
}

const handleSelect = (keys, rows) => {
  selectedKeys.value = keys
  const existingMap = new Map(selectedRows.value.map(r => [r.id, r]))
  rows.forEach(r => existingMap.set(r.id, r))
  selectedRows.value = Array.from(existingMap.values()).filter(r => keys.includes(r.id))
}

const removeSelected = (id) => {
  selectedKeys.value = selectedKeys.value.filter(k => k !== id)
  selectedRows.value = selectedRows.value.filter(r => r.id !== id)
}

const clearSelected = () => {
  selectedKeys.value = [...props.existingGoodsIds]
  selectedRows.value = selectedRows.value.filter(r => props.existingGoodsIds.includes(r.id))
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchGoodsList()
}

const handleConfirm = () => {
  emit('confirm', {
    selectedGoods: selectedRows.value,
    priceField: priceField.value
  })
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}

defineExpose({ refresh: fetchGoodsList })
</script>

<style lang="scss" scoped>
.goods-modal-content {
  .search-area {
    margin-bottom: 20px;
    
    :deep(.ant-input-affix-wrapper) {
      border-radius: 8px;
      padding: 8px 12px;
      border: 1px solid #e5e7eb;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      transition: all 0.2s;
      
      &:hover {
        border-color: #7c3aed;
      }
      
      &:focus, &.ant-input-affix-wrapper-focused {
        border-color: #7c3aed;
        box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
      }
      
      .ant-input {
        font-size: 14px;
        
        &::placeholder {
          color: #9ca3af;
        }
      }
      
      .ant-input-suffix {
        margin-left: 8px;
      }
    }
    
    .search-icon {
      color: #9ca3af;
      font-size: 16px;
    }
    
    .search-btn {
      border-radius: 6px;
      height: 32px;
      padding: 0 20px;
      background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
      border: none;
      font-weight: 500;
      
      &:hover {
        background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
      }
    }
  }
  
  .selected-tags {
    background: #f6f8fa;
    border-radius: 8px;
    padding: 12px 16px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    
    .selected-label {
      color: #666;
      font-size: 13px;
      margin-right: 4px;
    }
    
    :deep(.ant-tag) {
      margin: 0;
      border-radius: 4px;
      padding: 2px 8px;
    }
  }
  
  .goods-info {
    .goods-name {
      font-weight: 500;
      color: #262626;
      margin-bottom: 2px;
    }
    
    .goods-code {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
  
  .spec-text {
    color: #666;
    font-size: 13px;
  }
  
  .price-text {
    color: #f5222d;
    font-weight: 600;
    font-size: 14px;
  }
  
  :deep(.ant-table) {
    border-radius: 8px;
    overflow: hidden;
    
    .ant-table-thead > tr > th {
      background: #fafafa;
      font-weight: 600;
      color: #262626;
    }
    
    .ant-table-tbody > tr {
      &:hover > td {
        background: #f0f5ff;
      }
      
      &.ant-table-row-selected > td {
        background: #e6f4ff;
      }
    }
  }
}
</style>
