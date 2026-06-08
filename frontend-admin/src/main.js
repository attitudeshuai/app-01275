import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import * as Icons from '@ant-design/icons-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import './styles/index.scss'
import logger from './utils/logger'

const app = createApp(App)

// 注册所有图标
Object.keys(Icons).forEach(key => {
  app.component(key, Icons[key])
})

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  logger.error('Vue Error', { error: err.message, info, stack: err.stack })
}

// Ant Design 主题配置
const antdConfig = {
  theme: {
    token: {
      colorPrimary: '#667eea',
      borderRadius: 8,
      fontSize: 14,
      fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
    }
  }
}

app.use(createPinia())
app.use(router)
app.use(Antd, antdConfig)
app.mount('#app')
