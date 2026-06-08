import request from './request'

// 采购单分页查询
export const getPurchasePage = (params) => request.get('/biz/purchase/page', { params })

// 获取采购单详情
export const getPurchaseDetail = (id) => request.get(`/biz/purchase/${id}`)

// 创建采购申请
export const applyPurchase = (data) => request.post('/biz/purchase', data)

// 更新采购单
export const updatePurchase = (data) => request.put('/biz/purchase', data)

// 删除采购单
export const deletePurchase = (id) => request.delete(`/biz/purchase/${id}`)

// 审批采购单
export const approvePurchase = (id, approved, remark) => request.put('/biz/purchase/approve', { id, approved, remark })

// 采购入库
export const inboundPurchase = (id) => request.put('/biz/purchase/inbound', { id })
