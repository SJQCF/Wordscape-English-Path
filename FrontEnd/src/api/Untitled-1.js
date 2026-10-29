// src/composables/useStudyData.js
import { ref, onMounted, watch } from 'vue'
import { getStudyDays, getTotalStudyDays, getUserType } from '@/api/study'

// 全局缓存，避免重复请求
let cache = null
let cacheTime = 0

export function useStudyData() {
  // 状态定义
  const studyDays = ref(0)
  const totalStudyDays = ref(0)
  const userType = ref(2) // 默认四级
  const loading = ref(false)

  // 数据请求函数
  const fetchData = async () => {
    // 缓存有效时间（如5分钟）
    const CACHE_DURATION = 5 * 60 * 1000
    const now = Date.now()

    // 如果缓存存在且未过期，直接使用缓存
    if (cache && (now - cacheTime < CACHE_DURATION)) {
      studyDays.value = cache.studyDays
      totalStudyDays.value = cache.totalStudyDays
      userType.value = cache.userType
      return
    }

    loading.value = true
    const userId = 1 // 实际从登录信息获取

    try {
      const [studyDaysResult, totalStudyDaysResult, userTypeResult] = await Promise.all([
        getStudyDays(userId),
        getTotalStudyDays(userId),
        getUserType(userId)
      ])

      // 更新状态
      if (studyDaysResult.code) studyDays.value = studyDaysResult.data
      if (totalStudyDaysResult.code) totalStudyDays.value = totalStudyDaysResult.data
      if (userTypeResult.code) userType.value = userTypeResult.data

      // 更新缓存
      cache = { studyDays: studyDays.value, totalStudyDays: totalStudyDays.value, userType: userType.value }
      cacheTime = now
    } catch (error) {
      console.error('获取学习数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 组件挂载时请求数据
  onMounted(() => {
    fetchData()
  })

  return {
    studyDays,
    totalStudyDays,
    userType,
    loading,
    refresh: fetchData // 提供手动刷新方法
  }
}
