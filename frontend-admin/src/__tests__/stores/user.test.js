import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../../stores/user'

// Mock API
vi.mock('../../api/auth', () => ({
  getUserInfo: vi.fn(() => Promise.resolve({
    data: {
      id: 1,
      username: 'admin',
      realName: '管理员',
      roles: ['SUPER_ADMIN'],
      menus: []
    }
  }))
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('should initialize with empty user info', () => {
    const store = useUserStore()
    expect(store.userInfo).toBeNull()
  })

  it('should fetch user info', async () => {
    const store = useUserStore()
    await store.fetchUserInfo()
    expect(store.userInfo).not.toBeNull()
    expect(store.userInfo.username).toBe('admin')
  })

  it('should logout and clear user info', () => {
    const store = useUserStore()
    store.userInfo = { id: 1, username: 'admin' }
    localStorage.setItem('token', 'test-token')
    
    store.logout()
    
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('should set user info', () => {
    const store = useUserStore()
    const userInfo = { id: 1, username: 'test', realName: 'Test User' }
    
    store.setUserInfo(userInfo)
    
    expect(store.userInfo).toEqual(userInfo)
  })
})
