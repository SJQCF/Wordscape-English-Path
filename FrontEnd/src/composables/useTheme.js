import { ref, onMounted, watch } from 'vue'
import request from '@/utils/axios.js'
import { getCurrentUserId } from '@/utils/auth.js'

export function useTheme() {
  const isDarkMode = ref(false)
  const loading = ref(false)
  const error = ref('')
  
  // 初始化主题
  const initTheme = async () => {
    try {
      loading.value = true
      
      // 先从本地缓存读取
      const cachedMode = localStorage.getItem('nightMode')
      if (cachedMode !== null) {
        isDarkMode.value = cachedMode === 'true'
        applyTheme()
        return
      }
      
      // 从后端获取用户设置
      const userId = getCurrentUserId()
      if (userId) {
        const response = await request.get(`/user/isNightMode/${userId}`)
        isDarkMode.value = response.data.data
      }
      
      // 缓存到本地
      localStorage.setItem('nightMode', isDarkMode.value)
      applyTheme()
    } catch (err) {
      console.error('初始化主题失败:', err)
      error.value = '初始化主题失败'
      // 失败时使用默认值
      isDarkMode.value = false
      applyTheme()
    } finally {
      loading.value = false
    }
  }
  
  // 应用主题
  const applyTheme = () => {
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
  
  // 切换主题
  const toggleTheme = async (mode) => {
    try {
      loading.value = true
      isDarkMode.value = mode
      
      // 缓存到本地
      localStorage.setItem('nightMode', mode)
      applyTheme()
      
      // 同步到后端
      const userId = getCurrentUserId()
      if (userId) {
        await request.post(`/user/isNightMode/${userId}`, null, {
          params: { isNightMode: mode }
        })
      }
    } catch (err) {
      console.error('切换主题失败:', err)
      error.value = '切换主题失败'
    } finally {
      loading.value = false
    }
  }
  
  // 监听主题变化
  watch(isDarkMode, (newValue) => {
    applyTheme()
  })
  
  return { isDarkMode, loading, error, initTheme, toggleTheme }
}