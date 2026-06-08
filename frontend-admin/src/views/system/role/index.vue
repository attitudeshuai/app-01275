<template>
  <div class="page-container">
    <!-- 角色列表 -->
    <div class="role-section">
      <div class="section-card">
        <div class="card-header">
          <div class="header-title">
            <TeamOutlined />
            <span>角色管理</span>
            <a-tag color="blue">{{ roleList.length }} 个角色</a-tag>
          </div>
          <a-button type="primary" @click="handleAddRole">
            <template #icon><PlusOutlined /></template>
            新增角色
          </a-button>
        </div>

        <div class="role-grid">
          <div 
            v-for="role in roleList" 
            :key="role.id" 
            class="role-card"
            :class="{ active: selectedRole?.id === role.id }"
            @click="selectRole(role)"
          >
            <div class="role-icon" :class="getRoleClass(role.roleCode)">
              <CrownOutlined v-if="role.roleCode === 'SUPER_ADMIN'" />
              <ShoppingCartOutlined v-else-if="role.roleCode === 'PURCHASER'" />
              <ShopOutlined v-else-if="role.roleCode === 'SALESMAN'" />
              <InboxOutlined v-else-if="role.roleCode === 'WAREHOUSE'" />
              <UserOutlined v-else />
            </div>
            <div class="role-info">
              <div class="role-name">{{ role.roleName }}</div>
              <div class="role-code">{{ role.roleCode }}</div>
            </div>
            <div class="role-badge" v-if="role.roleCode === 'SUPER_ADMIN'">
              <StarFilled />
            </div>
            <div class="role-actions">
              <a-button type="text" size="small" @click.stop="handleEditRole(role)">
                <EditOutlined />
              </a-button>
              <a-popconfirm 
                v-if="role.roleCode !== 'SUPER_ADMIN'"
                title="确定删除该角色？" 
                @confirm="handleDeleteRole(role)"
              >
                <a-button type="text" size="small" danger @click.stop>
                  <DeleteOutlined />
                </a-button>
              </a-popconfirm>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 权限树 -->
    <div class="permission-section">
      <div class="section-card">
        <div class="card-header">
          <div class="header-title">
            <SafetyCertificateOutlined />
            <span>权限配置</span>
            <a-tag v-if="selectedRole" color="purple">{{ selectedRole.roleName }}</a-tag>
          </div>
          <a-button 
            type="primary" 
            :disabled="!selectedRole"
            :loading="saveLoading"
            @click="savePermissions"
          >
            <template #icon><SaveOutlined /></template>
            保存权限
          </a-button>
        </div>

        <div v-if="selectedRole" class="permission-content">
          <!-- 权限统计 -->
          <div class="permission-stats">
            <div class="stat-item">
              <div class="stat-value">{{ checkedKeys.length }}</div>
              <div class="stat-label">已选权限</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ totalMenuCount }}</div>
              <div class="stat-label">总权限数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ Math.round(checkedKeys.length / totalMenuCount * 100) }}%</div>
              <div class="stat-label">覆盖率</div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="quick-actions">
            <a-button size="small" @click="checkAll">
              <CheckSquareOutlined /> 全选
            </a-button>
            <a-button size="small" @click="uncheckAll">
              <BorderOutlined /> 取消全选
            </a-button>
            <a-button size="small" @click="expandAll">
              <NodeExpandOutlined /> 展开全部
            </a-button>
            <a-button size="small" @click="collapseAll">
              <NodeCollapseOutlined /> 收起全部
            </a-button>
          </div>

          <!-- 权限树 -->
          <div class="tree-wrapper">
            <a-tree
              v-model:checkedKeys="checkedKeys"
              v-model:expandedKeys="expandedKeys"
              :tree-data="menuTree"
              :field-names="{ title: 'menuName', key: 'id', children: 'children' }"
              checkable
              :selectable="false"
            >
              <template #title="{ menuName, type, icon, path }">
                <div class="tree-node">
                  <component :is="getMenuIcon(icon)" class="node-icon" />
                  <span class="node-name">{{ menuName }}</span>
                  <a-tag v-if="type === 1" size="small" color="blue">目录</a-tag>
                  <a-tag v-else-if="type === 2" size="small" color="green">菜单</a-tag>
                  <a-tag v-else size="small" color="orange">按钮</a-tag>
                  <span v-if="path" class="node-path">{{ path }}</span>
                </div>
              </template>
            </a-tree>
          </div>
        </div>

        <div v-else class="empty-state">
          <SafetyCertificateOutlined />
          <p>请选择一个角色查看权限配置</p>
        </div>
      </div>
    </div>

    <!-- 角色编辑弹窗 -->
    <a-modal 
      v-model:open="roleModalVisible" 
      :title="editingRole ? '编辑角色' : '新增角色'"
      @ok="submitRole"
      :confirm-loading="submitLoading"
      width="480px"
    >
      <a-form ref="roleFormRef" :model="roleForm" :rules="roleRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="padding: 20px 0;">
        <a-form-item label="角色名称" name="roleName">
          <a-input v-model:value="roleForm.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色编码" name="roleCode">
          <a-input 
            v-model:value="roleForm.roleCode" 
            placeholder="请输入角色编码" 
            :disabled="!!editingRole"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="roleForm.remark" placeholder="请输入备注" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  TeamOutlined, PlusOutlined, CrownOutlined, ShoppingCartOutlined,
  ShopOutlined, InboxOutlined, UserOutlined, StarFilled, EditOutlined,
  DeleteOutlined, SafetyCertificateOutlined, SaveOutlined, CheckSquareOutlined,
  BorderOutlined, NodeExpandOutlined, NodeCollapseOutlined,
  HomeOutlined, SettingOutlined, DatabaseOutlined, BarChartOutlined,
  FileTextOutlined, MenuOutlined, ApartmentOutlined, ContactsOutlined,
  SolutionOutlined, GiftOutlined, FormOutlined, FileDoneOutlined,
  AuditOutlined, ImportOutlined, DollarOutlined, ExportOutlined,
  SearchOutlined, SwapOutlined, AlertOutlined, RiseOutlined,
  LineChartOutlined, WarningOutlined, FundOutlined
} from '@ant-design/icons-vue'
import { getRolePage, saveRole, updateRole, deleteRole, getRoleMenus, saveRoleMenus } from '@/api/system'
import { getMenuTree } from '@/api/system'
import logger from '@/utils/logger'

const roleList = ref([])
const selectedRole = ref(null)
const menuTree = ref([])
const checkedKeys = ref([])
const expandedKeys = ref([])
const saveLoading = ref(false)
const submitLoading = ref(false)
const roleModalVisible = ref(false)
const editingRole = ref(null)
const roleFormRef = ref()

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  remark: ''
})

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称' }],
  roleCode: [{ required: true, message: '请输入角色编码' }]
}

const iconMap = {
  'home': HomeOutlined, 'HomeOutlined': HomeOutlined,
  'setting': SettingOutlined, 'SettingOutlined': SettingOutlined,
  'database': DatabaseOutlined, 'DatabaseOutlined': DatabaseOutlined,
  'bar-chart': BarChartOutlined, 'BarChartOutlined': BarChartOutlined,
  'file-text': FileTextOutlined, 'FileTextOutlined': FileTextOutlined,
  'menu': MenuOutlined, 'MenuOutlined': MenuOutlined,
  'apartment': ApartmentOutlined, 'ApartmentOutlined': ApartmentOutlined,
  'user': UserOutlined, 'UserOutlined': UserOutlined,
  'team': TeamOutlined, 'TeamOutlined': TeamOutlined,
  'contacts': ContactsOutlined, 'ContactsOutlined': ContactsOutlined,
  'solution': SolutionOutlined, 'SolutionOutlined': SolutionOutlined,
  'gift': GiftOutlined, 'GiftOutlined': GiftOutlined,
  'shopping-cart': ShoppingCartOutlined, 'ShoppingCartOutlined': ShoppingCartOutlined,
  'form': FormOutlined, 'FormOutlined': FormOutlined,
  'file-done': FileDoneOutlined, 'FileDoneOutlined': FileDoneOutlined,
  'audit': AuditOutlined, 'AuditOutlined': AuditOutlined,
  'import': ImportOutlined, 'ImportOutlined': ImportOutlined,
  'shop': ShopOutlined, 'ShopOutlined': ShopOutlined,
  'dollar': DollarOutlined, 'DollarOutlined': DollarOutlined,
  'export': ExportOutlined, 'ExportOutlined': ExportOutlined,
  'inbox': InboxOutlined, 'InboxOutlined': InboxOutlined,
  'search': SearchOutlined, 'SearchOutlined': SearchOutlined,
  'swap': SwapOutlined, 'SwapOutlined': SwapOutlined,
  'edit': EditOutlined, 'EditOutlined': EditOutlined,
  'alert': AlertOutlined, 'AlertOutlined': AlertOutlined,
  'rise': RiseOutlined, 'RiseOutlined': RiseOutlined,
  'line-chart': LineChartOutlined, 'LineChartOutlined': LineChartOutlined,
  'warning': WarningOutlined, 'WarningOutlined': WarningOutlined,
  'fund': FundOutlined, 'FundOutlined': FundOutlined,
  'save': SaveOutlined, 'SaveOutlined': SaveOutlined
}

const getMenuIcon = (icon) => iconMap[icon] || MenuOutlined

const totalMenuCount = computed(() => {
  const count = (nodes) => nodes.reduce((sum, n) => sum + 1 + (n.children ? count(n.children) : 0), 0)
  return count(menuTree.value)
})

const getRoleClass = (code) => {
  const map = {
    'SUPER_ADMIN': 'admin',
    'PURCHASER': 'purchase',
    'SALESMAN': 'sales',
    'WAREHOUSE': 'warehouse'
  }
  return map[code] || 'default'
}

const fetchRoles = async () => {
  try {
    const res = await getRolePage({ page: 1, size: 100 })
    roleList.value = res.data.records || res.data || []
  } catch (e) {
    logger.error('Failed to load roles', { error: e.message })
  }
}

const fetchMenuTree = async () => {
  try {
    const res = await getMenuTree()
    menuTree.value = res.data || []
    // 默认展开第一层
    expandedKeys.value = menuTree.value.map(m => m.id)
  } catch (e) {
    logger.error('Failed to load menu tree', { error: e.message })
  }
}

const selectRole = async (role) => {
  selectedRole.value = role
  try {
    const res = await getRoleMenus(role.id)
    checkedKeys.value = res.data || []
  } catch (e) {
    logger.error('Failed to load role menus', { error: e.message })
    checkedKeys.value = []
  }
}

const savePermissions = async () => {
  if (!selectedRole.value) return
  saveLoading.value = true
  try {
    await saveRoleMenus(selectedRole.value.id, checkedKeys.value)
    message.success('权限保存成功')
  } catch (e) {
    logger.error('Failed to save permissions', { error: e.message })
  } finally {
    saveLoading.value = false
  }
}

const checkAll = () => {
  const getAllIds = (nodes) => nodes.flatMap(n => [n.id, ...(n.children ? getAllIds(n.children) : [])])
  checkedKeys.value = getAllIds(menuTree.value)
}

const uncheckAll = () => {
  checkedKeys.value = []
}

const expandAll = () => {
  const getAllIds = (nodes) => nodes.flatMap(n => [n.id, ...(n.children ? getAllIds(n.children) : [])])
  expandedKeys.value = getAllIds(menuTree.value)
}

const collapseAll = () => {
  expandedKeys.value = []
}

const handleAddRole = () => {
  editingRole.value = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.remark = ''
  roleModalVisible.value = true
}

const handleEditRole = (role) => {
  editingRole.value = role
  roleForm.roleName = role.roleName
  roleForm.roleCode = role.roleCode
  roleForm.remark = role.remark || ''
  roleModalVisible.value = true
}

const submitRole = async () => {
  try {
    await roleFormRef.value.validate()
    submitLoading.value = true
    if (editingRole.value) {
      await updateRole({ ...roleForm, id: editingRole.value.id })
      message.success('角色更新成功')
    } else {
      await saveRole(roleForm)
      message.success('角色创建成功')
    }
    roleModalVisible.value = false
    fetchRoles()
  } catch (e) {
    if (e.errorFields) return
    logger.error('Failed to save role', { error: e.message })
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteRole = async (role) => {
  try {
    await deleteRole(role.id)
    message.success('角色删除成功')
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
      checkedKeys.value = []
    }
    fetchRoles()
  } catch (e) {
    logger.error('Failed to delete role', { error: e.message })
  }
}

onMounted(() => {
  fetchRoles()
  fetchMenuTree()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 24px;
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  
  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
    
    .anticon {
      color: #667eea;
    }
  }
  
  .ant-btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    color: #fff !important;
    
    .anticon {
      color: #fff !important;
    }
  }
}

.role-grid {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  
  &:hover {
    border-color: #667eea;
    background: #f8fafc;
    
    .role-actions {
      opacity: 1;
    }
  }
  
  &.active {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
  }
  
  .role-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #fff;
    
    &.admin { background: linear-gradient(135deg, #f59e0b, #ef4444); }
    &.purchase { background: linear-gradient(135deg, #06b6d4, #3b82f6); }
    &.sales { background: linear-gradient(135deg, #10b981, #059669); }
    &.warehouse { background: linear-gradient(135deg, #8b5cf6, #6366f1); }
    &.default { background: linear-gradient(135deg, #64748b, #475569); }
  }
  
  .role-info {
    flex: 1;
    
    .role-name {
      font-size: 15px;
      font-weight: 600;
      color: #1e293b;
    }
    
    .role-code {
      font-size: 12px;
      color: #94a3b8;
      font-family: monospace;
    }
  }
  
  .role-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    color: #f59e0b;
    font-size: 14px;
  }
  
  .role-actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s;
  }
}

.permission-section {
  .section-card {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.permission-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
}

.permission-stats {
  display: flex;
  gap: 24px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 10px;
  margin-bottom: 16px;
  
  .stat-item {
    text-align: center;
    
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #667eea;
    }
    
    .stat-label {
      font-size: 12px;
      color: #64748b;
      margin-top: 2px;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  
  .ant-btn {
    border-radius: 6px;
    font-size: 13px;
  }
}

.tree-wrapper {
  flex: 1;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  
  :deep(.ant-tree) {
    .ant-tree-treenode {
      padding: 4px 0;
    }
    
    .ant-tree-checkbox-checked .ant-tree-checkbox-inner {
      background: #667eea;
      border-color: #667eea;
    }
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .node-icon {
    color: #667eea;
    font-size: 14px;
  }
  
  .node-name {
    font-weight: 500;
  }
  
  .ant-tag {
    margin: 0;
    font-size: 10px;
    line-height: 16px;
    padding: 0 6px;
  }
  
  .node-path {
    font-size: 11px;
    color: #94a3b8;
    font-family: monospace;
  }
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #94a3b8;
  
  .anticon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  p {
    font-size: 15px;
    margin: 0;
  }
}

@media (max-width: 1200px) {
  .page-container {
    grid-template-columns: 1fr;
  }
}
</style>
