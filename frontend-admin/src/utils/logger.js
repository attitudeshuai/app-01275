/**
 * 前端日志系统
 * 支持不同级别的日志记录，可扩展为发送到后端
 */

const LOG_LEVELS = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3
}

const currentLevel = import.meta.env.DEV ? LOG_LEVELS.DEBUG : LOG_LEVELS.INFO

const formatTime = () => {
  return new Date().toISOString()
}

const formatMessage = (level, message, data) => {
  const time = formatTime()
  const dataStr = data ? ` | ${JSON.stringify(data)}` : ''
  return `[${time}] [${level}] ${message}${dataStr}`
}

const logger = {
  debug(message, data = null) {
    if (currentLevel <= LOG_LEVELS.DEBUG) {
      console.debug(formatMessage('DEBUG', message, data))
    }
  },

  info(message, data = null) {
    if (currentLevel <= LOG_LEVELS.INFO) {
      console.info(formatMessage('INFO', message, data))
    }
  },

  warn(message, data = null) {
    if (currentLevel <= LOG_LEVELS.WARN) {
      console.warn(formatMessage('WARN', message, data))
    }
  },

  error(message, data = null) {
    if (currentLevel <= LOG_LEVELS.ERROR) {
      console.error(formatMessage('ERROR', message, data))
    }
    // 可扩展：发送错误日志到后端
    // this.sendToServer('ERROR', message, data)
  },

  // 记录用户操作
  action(action, data = null) {
    this.info(`User Action: ${action}`, data)
  },

  // 记录API请求
  api(method, url, data = null) {
    this.debug(`API ${method} ${url}`, data)
  },

  // 记录API响应
  apiResponse(method, url, status, duration) {
    const level = status >= 400 ? 'warn' : 'debug'
    this[level](`API Response ${method} ${url}`, { status, duration: `${duration}ms` })
  },

  // 记录页面访问
  pageView(path, title) {
    this.info('Page View', { path, title })
  },

  // 性能日志
  performance(name, duration) {
    this.debug(`Performance: ${name}`, { duration: `${duration}ms` })
  }
}

export default logger
