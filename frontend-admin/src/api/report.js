import request from './request'

export const getDashboard = () => request.get('/report/dashboard')
export const getSaleRank = (params) => request.get('/report/sale/rank', { params })
export const getPurchaseTrend = (params) => request.get('/report/purchase/trend', { params })
export const getStockWarningReport = () => request.get('/report/stock/warning')
export const getProfitAnalysis = (params) => request.get('/report/profit', { params })
export const getProfitReport = (params) => request.get('/report/profit', { params })
