import request from './request'

// 库存分页查询
export const getStockPage = (params) => request.get('/biz/stock/page', { params })

// 库存预警列表
export const getStockWarningList = (params) => request.get('/biz/stock/warning', { params })

// 出入库记录
export const getStockRecords = (params) => request.get('/biz/stock/record', { params })

// 盘点单分页查询
export const getStockCheckPage = (params) => request.get('/biz/stock/check/page', { params })

// 获取盘点单详情
export const getStockCheckDetail = (id) => request.get(`/biz/stock/check/${id}`)

// 创建盘点单
export const createStockCheck = (data) => request.post('/biz/stock/check', data)

// 确认盘点
export const confirmStockCheck = (id) => request.put(`/biz/stock/check/confirm/${id}`)

// 调拨单分页查询
export const getStockTransferPage = (params) => request.get('/biz/stock/transfer/page', { params })

// 创建调拨单
export const createStockTransfer = (data) => request.post('/biz/stock/transfer', data)

// 报损报溢分页查询
export const getStockAdjustPage = (params) => request.get('/biz/stock/adjust/page', { params })

// 创建报损报溢
export const createStockAdjust = (data) => request.post('/biz/stock/adjust', data)
