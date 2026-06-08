import request from './request'

// 用户管理
export const getUserPage = (params) => request.get('/sys/user/page', { params })
export const getUserList = (params) => request.get('/sys/user/page', { params })
export const getUser = (id) => request.get(`/sys/user/${id}`)
export const saveUser = (data) => request.post('/sys/user', data)
export const updateUser = (data) => request.put('/sys/user', data)
export const deleteUser = (id) => request.delete(`/sys/user/${id}`)
export const updateUserStatus = (id, status) => request.put('/sys/user/status', null, { params: { id, status } })
export const resetPassword = (id) => request.put(`/sys/user/reset-password/${id}`)
export const updateUserInfo = (data) => request.put('/sys/user/info', data)
export const changeUserPassword = (data) => request.put('/auth/password', data)

// 角色管理
export const getRolePage = (params) => request.get('/sys/role/page', { params })
export const getRoleList = () => request.get('/sys/role/list')
export const getRole = (id) => request.get(`/sys/role/${id}`)
export const saveRole = (data) => request.post('/sys/role', data)
export const updateRole = (data) => request.put('/sys/role', data)
export const deleteRole = (id) => request.delete(`/sys/role/${id}`)
export const assignMenus = (data) => request.put('/sys/role/menu', data)
export const getRoleMenus = (roleId) => request.get(`/sys/role/menu/${roleId}`)
export const saveRoleMenus = (roleId, menuIds) => request.put('/sys/role/menu', { roleId, menuIds })

// 菜单管理
export const getMenuTree = () => request.get('/sys/menu/tree')
export const saveMenu = (data) => request.post('/sys/menu', data)
export const updateMenu = (data) => request.put('/sys/menu', data)
export const deleteMenu = (id) => request.delete(`/sys/menu/${id}`)

// 部门管理
export const getDeptTree = () => request.get('/sys/dept/tree')
export const saveDept = (data) => request.post('/sys/dept', data)
export const updateDept = (data) => request.put('/sys/dept', data)
export const deleteDept = (id) => request.delete(`/sys/dept/${id}`)

// 操作日志
export const getLogPage = (params) => request.get('/sys/log/page', { params })
export const getLogList = (params) => request.get('/sys/log/page', { params })
export const clearLog = () => request.delete('/sys/log/clear')

// 数据备份
export const getBackupList = (params) => request.get('/sys/backup/page', { params })
export const createBackup = () => request.post('/sys/backup')
export const restoreBackup = (id) => request.post(`/sys/backup/restore/${id}`)
export const deleteBackup = (id) => request.delete(`/sys/backup/${id}`)
export const downloadBackup = (id) => request.get(`/sys/backup/download/${id}`, { responseType: 'blob' })

// 系统通知
export const getUnreadNotifications = (limit = 10) => request.get('/sys/notification/unread', { params: { limit } })
export const getUnreadNotificationCount = () => request.get('/sys/notification/unread/count')
export const markNotificationAsRead = (id) => request.put(`/sys/notification/read/${id}`)
export const markAllNotificationsAsRead = () => request.put('/sys/notification/read/all')

// 用户偏好设置
export const getGoodsHistory = (type) => request.get(`/sys/preference/goods-history/${type}`)
export const saveGoodsHistory = (type, goodsIds) => request.post(`/sys/preference/goods-history/${type}`, { goodsIds })
export const clearGoodsHistory = (type) => request.delete(`/sys/preference/goods-history/${type}`)
