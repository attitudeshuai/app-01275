<template>
  <div class="page-container">
    <a-row :gutter="24">
      <a-col :span="8">
        <div class="profile-card">
          <div class="avatar-section">
            <a-avatar :size="80"><template #icon><user-outlined /></template></a-avatar>
            <h2>{{ userInfo.realName || '用户' }}</h2>
            <p>{{ userInfo.username }}</p>
          </div>
          <a-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">部门</span>
              <span class="value">{{ userInfo.deptName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机</span>
              <span class="value">{{ userInfo.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色</span>
              <span class="value">
                <a-tag v-for="role in userInfo.roles" :key="role" color="blue">{{ role }}</a-tag>
              </span>
            </div>
          </div>
        </div>
      </a-col>
      <a-col :span="16">
        <div class="content-card">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="info" tab="基本信息">
              <a-form ref="infoFormRef" :model="infoForm" :rules="infoRules" :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
                <a-form-item label="真实姓名" name="realName">
                  <a-input v-model:value="infoForm.realName" placeholder="请输入真实姓名" />
                </a-form-item>
                <a-form-item label="手机号" name="phone">
                  <a-input v-model:value="infoForm.phone" placeholder="请输入手机号" />
                </a-form-item>
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="infoForm.email" placeholder="请输入邮箱" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 4 }">
                  <a-button type="primary" :loading="infoLoading" @click="updateInfo">保存修改</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="password" tab="修改密码">
              <a-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
                <a-form-item label="原密码" name="oldPassword">
                  <a-input-password v-model:value="pwdForm.oldPassword" placeholder="请输入原密码" />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                  <a-input-password v-model:value="pwdForm.newPassword" placeholder="请输入新密码" />
                </a-form-item>
                <a-form-item label="确认密码" name="confirmPassword">
                  <a-input-password v-model:value="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 4 }">
                  <a-button type="primary" :loading="pwdLoading" @click="changePassword">修改密码</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changeUserPassword } from '@/api/system'
import logger from '@/utils/logger'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})

const activeTab = ref('info')
const infoLoading = ref(false)
const pwdLoading = ref(false)
const infoFormRef = ref()
const pwdFormRef = ref()

const infoForm = reactive({ realName: '', phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const infoRules = { realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }] }
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (_, value) => value === pwdForm.newPassword ? Promise.resolve() : Promise.reject('两次输入的密码不一致'), trigger: 'blur' }
  ]
}

const updateInfo = async () => {
  try {
    await infoFormRef.value.validate()
    infoLoading.value = true
    await updateUserInfo(infoForm)
    message.success('信息更新成功')
    userStore.fetchUserInfo()
    logger.info('User info updated')
  } catch (e) { if (e.errorFields) return; logger.error('Failed to update user info', { error: e.message }) }
  finally { infoLoading.value = false }
}

const changePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    pwdLoading.value = true
    await changeUserPassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    message.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    logger.info('Password changed')
  } catch (e) { if (e.errorFields) return; logger.error('Failed to change password', { error: e.message }) }
  finally { pwdLoading.value = false }
}

onMounted(() => {
  if (userInfo.value) {
    infoForm.realName = userInfo.value.realName || ''
    infoForm.phone = userInfo.value.phone || ''
    infoForm.email = userInfo.value.email || ''
  }
})
</script>

<style lang="scss" scoped>
.page-container { padding: 24px; }
.profile-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); .avatar-section { text-align: center; h2 { margin: 16px 0 4px; font-size: 20px; } p { color: #8c8c8c; } } .info-list { .info-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f0f0f0; &:last-child { border-bottom: none; } .label { color: #8c8c8c; } .value { color: #262626; } } } }
.content-card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); min-height: 400px; }
</style>
