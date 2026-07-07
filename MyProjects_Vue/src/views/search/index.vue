<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowLeft, Search as SearchIcon } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { searchWord } from '@/api/word'
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.back()
}

// 搜索关键词
const searchQuery = ref('')
// 搜索结果
const searchResults = ref([])
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 当前页码
const currentPage = ref(1)
// 每页数量
const pageSize = ref(10)
// 是否还有更多数据
const hasMore = ref(true)

// 执行搜索
const performSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage({
      message: '请输入搜索关键词',
      type: 'warning',
      duration: 3000
    })
    return
  }
  
  loading.value = true
  error.value = ''
  currentPage.value = 1
  searchResults.value = []
  hasMore.value = true
  
  try {
    const response = await searchWord(searchQuery.value.trim(), currentPage.value)
    if (response.code && response.data) {
      searchResults.value = response.data
      // 检查是否还有更多数据
      hasMore.value = response.data.length >= pageSize.value
    } else {
      searchResults.value = []
      hasMore.value = false
    }
  } catch (err) {
    console.error('搜索单词失败:', err)
    error.value = '搜索失败，请稍后重试'
    ElMessage({
      message: '搜索失败，请稍后重试',
      type: 'error',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  
  loading.value = true
  currentPage.value++
  
  try {
    const response = await searchWord(searchQuery.value.trim(), currentPage.value)
    if (response.code && response.data && response.data.length > 0) {
      searchResults.value = [...searchResults.value, ...response.data]
      hasMore.value = response.data.length >= pageSize.value
    } else {
      hasMore.value = false
    }
  } catch (err) {
    console.error('加载更多失败:', err)
    ElMessage({
      message: '加载更多失败，请稍后重试',
      type: 'error',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

// 监听滚动事件
const handleScroll = () => {
  const scrollContainer = document.querySelector('.search-results')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  
  // 当滚动到底部100px以内时，加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value && !loading.value) {
    loadMore()
  }
}

// 点击搜索结果，跳转到单词详情页面
const goToWordDetail = (word) => {
  console.log('跳转到单词详情页，单词数据:', word)
  router.push({
    path: '/showWord',
    query: { 
      id: word.id,
      word: word.word 
    }
  })
}

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时，检查是否有搜索关键词
onMounted(() => {
  if (route.query.q) {
    searchQuery.value = route.query.q
    performSearch()
  }
  
  // 添加滚动监听
  const scrollContainer = document.querySelector('.search-results')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
  
  // 初始化主题
  initTheme()
})

// 组件卸载时，移除滚动监听
onUnmounted(() => {
  const scrollContainer = document.querySelector('.search-results')
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="search-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <div class="search-input-container">
          <el-input
            v-model="searchQuery"
            placeholder="请输入英文单词或中文释义"
            prefix-icon="Search"
            clearable
            @keyup.enter="performSearch"
            class="search-input"
          />
          <el-button type="primary" @click="performSearch" class="search-button" :loading="loading">
            搜索
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 搜索结果容器 -->
    <div class="search-results">
      <!-- 加载状态 -->
      <div v-if="loading && currentPage === 1" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>搜索中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="performSearch" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!loading && searchResults.length === 0 && currentPage === 1" class="empty-container">
        <p>暂无搜索结果</p>
        <p class="empty-hint">请尝试其他关键词</p>
      </div>
      
      <!-- 搜索结果 -->
      <div v-else class="results-list">
        <div 
          v-for="word in searchResults" 
          :key="word.id" 
          class="result-item"
          @click="goToWordDetail(word)"
        >
          <h3 class="word-title">{{ word.word }}</h3>
          <div class="word-phonetic">{{ word.phonetic || word.phoneticSymbol || word.yinbiao || '' }}</div>
          <div class="word-definition">{{ word.definition || word.chinese || word.zhongwen || '' }}</div>
        </div>
        
        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <el-loading v-if="loading && currentPage > 1" type="spinner" color="#409EFF" />
          <p v-else-if="!loading && hasMore">下拉加载更多</p>
        </div>
        
        <!-- 没有更多数据 -->
        <div v-else-if="searchResults.length > 0" class="no-more">
          <p>没有更多数据了</p>
        </div>
      </div>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 整体容器 */
.search-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  /* 适配刘海屏 */
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  /* 确保主容器完全不滚动 */
  overflow: hidden;
  /* 固定定位，确保不会出现外部滚动 */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

/* 顶部固定区域 */
.top-sticky-section {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--bg-primary);
  z-index: 100;
  padding: env(safe-area-inset-top) 1.2rem 0;
  /* 移除阴影，保持视觉一致性 */
  box-shadow: none;
  height: auto;
  min-height: 90px;
  transition: all 0.3s ease;
}

/* 头部导航 */
.course-header {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 1.5rem 0;
  gap: 1rem;
}

.back-button {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  padding: 0;
  font-size: 1rem;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  justify-content: center;
  cursor: pointer;
  transition: color 0.3s ease, background-color 0.3s ease;
}

.back-button:hover {
  background-color: transparent;
}

/* 搜索输入容器 */
.search-input-container {
  flex: 1;
  display: flex;
  gap: 0.8rem;
}

.search-input {
  flex: 1;
  border-radius: 8px;
  height: 44px;
  font-size: 1rem;
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color) !important;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper) {
  background-color: var(--bg-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color) !important;
}

.search-input :deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

.search-input :deep(.el-input__placeholder) {
  color: var(--text-tertiary) !important;
}

.search-button {
  border-radius: 8px;
  white-space: nowrap;
  height: 44px;
  padding: 0 1.2rem;
  font-size: 1rem;
  background-color: #FF9800 !important;
  border-color: #FF9800 !important;
}

.search-button:hover {
  background-color: #FF9800 !important;
  border-color: #FF9800 !important;
}

/* 搜索结果容器 */
.search-results {
  flex: 1;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  padding: 100px 1rem 1rem;
  box-sizing: border-box;
  overflow-y: auto;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background-color: rgba(245, 108, 108, 0.1);
  border-radius: 8px;
  margin: 1rem 0;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
}

.empty-hint {
  color: var(--text-tertiary);
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

/* 搜索结果列表 */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 搜索结果项 */
.result-item {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
}

.result-item:hover {
  box-shadow: none;
  transform: none;
}

.word-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--primary-color);
  margin: 0 0 0.5rem 0;
}

.word-phonetic {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  font-style: italic;
  margin: 0 0 0.5rem 0;
}

.word-definition {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.4;
  margin: 0;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  padding: 1rem;
}

/* 没有更多数据 */
.no-more {
  display: flex;
  justify-content: center;
  padding: 1rem;
  color: var(--text-tertiary);
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .course-header {
    padding: 1.2rem 0;
    gap: 0.5rem;
  }
  
  .search-results {
    padding: 90px 0.8rem 1rem;
  }
  
  .result-item {
    padding: 0.8rem;
  }
  
  .word-title {
    font-size: 1.1rem;
  }
  
  .word-phonetic {
    font-size: 0.85rem;
  }
  
  .word-definition {
    font-size: 0.9rem;
  }
}
</style>