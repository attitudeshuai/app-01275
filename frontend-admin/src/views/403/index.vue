<template>
  <div class="forbidden-container">
    <div class="forbidden-content">
      <div class="error-code">403</div>
      <div class="error-title">无权限访问</div>
      <div class="error-desc">抱歉，您没有权限访问此页面，请联系管理员</div>
      <div class="error-actions">
        <a-button type="primary" size="large" @click="goHome">
          <template #icon><HomeOutlined /></template>
          返回首页
        </a-button>
        <a-button size="large" @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回上页
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const goHome = () => router.push('/dashboard')
const goBack = () => router.go(-1)
</script>

<style lang="scss" scoped>
.forbidden-container {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.forbidden-content {
  text-align: center;
  padding: 40px;
  
  .error-code {
    font-size: 120px;
    font-weight: 700;
    background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1;
    margin-bottom: 16px;
  }
  
  .error-title {
    font-size: 28px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 12px;
  }
  
  .error-desc {
    font-size: 16px;
    color: #64748b;
    margin-bottom: 32px;
  }
  
  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    
    :deep(.ant-btn-primary) {
      background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
      border: none;
      height: 44px;
      padding: 0 28px;
      border-radius: 8px;
      font-weight: 500;
      
      &:hover {
        background: linear-gradient(135deg, #fbbf24 0%, #f87171 100%);
      }
    }
    
    :deep(.ant-btn-default) {
      height: 44px;
      padding: 0 28px;
      border-radius: 8px;
      font-weight: 500;
    }
  }
}
</style>
