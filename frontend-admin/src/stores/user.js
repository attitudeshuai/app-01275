import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)

  // 用户拥有的菜单路径列表
  const allowedPaths = computed(() => {
    if (!userInfo.value?.menus) return []
    const paths = []
    const extractPaths = (menus) => {
      menus.forEach(menu => {
        if (menu.path) paths.push(menu.path)
        if (menu.children) extractPaths(menu.children)
      })
    }
    extractPaths(userInfo.value.menus)
    return paths
  })

  // 用户权限码列表
  const permissions = computed(() => userInfo.value?.permissions || [])

  // 检查是否有某个路径的访问权限
  const hasPathPermission = (path) => {
    if (!userInfo.value) return false
    // 公共路径始终允许
    const publicPaths = ['/dashboard', '/profile', '/login', '/register']
    if (publicPaths.includes(path)) return true
    // 检查是否在允许的菜单路径中
    return allowedPaths.value.some(p => path === p || path.startsWith(p + '/'))
  }

  // 检查是否有某个权限码
  const hasPermission = (perm) => {
    if (!userInfo.value) return false
    return permissions.value.includes(perm) || permissions.value.includes('*:*:*')
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      return res.data
    } catch (e) {
      console.error('Failed to fetch user info', e)
      return null
    }
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const logout = () => {
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    allowedPaths,
    permissions,
    hasPathPermission,
    hasPermission,
    fetchUserInfo,
    setUserInfo,
    logout
  }
})
