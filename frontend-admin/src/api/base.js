import request from './request'

// 供应商
export const getSupplierPage = (params) => request.get('/biz/supplier/page', { params })
export const getSupplierList = () => request.get('/biz/supplier/list')
export const saveSupplier = (data) => request.post('/biz/supplier', data)
export const updateSupplier = (data) => request.put('/biz/supplier', data)
export const deleteSupplier = (id) => request.delete(`/biz/supplier/${id}`)

// 客户
export const getCustomerPage = (params) => request.get('/biz/customer/page', { params })
export const getCustomerList = () => request.get('/biz/customer/list')
export const saveCustomer = (data) => request.post('/biz/customer', data)
export const updateCustomer = (data) => request.put('/biz/customer', data)
export const deleteCustomer = (id) => request.delete(`/biz/customer/${id}`)

// 商品
export const getGoodsPage = (params) => request.get('/biz/goods/page', { params })
export const getGoodsList = () => request.get('/biz/goods/list')
export const getGoodsByIds = (ids) => request.post('/biz/goods/byIds', ids)
export const getGoods = (id) => request.get(`/biz/goods/${id}`)
export const saveGoods = (data) => request.post('/biz/goods', data)
export const updateGoods = (data) => request.put('/biz/goods', data)
export const deleteGoods = (id) => request.delete(`/biz/goods/${id}`)
export const getCategoryTree = () => request.get('/biz/goods/category/tree')
export const saveCategory = (data) => request.post('/biz/goods/category', data)
export const updateCategory = (data) => request.put('/biz/goods/category', data)
export const deleteCategory = (id) => request.delete(`/biz/goods/category/${id}`)

// 仓库
export const getWarehousePage = (params) => request.get('/biz/warehouse/page', { params })
export const getWarehouseList = () => request.get('/biz/warehouse/list')
export const saveWarehouse = (data) => request.post('/biz/warehouse', data)
export const updateWarehouse = (data) => request.put('/biz/warehouse', data)
export const deleteWarehouse = (id) => request.delete(`/biz/warehouse/${id}`)
