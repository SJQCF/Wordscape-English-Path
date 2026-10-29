/**
 * 认证相关工具函数
 */

// 获取当前用户信息
export const getCurrentUser = () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      return JSON.parse(userInfoStr)
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 获取当前用户ID
export const getCurrentUserId = () => {
  const userInfo = getCurrentUser()
  if (!userInfo || userInfo.id == null || userInfo.id === '') {
    return null
  }
  const id = Number(userInfo.id)
  if (Number.isFinite(id) && id > 0) {
    return id
  }
  return null
}

// 检查用户是否已登录
export const isLoggedIn = () => {
  const userInfo = getCurrentUser()
  return userInfo !== null
}