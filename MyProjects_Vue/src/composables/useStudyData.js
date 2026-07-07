// src/composables/useStudyData.js
import { ref, onMounted } from 'vue'
import { getStudyDays, getTotalStudyDays } from '@/api/user'
import { getTodayNewWordsNum, getTodayReviewWordsNum, getStudiedWordsNum, getStudyPlan, getReviewStudyPlan } from '@/api/study'
import { getUserType } from '@/api/user'
import { getCurrentUserId } from '@/utils/auth.js'

// 全局缓存，避免重复请求
let cache = null
let cacheTime = 0

// 清除缓存的函数，用于在用户类型变更后刷新数据
export function clearStudyDataCache() {
  cache = null
  cacheTime = 0
}

export function useStudyData() {
  // 状态定义
  const studyDays = ref(0)
  const totalStudyDays = ref(0)
  const userType = ref(2) // 默认四级
  const newWords = ref(0) // 今日新学单词
  const reviewedWords = ref(0) // 今日复习单词
  const studiedWords = ref(0) // 已学习单词总数
  const studyPlan = ref(10) // 每日学习计划数量
  const reviewStudyPlan = ref(10) // 每日复习计划数量
  const loading = ref(false)

  // 数据请求函数
  const fetchData = async (forceRefresh = false) => {
    // 缓存有效时间（如5分钟）
    const CACHE_DURATION = 5 * 60 * 1000
    const now = Date.now()

    // 首先尝试从本地存储获取学习计划设置
    const savedPlan = localStorage.getItem('studyPlan')
    if (savedPlan) {
      try {
        const parsedPlan = JSON.parse(savedPlan)
        studyPlan.value = parsedPlan.dailyNewWords || 10
        reviewStudyPlan.value = parsedPlan.dailyReviewWords || 10
      } catch (error) {
        console.error('解析本地学习计划失败:', error)
      }
    }

    // 如果缓存存在且未过期，且不是强制刷新，直接使用缓存
    if (!forceRefresh && cache && (now - cacheTime < CACHE_DURATION)) {
      studyDays.value = cache.studyDays
      totalStudyDays.value = cache.totalStudyDays
      userType.value = cache.userType
      newWords.value = cache.newWords
      reviewedWords.value = cache.reviewedWords
      studiedWords.value = cache.studiedWords
      // 本地存储的学习计划优先级高于缓存
      return
    }

    loading.value = true
    const userId = getCurrentUserId()

    // 只有当有有效用户ID时才进行API调用
    if (userId) {
      try {
        // 并行请求所有学习相关的API
        const [
          studyDaysResult,
          totalStudyDaysResult,
          userTypeResult,
          newWordsResult,
          reviewedWordsResult,
          studiedWordsResult
        ] = await Promise.all([
          getStudyDays(userId),
          getTotalStudyDays(userId),
          getUserType(userId),
          getTodayNewWordsNum(userId),
          getTodayReviewWordsNum(userId),
          getStudiedWordsNum(userId)
        ])

        // 更新状态
        if (studyDaysResult.code) studyDays.value = studyDaysResult.data
        if (totalStudyDaysResult.code) totalStudyDays.value = totalStudyDaysResult.data
        if (userTypeResult.code) userType.value = userTypeResult.data
        if (newWordsResult.code) newWords.value = newWordsResult.data
        if (reviewedWordsResult.code) reviewedWords.value = reviewedWordsResult.data
        if (studiedWordsResult.code) studiedWords.value = studiedWordsResult.data

        // 更新缓存
        cache = {
          studyDays: studyDays.value,
          totalStudyDays: totalStudyDays.value,
          userType: userType.value,
          newWords: newWords.value,
          reviewedWords: reviewedWords.value,
          studiedWords: studiedWords.value,
          studyPlan: studyPlan.value,
          reviewStudyPlan: reviewStudyPlan.value
        }
        cacheTime = now
      } catch (error) {
        console.error('获取学习数据失败:', error)
      } finally {
        loading.value = false
      }
    } else {
      console.log('没有有效用户ID，使用默认学习数据')
      // 没有有效用户ID，使用默认值
      studyDays.value = 0
      totalStudyDays.value = 0
      userType.value = 2 // 默认四级
      newWords.value = 0
      reviewedWords.value = 0
      studiedWords.value = 0
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
    newWords,
    reviewedWords,
    studiedWords,
    studyPlan,
    reviewStudyPlan,
    loading,
    refresh: fetchData // 提供手动刷新方法
  }
}