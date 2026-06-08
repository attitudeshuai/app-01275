import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'
import logger from '@/utils/logger'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 记录请求开始时间
    config.metadata = { startTime: Date.now() }
    logger.api(config.method?.toUpperCase(), config.url, config.data || config.params)
    return config
  },
  error => {
    logger.error('Request Error', { message: error.message })
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const duration = Date.now() - (response.config.metadata?.startTime || Date.now())
    logger.apiResponse(
      response.config.method?.toUpperCase(),
      response.config.url,
      response.status,
      duration
    )
    
    const res = response.data
    if (res.code !== 200) {
      const errorMsg = res.message || '请求失败'
      logger.warn('API Business Error', { 
        url: response.config.url, 
        code: res.code, 
        message: errorMsg 
      })
      // 显示错误提示 - 延迟执行确保 App 已挂载
      setTimeout(() => {
        message.error(errorMsg)
      }, 0)
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(errorMsg))
    }
    return res
  },
  error => {
    const duration = Date.now() - (error.config?.metadata?.startTime || Date.now())
    const errorMsg = error.response?.data?.message || error.message || '网络错误'
    logger.error('API Error', { 
      url: error.config?.url, 
      status: error.response?.status,
      message: errorMsg,
      duration: `${duration}ms`
    })
    // 显示错误提示 - 延迟执行确保 App 已挂载
    setTimeout(() => {
      message.error(errorMsg)
    }, 0)
    return Promise.reject(new Error(errorMsg))
  }
)

export default request
