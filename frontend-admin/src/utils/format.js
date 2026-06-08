import dayjs from 'dayjs'

/**
 * 格式化日期时间
 * @param {string|Date} value - 日期时间值
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的字符串
 */
export const formatDateTime = (value, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) return '-'
  return dayjs(value).format(format)
}

/**
 * 格式化日期
 * @param {string|Date} value - 日期值
 * @returns {string} 格式化后的字符串
 */
export const formatDate = (value) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param {string|Date} value - 时间值
 * @returns {string} 格式化后的字符串
 */
export const formatTime = (value) => {
  if (!value) return '-'
  return dayjs(value).format('HH:mm:ss')
}

/**
 * 格式化金额
 * @param {number} value - 金额
 * @param {number} decimals - 小数位数，默认 2
 * @returns {string} 格式化后的字符串
 */
export const formatMoney = (value, decimals = 2) => {
  if (value === null || value === undefined) return '¥0.00'
  return '¥' + Number(value).toLocaleString('zh-CN', { 
    minimumFractionDigits: decimals, 
    maximumFractionDigits: decimals 
  })
}
