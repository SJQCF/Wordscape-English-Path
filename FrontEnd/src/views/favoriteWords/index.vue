<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getUserFavoriteWords } from '@/api/word.js'
import { useTheme } from '@/composables/useTheme'
import { getCurrentUserId } from '@/utils/auth.js'

const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 收藏单词数据
const favoriteWords = ref([])
// 加载状态
const loading = ref(true)
// 错误信息
const error = ref('')
// 分页相关
const currentPage = ref(1)
const pageSize = ref(10) // 每次查询10个数据
const hasMore = ref(true)
const loadingMore = ref(false)

// 获取收藏单词数据
const fetchFavoriteWords = async (isLoadMore = false) => {
  if (isLoadMore && !hasMore.value) return
  
  try {
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
      currentPage.value = 1
      favoriteWords.value = []
      hasMore.value = true
    }
    
    error.value = ''
    // 从登录信息获取用户ID
    const userId = getCurrentUserId()
    const pageNum = isLoadMore ? currentPage.value + 1 : 1
    const response = await getUserFavoriteWords(userId, pageNum)
    
    if (response.code && response.data) {
      // 确保数据是数组格式
      const newWords = Array.isArray(response.data) ? response.data : []
      
      if (isLoadMore) {
        // 加载更多，追加数据
        favoriteWords.value = [...favoriteWords.value, ...newWords]
        currentPage.value = pageNum
        // 如果返回的数据少于pageSize，说明没有更多数据了
        hasMore.value = newWords.length >= pageSize.value
      } else {
        // 首次加载，替换数据
        favoriteWords.value = newWords
        // 如果返回的数据少于pageSize，说明没有更多数据了
        hasMore.value = newWords.length >= pageSize.value
      }
    } else {
      if (!isLoadMore) {
        favoriteWords.value = []
        hasMore.value = false
      } else {
        hasMore.value = false
      }
    }
  } catch (err) {
    console.error('获取收藏单词失败:', err)
    error.value = '获取收藏单词失败，请稍后重试'
    
    // 如果是首次加载失败，使用模拟数据作为备选
    if (!isLoadMore) {
      favoriteWords.value = [
        {
          id: 1,
          word: 'abandon',
          phonetic: '/əˈbændən/',
          definition: '放弃，抛弃'
        },
        {
          id: 2,
          word: 'abnormal',
          phonetic: '/æbˈnɔːrml/',
          definition: '不正常的，反常的'
        },
        {
          id: 3,
          word: 'abundant',
          phonetic: '/əˈbʌndənt/',
          definition: '丰富的，充裕的'
        },
        {
          id: 4,
          word: 'academic',
          phonetic: '/ˌækəˈdemɪk/',
          definition: '学术的，教学的'
        },
        {
          id: 5,
          word: 'accelerate',
          phonetic: '/əkˈseləreɪt/',
          definition: '加速，促进'
        }
      ]
      hasMore.value = false
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 监听滚动事件，实现无限加载
const handleScroll = () => {
  const scrollContainer = document.querySelector('.words-scroll-container')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  // 当滚动到距离底部50px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50 && !loadingMore.value && hasMore.value) {
    fetchFavoriteWords(true)
  }
}

// 点击单词跳转到单词详情页
const goToWordDetail = (word) => {
  router.push({
    path: '/showWord',
    query: { wordId: word.id }
  })
}

// 组件挂载时获取数据
onMounted(() => {
  initTheme()
  fetchFavoriteWords()
  // 添加滚动事件监听
  const scrollContainer = document.querySelector('.words-scroll-container')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
})

// 组件卸载时移除滚动事件监听
onUnmounted(() => {
  const scrollContainer = document.querySelector('.words-scroll-container')
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="favorite-words-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24" class="back-icon"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">收藏单词本</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">收藏单词本</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="words-scroll-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchFavoriteWords" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 单词列表 -->
      <div v-else class="words-list">
        <div
          v-for="word in favoriteWords"
          :key="word.id"
          class="word-card"
          @click="goToWordDetail(word)"
        >
          <div class="word-info">
            <div class="word-title">{{ word.word }}</div>
            <div class="word-phonetic">{{ word.phonetic }}</div>
            <div class="word-definition">{{ word.definition }}</div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="favoriteWords.length === 0" class="empty-state">
          <p>暂无收藏单词</p>
        </div>
        
        <!-- 加载更多指示器 -->
        <div v-if="loadingMore" class="loading-more">
          <el-loading type="spinner" size="small" color="#409EFF" />
          <span>加载更多...</span>
        </div>
        
        <!-- 没有更多数据提示 -->
        <div v-if="!hasMore && favoriteWords.length > 0" class="no-more">
          <p>没有更多数据了</p>
        </div>
      </div>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 主容器 */
.favorite-words-container {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  background-color: var(--bg-primary);
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
  padding: env(safe-area-inset-top) 1rem 1rem;
  /* 移除阴影，保持视觉一致性 */
  box-shadow: none;
  transition: all 0.3s ease;
}

/* 头部导航 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 0.5rem 0;
}

.page-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  transition: all 0.3s ease;
}

.back-button {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  padding: 0;
  font-size: 1rem;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  justify-content: center;
  transition: all 0.3s ease;
}

.header-placeholder {
  width: 40px;
}

/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.words-scroll-container {
  /* 核心：设置明确的高度和位置，确保可以滚动 */
  position: fixed;
  top: 70px; /* 进一步减小与头部的距离 */
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有收藏 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 添加左右内边距 */
  padding-left: 1rem;
  padding-right: 1rem;
  /* 添加顶部内边距 */
  padding-top: 0.3rem;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) var(--bg-primary);
}

/* 单词列表 */
.words-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

/* 单词卡片 */
.word-card {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.word-card:active {
  transform: scale(0.98);
}

/* 单词信息 */
.word-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.word-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--primary-color);
  transition: all 0.3s ease;
}

.word-phonetic {
  font-size: 1.1rem;
  color: var(--text-tertiary);
  font-style: italic;
  transition: all 0.3s ease;
}

.word-definition {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-height: 1.4rem;
  transition: all 0.3s ease;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: var(--text-tertiary);
  width: 100%;
  transition: all 0.3s ease;
}

/* 加载更多 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

/* 没有更多数据 */
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  color: var(--text-tertiary);
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  width: 100%;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  color: var(--danger-color);
  width: 100%;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .words-scroll-container {
    padding: 0.8rem;
  }
  
  .word-card {
    padding: 1.2rem;
  }
  
  .word-title {
    font-size: 1.3rem;
  }
  
  .word-phonetic,
  .word-definition {
    font-size: 1rem;
  }
  
  .word-definition {
    line-height: 1.3;
    max-height: 1.3rem;
  }
}
</style>