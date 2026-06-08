<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="bg-animation">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>
    </div>

    <!-- 左侧品牌区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="6" y="14" width="36" height="28" rx="3" stroke="currentColor" stroke-width="3"/>
              <path d="M14 14V10C14 7.79086 15.7909 6 18 6H30C32.2091 6 34 7.79086 34 10V14" stroke="currentColor" stroke-width="3"/>
              <path d="M6 24H42" stroke="currentColor" stroke-width="3"/>
              <circle cx="24" cy="24" r="4" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="brand-name">WMS Pro</h1>
        </div>
        <h2 class="brand-title">智能仓储管理系统</h2>
        <p class="brand-subtitle">Intelligent Warehouse Management System</p>
        
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <check-circle-outlined />
            </div>
            <div class="feature-text">
              <h4>全流程管理</h4>
              <p>采购、销售、库存一体化</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <thunderbolt-outlined />
            </div>
            <div class="feature-text">
              <h4>实时数据分析</h4>
              <p>智能报表，决策支持</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <safety-certificate-outlined />
            </div>
            <div class="feature-text">
              <h4>安全可靠</h4>
              <p>多级权限，数据加密</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-section">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户继续使用</p>
        </div>

        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          layout="vertical"
          @finish="handleLogin"
          class="login-form"
        >
          <a-form-item name="username" label="用户名">
            <a-input
              v-model:value="form.username"
              placeholder="请输入用户名"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <user-outlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" label="密码">
            <a-input-password
              v-model:value="form.password"
              placeholder="请输入密码"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <lock-outlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="captcha" label="验证码">
            <div class="captcha-wrapper">
              <a-input
                v-model:value="form.captcha"
                placeholder="请输入验证码"
                size="large"
                class="custom-input captcha-input"
                @pressEnter="handleLogin"
              >
                <template #prefix>
                  <safety-outlined class="input-icon" />
                </template>
              </a-input>
              <div class="captcha-box" @click="refreshCaptcha">
                <img :src="captchaImg" alt="验证码" v-if="captchaImg" />
                <div class="captcha-loading" v-else>
                  <loading-outlined spin />
                </div>
                <div class="captcha-refresh">
                  <reload-outlined />
                </div>
              </div>
            </div>
          </a-form-item>

          <a-form-item class="login-actions">
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="login-btn"
            >
              <template #icon v-if="!loading">
                <login-outlined />
              </template>
              {{ loading ? '登录中...' : '立即登录' }}
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          <div class="divider">
            <span>快速体验</span>
          </div>
          <div class="demo-accounts">
            <div 
              class="account-card" 
              :class="{ active: selectedAccount === 'admin' }"
              @click="fillAccount('admin', 'admin123')"
            >
              <div class="account-glow admin"></div>
              <div class="account-content">
                <div class="account-avatar admin">
                  <crown-outlined />
                </div>
                <div class="account-info">
                  <span class="account-role">超级管理员</span>
                  <span class="account-perms">
                    <span class="perm-tag">全部权限</span>
                  </span>
                </div>
                <div class="account-action">
                  <right-outlined />
                </div>
              </div>
              <div class="account-credentials">
                <span><user-outlined /> admin</span>
                <span><lock-outlined /> admin123</span>
              </div>
            </div>
            <div 
              class="account-card" 
              :class="{ active: selectedAccount === 'purchase' }"
              @click="fillAccount('purchase', 'admin123')"
            >
              <div class="account-glow purchase"></div>
              <div class="account-content">
                <div class="account-avatar purchase">
                  <shopping-cart-outlined />
                </div>
                <div class="account-info">
                  <span class="account-role">采购专员</span>
                  <span class="account-perms">
                    <span class="perm-tag">采购管理</span>
                    <span class="perm-tag">库存查看</span>
                  </span>
                </div>
                <div class="account-action">
                  <right-outlined />
                </div>
              </div>
              <div class="account-credentials">
                <span><user-outlined /> purchase</span>
                <span><lock-outlined /> admin123</span>
              </div>
            </div>
            <div 
              class="account-card" 
              :class="{ active: selectedAccount === 'sales' }"
              @click="fillAccount('sales', 'admin123')"
            >
              <div class="account-glow sales"></div>
              <div class="account-content">
                <div class="account-avatar sales">
                  <shop-outlined />
                </div>
                <div class="account-info">
                  <span class="account-role">销售专员</span>
                  <span class="account-perms">
                    <span class="perm-tag">销售管理</span>
                    <span class="perm-tag">客户管理</span>
                  </span>
                </div>
                <div class="account-action">
                  <right-outlined />
                </div>
              </div>
              <div class="account-credentials">
                <span><user-outlined /> sales</span>
                <span><lock-outlined /> admin123</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="copyright">
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
        © 2026 WMS Pro. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCaptcha, login } from '@/api/auth'
import logger from '@/utils/logger'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaImg = ref('')
const captchaUuid = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const selectedAccount = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  captchaImg.value = ''
  try {
    const res = await getCaptcha()
    captchaImg.value = res.data.image
    captchaUuid.value = res.data.uuid
    logger.debug('Captcha refreshed')
  } catch (e) {
    logger.error('Failed to refresh captcha', { error: e.message })
  }
}

const fillAccount = (username, password) => {
  form.username = username
  form.password = password
  selectedAccount.value = username
  // 清除表单验证错误
  formRef.value?.clearValidate()
  const roleName = username === 'admin' ? '管理员' : username === 'purchase' ? '采购员' : '销售员'
  message.success(`已选择 ${roleName} 账号`)
}

const handleLogin = async () => {
  loading.value = true
  logger.action('Login attempt', { username: form.username })
  try {
    const res = await login({
      ...form,
      uuid: captchaUuid.value
    })
    localStorage.setItem('token', res.data.token)
    message.success('登录成功，欢迎回来！')
    logger.info('Login success', { username: form.username })
    router.push('/')
  } catch (e) {
    // 错误消息已在 request.js 拦截器中显示，这里只需刷新验证码
    logger.warn('Login failed', { username: form.username, error: e.message })
    form.captcha = ''
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 只在用户主动访问登录页时刷新验证码，不清除 token
  // token 的清除由 request.js 中的 401 响应处理
  refreshCaptcha()
  logger.pageView('/login', '登录')
})
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #0f172a;
}

// 动态背景
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
        left: -100px;
        animation-delay: 0s;
      }
      
      &.shape-2 {
        width: 300px;
        height: 300px;
        background: linear-gradient(135deg, #06b6d4, #3b82f6);
        top: 50%;
        right: -50px;
        animation-delay: -5s;
      }
      
      &.shape-3 {
        width: 250px;
        height: 250px;
        background: linear-gradient(135deg, #ec4899, #f43f5e);
        bottom: -50px;
        left: 30%;
        animation-delay: -10s;
      }
      
      &.shape-4 {
        width: 200px;
        height: 200px;
        background: linear-gradient(135deg, #10b981, #14b8a6);
        top: 30%;
        left: 20%;
        animation-delay: -15s;
      }
      
      &.shape-5 {
        width: 350px;
        height: 350px;
        background: linear-gradient(135deg, #f59e0b, #ef4444);
        bottom: 20%;
        right: 30%;
        animation-delay: -7s;
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

// 左侧品牌区域
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
  
  @media (max-width: 1024px) {
    display: none;
  }
}

.brand-content {
  max-width: 500px;
  color: #fff;
  
  .brand-logo {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    
    .logo-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      
      svg {
        width: 32px;
        height: 32px;
      }
    }
    
    .brand-name {
      font-size: 32px;
      font-weight: 700;
      background: linear-gradient(135deg, #fff, #94a3b8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      letter-spacing: -0.5px;
    }
  }
  
  .brand-title {
    font-size: 42px;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #fff 0%, #cbd5e1 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .brand-subtitle {
    font-size: 16px;
    color: #64748b;
    margin-bottom: 48px;
    letter-spacing: 1px;
  }
  
  .features {
    display: flex;
    flex-direction: column;
    gap: 24px;
    
    .feature-item {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 16px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.08);
        transform: translateX(8px);
      }
      
      .feature-icon {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #6366f1, #8b5cf6);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        color: #fff;
        flex-shrink: 0;
      }
      
      .feature-text {
        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #fff;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 14px;
          color: #94a3b8;
          margin: 0;
        }
      }
    }
  }
}

// 右侧登录区域
.login-section {
  width: 520px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(20px);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  
  @media (max-width: 1024px) {
    width: 100%;
    border-left: none;
    background: transparent;
  }
}

.login-card {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  
  .login-header {
    text-align: center;
    margin-bottom: 36px;
    
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

.login-form {
  :deep(.ant-form-item-label > label) {
    color: #94a3b8;
    font-weight: 500;
  }
  
  .custom-input {
    height: 50px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    
    &:hover, &:focus {
      border-color: #6366f1;
      background: rgba(255, 255, 255, 0.08);
    }
    
    :deep(.ant-input) {
      background: transparent !important;
      color: #fff !important;
      font-size: 15px;
      
      &::placeholder {
        color: #64748b !important;
      }
    }
    
    :deep(.ant-input-password-icon) {
      color: #64748b;
      
      &:hover {
        color: #94a3b8;
      }
    }
    
    .input-icon {
      color: #64748b;
      font-size: 18px;
    }
  }
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
  
  .captcha-input {
    flex: 1;
  }
  
  .captcha-box {
    width: 140px;
    min-width: 140px;
    height: 50px;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    position: relative;
    background: #fff;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
    flex-shrink: 0;
    
    &:hover {
      border-color: #6366f1;
      transform: scale(1.02);
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
      
      .captcha-refresh {
        opacity: 1;
      }
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      display: block;
    }
    
    .captcha-loading {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      font-size: 20px;
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
      font-size: 18px;
      opacity: 0;
      transition: all 0.3s ease;
      backdrop-filter: blur(2px);
      
      &::after {
        content: '点击刷新';
        font-size: 12px;
        margin-left: 6px;
      }
    }
  }
}

.login-actions {
  margin-top: 8px;
  
  .login-btn {
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 32px rgba(99, 102, 241, 0.5);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  margin-top: 32px;
  
  .divider {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    &::before, &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
    }
    
    span {
      padding: 0 20px;
      font-size: 13px;
      color: #94a3b8;
      font-weight: 500;
      letter-spacing: 0.5px;
    }
  }
  
  .demo-accounts {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .account-card {
      position: relative;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      overflow: hidden;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.06);
      
      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: linear-gradient(135deg, rgba(255,255,255,0.05) 0%, transparent 50%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      &:hover {
        transform: translateY(-2px);
        border-color: rgba(255, 255, 255, 0.12);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
        
        &::before {
          opacity: 1;
        }
        
        .account-glow {
          opacity: 0.15;
        }
        
        .account-action {
          opacity: 1;
          transform: translateX(0);
        }
        
        .account-credentials {
          max-height: 40px;
          opacity: 1;
          padding: 10px 16px;
        }
      }
      
      &.active {
        border-color: rgba(99, 102, 241, 0.5);
        background: rgba(99, 102, 241, 0.1);
        
        .account-glow {
          opacity: 0.2;
        }
        
        .account-avatar {
          transform: scale(1.05);
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        }
      }
      
      .account-glow {
        position: absolute;
        top: -50%;
        right: -50%;
        width: 200%;
        height: 200%;
        opacity: 0;
        transition: opacity 0.4s ease;
        pointer-events: none;
        
        &.admin {
          background: radial-gradient(circle at 30% 30%, #f59e0b 0%, transparent 50%);
        }
        
        &.purchase {
          background: radial-gradient(circle at 30% 30%, #3b82f6 0%, transparent 50%);
        }
        
        &.sales {
          background: radial-gradient(circle at 30% 30%, #10b981 0%, transparent 50%);
        }
      }
      
      .account-content {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 16px;
        position: relative;
        z-index: 1;
      }
      
      .account-avatar {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        color: #fff;
        flex-shrink: 0;
        transition: all 0.3s ease;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          inset: -2px;
          border-radius: 16px;
          background: inherit;
          filter: blur(8px);
          opacity: 0.5;
          z-index: -1;
        }
        
        &.admin {
          background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
        }
        
        &.purchase {
          background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
        }
        
        &.sales {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }
      }
      
      .account-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 6px;
        
        .account-role {
          font-size: 15px;
          font-weight: 600;
          color: #fff;
          letter-spacing: 0.3px;
        }
        
        .account-perms {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
          
          .perm-tag {
            font-size: 11px;
            padding: 2px 8px;
            background: rgba(255, 255, 255, 0.08);
            border-radius: 20px;
            color: #94a3b8;
            font-weight: 500;
          }
        }
      }
      
      .account-action {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 10px;
        color: #fff;
        font-size: 14px;
        opacity: 0;
        transform: translateX(-8px);
        transition: all 0.3s ease;
      }
      
      .account-credentials {
        display: flex;
        justify-content: center;
        gap: 24px;
        max-height: 0;
        opacity: 0;
        overflow: hidden;
        background: rgba(0, 0, 0, 0.2);
        border-top: 1px solid rgba(255, 255, 255, 0.05);
        transition: all 0.3s ease;
        
        span {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #94a3b8;
          font-family: 'SF Mono', 'Monaco', 'Inconsolata', monospace;
          
          .anticon {
            font-size: 12px;
            color: #64748b;
          }
        }
      }
    }
  }
}

.copyright {
  text-align: center;
  margin-top: 40px;
  font-size: 13px;
  color: #475569;
  
  .register-link {
    margin-bottom: 12px;
    font-size: 14px;
    
    span {
      color: #64748b;
    }
    
    a {
      color: #6366f1;
      font-weight: 500;
      margin-left: 4px;
      
      &:hover {
        color: #8b5cf6;
      }
    }
  }
}
</style>
