<template>
  <div class="register-page">
    <div class="bg-animation">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <div class="register-section">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="6" y="14" width="36" height="28" rx="3" stroke="currentColor" stroke-width="3"/>
              <path d="M14 14V10C14 7.79086 15.7909 6 18 6H30C32.2091 6 34 7.79086 34 10V14" stroke="currentColor" stroke-width="3"/>
              <path d="M6 24H42" stroke="currentColor" stroke-width="3"/>
              <circle cx="24" cy="24" r="4" fill="currentColor"/>
            </svg>
          </div>
          <h2>员工注册</h2>
          <p>创建您的账户加入 WMS Pro</p>
        </div>

        <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" @finish="handleRegister" class="register-form">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item name="username" label="用户名">
                <a-input v-model:value="form.username" placeholder="请输入用户名" size="large" class="custom-input">
                  <template #prefix><user-outlined class="input-icon" /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item name="realName" label="真实姓名">
                <a-input v-model:value="form.realName" placeholder="请输入真实姓名" size="large" class="custom-input">
                  <template #prefix><idcard-outlined class="input-icon" /></template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item name="password" label="密码">
                <a-input-password v-model:value="form.password" placeholder="请输入密码" size="large" class="custom-input">
                  <template #prefix><lock-outlined class="input-icon" /></template>
                </a-input-password>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item name="confirmPassword" label="确认密码">
                <a-input-password v-model:value="form.confirmPassword" placeholder="请再次输入密码" size="large" class="custom-input">
                  <template #prefix><lock-outlined class="input-icon" /></template>
                </a-input-password>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item name="phone" label="手机号">
                <a-input v-model:value="form.phone" placeholder="请输入手机号" size="large" class="custom-input">
                  <template #prefix><phone-outlined class="input-icon" /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item name="email" label="邮箱">
                <a-input v-model:value="form.email" placeholder="请输入邮箱" size="large" class="custom-input">
                  <template #prefix><mail-outlined class="input-icon" /></template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item name="deptId" label="所属部门">
            <a-tree-select v-model:value="form.deptId" :tree-data="deptTree" placeholder="请选择部门" size="large"
              :field-names="{ label: 'deptName', value: 'id', children: 'children' }" tree-default-expand-all allow-clear class="custom-select" />
          </a-form-item>

          <a-form-item name="captcha" label="验证码">
            <div class="captcha-wrapper">
              <a-input v-model:value="form.captcha" placeholder="请输入验证码" size="large" class="custom-input captcha-input">
                <template #prefix><safety-outlined class="input-icon" /></template>
              </a-input>
              <div class="captcha-box" @click="refreshCaptcha">
                <img :src="captchaImg" alt="验证码" v-if="captchaImg" />
                <div class="captcha-loading" v-else><loading-outlined spin /></div>
                <div class="captcha-refresh"><reload-outlined /></div>
              </div>
            </div>
          </a-form-item>

          <a-form-item class="register-actions">
            <a-button type="primary" html-type="submit" size="large" :loading="loading" block class="register-btn">
              <template #icon v-if="!loading"><user-add-outlined /></template>
              {{ loading ? '注册中...' : '立即注册' }}
            </a-button>
          </a-form-item>
        </a-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
      <div class="copyright">© 2026 WMS Pro. All rights reserved.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCaptcha, register } from '@/api/auth'
import { getDeptTree } from '@/api/system'
import logger from '@/utils/logger'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaImg = ref('')
const captchaUuid = ref('')
const deptTree = ref([])

const form = reactive({
  username: '', password: '', confirmPassword: '', realName: '',
  phone: '', email: '', deptId: null, captcha: ''
})

const validateConfirmPassword = async (rule, value) => {
  if (value && value !== form.password) {
    throw new Error('两次输入的密码不一致')
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  captchaImg.value = ''
  try {
    const res = await getCaptcha()
    captchaImg.value = res.data.image
    captchaUuid.value = res.data.uuid
  } catch (e) { logger.error('Failed to refresh captcha', { error: e.message }) }
}

const loadDeptTree = async () => {
  try {
    const res = await getDeptTree()
    deptTree.value = res.data
  } catch (e) { logger.error('Failed to load dept tree', { error: e.message }) }
}

const handleRegister = async () => {
  loading.value = true
  logger.action('Register attempt', { username: form.username })
  try {
    await register({ ...form, uuid: captchaUuid.value })
    message.success('注册成功，请登录')
    logger.info('Register success', { username: form.username })
    router.push('/login')
  } catch (e) {
    logger.warn('Register failed', { username: form.username, error: e.message })
    form.captcha = ''
    refreshCaptcha()
  } finally { loading.value = false }
}

onMounted(() => {
  refreshCaptcha()
  loadDeptTree()
  logger.pageView('/register', '注册')
})
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: #0f172a;
}

.bg-animation {
  position: absolute;
  inset: 0;
  z-index: 0;
  
  .bg-gradient {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  }
  
  .bg-pattern {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle at 1px 1px, rgba(255,255,255,0.05) 1px, transparent 0);
    background-size: 40px 40px;
  }
  
  .floating-shapes {
    position: absolute;
    inset: 0;
    overflow: hidden;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      filter: blur(60px);
      opacity: 0.4;
      animation: float 20s infinite ease-in-out;
      
      &.shape-1 {
        width: 400px;
        height: 400px;
        background: linear-gradient(135deg, #6366f1, #8b5cf6);
        top: -100px;
        right: -100px;
        animation-delay: 0s;
      }
      
      &.shape-2 {
        width: 300px;
        height: 300px;
        background: linear-gradient(135deg, #10b981, #14b8a6);
        bottom: -50px;
        left: -50px;
        animation-delay: -5s;
      }
      
      &.shape-3 {
        width: 250px;
        height: 250px;
        background: linear-gradient(135deg, #f59e0b, #ef4444);
        top: 50%;
        left: 30%;
        animation-delay: -10s;
      }
    }
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.9); }
  75% { transform: translate(20px, 30px) scale(1.05); }
}

.register-section {
  width: 100%;
  max-width: 560px;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.register-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  
  .register-header {
    text-align: center;
    margin-bottom: 32px;
    
    .logo-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin: 0 auto 16px;
      
      svg { width: 32px; height: 32px; }
    }
    
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 8px;
    }
    
    p {
      font-size: 15px;
      color: #64748b;
    }
  }
}

.register-form {
  :deep(.ant-form-item-label > label) {
    color: #94a3b8;
    font-weight: 500;
  }
  
  .custom-input, .custom-select {
    height: 46px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    
    &:hover, &:focus {
      border-color: #6366f1;
      background: rgba(255, 255, 255, 0.08);
    }
    
    :deep(.ant-input), :deep(.ant-select-selection-item) {
      background: transparent;
      color: #fff;
      font-size: 14px;
    }
    
    :deep(.ant-input::placeholder), :deep(.ant-select-selection-placeholder) {
      color: #64748b;
    }
    
    .input-icon { color: #64748b; font-size: 16px; }
  }
  
  :deep(.ant-select-selector) {
    background: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    border-radius: 10px !important;
    height: 46px !important;
    
    .ant-select-selection-search-input { color: #fff; }
  }
  
  :deep(.ant-select-arrow) { color: #64748b; }
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
  
  .captcha-input { flex: 1; }
  
  .captcha-box {
    width: 120px;
    height: 46px;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    position: relative;
    background: #fff;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #6366f1;
      .captcha-refresh { opacity: 1; }
    }
    
    img { width: 100%; height: 100%; object-fit: contain; }
    
    .captcha-loading {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      font-size: 18px;
      background: rgba(255, 255, 255, 0.05);
    }
    
    .captcha-refresh {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 16px;
      opacity: 0;
      transition: all 0.3s ease;
    }
  }
}

.register-actions {
  margin-top: 16px;
  
  .register-btn {
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 32px rgba(99, 102, 241, 0.5);
    }
  }
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #64748b;
  
  .login-link {
    color: #6366f1;
    font-weight: 500;
    margin-left: 4px;
    
    &:hover { color: #8b5cf6; }
  }
}

.copyright {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: #475569;
}
</style>
