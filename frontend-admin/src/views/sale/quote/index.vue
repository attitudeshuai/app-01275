<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header"><h3>销售报价</h3></div>
      
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="24">
          <a-col :span="8">
            <a-form-item label="客户" name="customerId">
              <a-select
                v-model:value="form.customerId"
                placeholder="请选择客户"
                show-search
                :filter-option="filterOption"
                :options="customerList"
                :field-names="{ label: 'customerName', value: 'id' }"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="出库仓库" name="warehouseId">
              <a-select
                v-model:value="form.warehouseId"
                placeholder="请选择仓库"
                :options="warehouseList"
                :field-names="{ label: 'warehouseName', value: 'id' }"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="备注">
              <a-input v-model:value="form.remark" placeholder="请输入备注" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>商品明细</a-divider>
        
        <div class="toolbar">
          <a-button type="primary" @click="goodsModalVisible = true">
            <template #icon><PlusOutlined /></template>
            选择商品
          </a-button>
        </div>

        <a-table
          :columns="itemColumns"
          :data-source="form.items"
          :pagination="false"
          row-key="goodsId"
          size="middle"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'quantity'">
              <a-input-number v-model:value="record.quantity" :min="1" @change="calcAmount(record)" />
            </template>
            <template v-if="column.key === 'price'">
              <a-input-number v-model:value="record.price" :min="0" :precision="2" @change="calcAmount(record)" />
            </template>
            <template v-if="column.key === 'amount'">
              ¥{{ record.amount?.toFixed(2) }}
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" danger size="small" @click="removeItem(index)">删除</a-button>
            </template>
          </template>
          <template #summary>
            <a-table-summary-row>
              <a-table-summary-cell :col-span="4">合计</a-table-summary-cell>
              <a-table-summary-cell>
                <span style="font-weight: 600; color: #f5222d">¥{{ totalAmount.toFixed(2) }}</span>
              </a-table-summary-cell>
              <a-table-summary-cell />
            </a-table-summary-row>
          </template>
        </a-table>

        <div class="form-footer">
          <a-button type="primary" size="large" :loading="submitLoading" @click="handleSubmit">
            提交报价
          </a-button>
        </div>
      </a-form>
    </div>

    <!-- 商品选择组件 -->
    <GoodsSelectModal
      v-model:open="goodsModalVisible"
      business-type="SALE"
      :existing-goods-ids="existingGoodsIds"
      @confirm="handleGoodsConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getCustomerList, getWarehouseList } from '@/api/base'
import { quoteSale } from '@/api/sale'
import { saveGoodsHistory } from '@/api/system'
import GoodsSelectModal from '@/components/GoodsSelectModal.vue'
import logger from '@/utils/logger'

const router = useRouter()
const formRef = ref()
const submitLoading = ref(false)
const customerList = ref([])
const warehouseList = ref([])
const goodsModalVisible = ref(false)

const form = reactive({
  customerId: null,
  warehouseId: null,
  remark: '',
  items: []
})

const rules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }]
}

const itemColumns = [
  { title: '商品编码', dataIndex: 'goodsCode', key: 'goodsCode' },
  { title: '商品名称', dataIndex: 'goodsName', key: 'goodsName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 120 },
  { title: '单价', dataIndex: 'price', key: 'price', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '操作', key: 'action', width: 80 }
]

const totalAmount = computed(() => form.items.reduce((sum, item) => sum + (item.amount || 0), 0))
const existingGoodsIds = computed(() => form.items.map(i => i.goodsId))

const filterOption = (input, option) => option.customerName.toLowerCase().includes(input.toLowerCase())
const calcAmount = (record) => { record.amount = (record.quantity || 0) * (record.price || 0) }

const removeItem = (index) => {
  form.items.splice(index, 1)
}

const handleGoodsConfirm = ({ selectedGoods, priceField }) => {
  const existingIds = form.items.map(i => i.goodsId)
  selectedGoods.forEach(goods => {
    if (!existingIds.includes(goods.id)) {
      const price = goods[priceField] || 0
      form.items.push({
        goodsId: goods.id,
        goodsCode: goods.goodsCode,
        goodsName: goods.goodsName,
        quantity: 1,
        price,
        amount: price
      })
    }
  })
}

const fetchCustomerList = async () => {
  try {
    const res = await getCustomerList()
    customerList.value = res.data
  } catch (e) {
    logger.error('Failed to load customer list', { error: e.message })
  }
}

const fetchWarehouseList = async () => {
  try {
    const res = await getWarehouseList()
    warehouseList.value = res.data
  } catch (e) {
    logger.error('Failed to load warehouse list', { error: e.message })
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.items.length === 0) {
      message.warning('请添加商品明细')
      return
    }
    submitLoading.value = true
    await quoteSale(form)
    await saveGoodsHistory('SALE', form.items.map(i => i.goodsId))
    message.success('销售报价提交成功')
    logger.info('Sale quote submitted')
    router.push('/sale/order')
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to submit sale quote', { error: e.message })
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchCustomerList()
  fetchWarehouseList()
})
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  
  .card-header {
    margin-bottom: 24px;
    h3 { margin: 0; font-size: 18px; }
  }
}
.toolbar { margin-bottom: 16px; }
.form-footer { margin-top: 24px; text-align: center; }
</style>
