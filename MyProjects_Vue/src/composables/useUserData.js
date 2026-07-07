import { ref, onMounted } from 'vue'
import { getUserSignature, getUserName, getUserAvatar } from '@/api/user'
import { getCurrentUserId } from '@/utils/auth.js'

// 全局缓存，避免重复请求
let userCache = null
let userCacheTime = 0

export function useUserData() {
  // 状态定义
  const userSignature = ref('')
  const userName = ref('')
  const userAvatar = ref('') // 添加用户头像状态
  const loading = ref(false)

  // 数据请求函数
  const fetchUserData = async (forceRefresh = false) => {
    // 缓存有效时间（如5分钟）
    const CACHE_DURATION = 5 * 60 * 1000
    const now = Date.now()

    // 如果缓存存在且未过期，且不强制刷新，直接使用缓存
    if (userCache && (now - userCacheTime < CACHE_DURATION) && !forceRefresh) {
      userSignature.value = userCache.userSignature
      userName.value = userCache.userName
      userAvatar.value = userCache.userAvatar // 使用缓存的头像
      return
    }

    loading.value = true
    const userId = getCurrentUserId()
    try {
      // 并行请求用户相关的API
      const [signatureResult, nameResult, avatarResult] = await Promise.all([
        getUserSignature(userId),
        getUserName(userId),
        getUserAvatar(userId) // 添加获取头像的API请求
      ])

      // 更新状态
      if (signatureResult.code) userSignature.value = signatureResult.data
      if (nameResult.code) userName.value = nameResult.data
      if (avatarResult.code) userAvatar.value = avatarResult.data || '' // 更新头像

      // 更新缓存
      userCache = {
        userSignature: userSignature.value,
        userName: userName.value,
        userAvatar: userAvatar.value // 添加头像到缓存
      }
      userCacheTime = now
    } catch (error) {
      console.error('获取用户数据失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  // 组件挂载时请求数据
  onMounted(() => {
    fetchUserData()
  })

  return {
    userSignature,
    userName,
    userAvatar, // 返回用户头像
    loading,
    refresh: () => fetchUserData(true) // 提供手动刷新方法，强制绕过缓存
  }
}