import request from './request'

export const getCaptcha = () => request.get('/auth/captcha')

export const login = (data) => request.post('/auth/login', data)

export const register = (data) => request.post('/auth/register', data)

export const logout = () => request.post('/auth/logout')

export const getUserInfo = () => request.get('/auth/info')

export const updatePassword = (data) => request.put('/auth/password', data)
