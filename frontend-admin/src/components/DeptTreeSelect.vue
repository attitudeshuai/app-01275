<template>
  <a-tree-select
    v-model:value="selectedValue"
    :tree-data="deptTree"
    :placeholder="placeholder"
    :allow-clear="allowClear"
    :disabled="disabled"
    :multiple="multiple"
    :style="{ width: width }"
    :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
    :tree-default-expand-all="treeDefaultExpandAll"
    :show-search="showSearch"
    :tree-node-filter-prop="'deptName'"
    @change="handleChange"
  />
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getDeptTree } from '@/api/system'
import logger from '@/utils/logger'

const props = defineProps({
  modelValue: {
    type: [Number, String, Array],
    default: null
  },
  placeholder: {
    type: String,
    default: '请选择部门'
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  multiple: {
    type: Boolean,
    default: false
  },
  width: {
    type: String,
    default: '100%'
  },
  treeDefaultExpandAll: {
    type: Boolean,
    default: false
  },
  showSearch: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)
const deptTree = ref([])

const fetchDeptTree = async () => {
  try {
    const res = await getDeptTree()
    deptTree.value = res.data
    logger.info('DeptTreeSelect: dept tree loaded', { count: res.data?.length })
  } catch (e) {
    logger.error('DeptTreeSelect: failed to load dept tree', { error: e.message })
  }
}

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}

watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
})

onMounted(() => {
  fetchDeptTree()
})

defineExpose({
  refresh: fetchDeptTree
})
</script>
