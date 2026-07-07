<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getListening, getListeningByType, getUserFavoriteListening } from '@/api/listening.js'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例和当前路由
const router = useRouter()
const route = useRoute()

// 是否只展示收藏的听力
const showFavorites = ref(false)

// 返回上一页
const goBack = () => {
  router.back()
}

// 听力数据
const listeningList = ref([])
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 当前页码
const pageNum = ref(1)
// 是否还有更多数据
const hasMore = ref(true)
// 当前选中的分类
const selectedType = ref('all') // 'all' 表示全部

// 分类选项
const categoryOptions = [
  { value: 'all', label: '全部' },
  { value: '1', label: '高中听力' },
  { value: '2', label: '四级听力' },
  { value: '3', label: '六级听力' },
  { value: '4', label: '新闻报道' },
  { value: '5', label: '英文歌曲' },
  { value: '6', label: '日常对话' }
]

// 搜索查询
const searchQuery = ref('')

// 过滤后的听力列表
const filteredListening = computed(() => {
  if (!searchQuery.value) {
    return listeningList.value
  }
  const query = searchQuery.value.toLowerCase()
  return listeningList.value.filter(item => 
    item.title.toLowerCase().includes(query)
  )
})

// 获取听力数据
const fetchListening = async (isLoadMore = false) => {
  // 记录当前滚动位置
  let scrollTop = 0
  let scrollHeight = 0
  if (isLoadMore) {
    const scrollContainer = document.querySelector('.listening-scroll-container')
    if (scrollContainer) {
      scrollTop = scrollContainer.scrollTop
      scrollHeight = scrollContainer.scrollHeight
    }
  }
  
  try {
    loading.value = true
    error.value = ''
    let response
    
    if (showFavorites.value) {
      // 只获取收藏的听力
      const userId = getCurrentUserId()
      response = await getUserFavoriteListening(userId, pageNum.value)
      
      // 根据分类筛选收藏的听力
      if (selectedType.value !== 'all' && response && response.code === 1 && Array.isArray(response.data)) {
        const filteredData = response.data.filter(item => item.category === parseInt(selectedType.value))
        response.data = filteredData
        hasMore.value = false // 收藏的听力一次性筛选完毕
      }
    } else {
      // 根据是否选择了分类调用不同的API
      if (selectedType.value === 'all') {
        // 获取全部听力信息
        response = await getListening(pageNum.value)
      } else {
        // 根据类型获取听力信息
        response = await getListeningByType(selectedType.value, pageNum.value)
      }
    }
    
    // 检查API返回的数据格式
    if (response && response.code === 1 && Array.isArray(response.data)) {
      const newListening = response.data
      
      // 如果是加载更多，则追加数据，否则替换数据
      if (isLoadMore) {
        listeningList.value = [...listeningList.value, ...newListening]
      } else {
        listeningList.value = newListening
      }
      
      // 检查是否还有更多数据
      hasMore.value = newListening.length >= 10
      
      // 无论是否是加载更多，只要有数据就增加页码
      if (newListening.length > 0) {
        pageNum.value++
      }
    } else {
      if (isLoadMore) {
        hasMore.value = false
      } else {
        error.value = showFavorites.value ? '获取收藏的听力失败' : '获取听力数据失败'
      }
    }
  } catch (err) {
    console.error('获取听力数据失败:', err)
    if (isLoadMore) {
      hasMore.value = false
    } else {
      error.value = showFavorites.value ? '获取收藏的听力失败，请稍后重试' : '获取听力数据失败，请稍后重试'
    }
  } finally {
    loading.value = false
    
    // 恢复滚动位置
    if (isLoadMore) {
      setTimeout(() => {
        const scrollContainer = document.querySelector('.listening-scroll-container')
        if (scrollContainer) {
          scrollContainer.scrollTop = scrollTop
        }
      }, 0)
    }
  }
}

// 监听分类变化，重新获取数据
watch(selectedType, () => {
  pageNum.value = 1
  hasMore.value = true
  fetchListening()
})

// 监听showFavorites变化，重新获取数据
watch(showFavorites, () => {
  pageNum.value = 1
  hasMore.value = true
  fetchListening()
})

// 加载更多数据
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  await fetchListening(true)
}

// 监听滚动事件
const handleScroll = () => {
  const scrollContainer = document.querySelector('.listening-scroll-container')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  
  // 当滚动到底部100px以内时，加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value && !loading.value) {
    loadMore()
  }
}

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时获取数据
onMounted(() => {
  // 检查是否从收藏页面进入，只展示收藏的听力
  showFavorites.value = route.query.showFavorites === 'true'
  fetchListening()
  
  // 添加滚动监听
  const scrollContainer = document.querySelector('.listening-scroll-container')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
  
  // 初始化主题
  initTheme()
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  const scrollContainer = document.querySelector('.listening-scroll-container')
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})

// 跳转到听力详情页
const goToDetail = (id) => {
  router.push(`/listening/${id}`)
}
</script>

<template>
  <div class="listening-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">{{ showFavorites ? '我的听力收藏' : '听力训练' }}</h1>
        <div class="header-placeholder"></div>
      </div>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索听力内容..."
          clearable
          class="search-input"
        />
      </div>
      
      <!-- 分类筛选 -->
      <div class="category-filter">
        <div class="category-scroll">
          <div class="category-options">
            <el-button
              v-for="option in categoryOptions"
              :key="option.value"
              :type="selectedType === option.value ? 'primary' : 'default'"
              size="small"
              @click="selectedType = option.value"
              class="category-button"
              :class="{ 'active': selectedType === option.value }"
            >
              {{ option.label }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="listening-scroll-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#FF9800" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchListening" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 听力列表 -->
      <div v-else class="listening-list">
        <div
          v-for="item in filteredListening"
          :key="item.id"
          class="listening-card"
          @click="goToDetail(item.id)"
        >
          <div class="listening-info">
            <div class="listening-title">{{ item.title }}</div>
            <div class="listening-duration">{{ Math.floor(item.duration / 60) }}:{{ (item.duration % 60).toString().padStart(2, '0') }}</div>
          </div>
          <el-button type="primary" round class="play-button">播放</el-button>
        </div>
        
        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <el-loading v-if="loading" type="spinner" color="#FF9800" />
          <p v-else-if="!loading && hasMore">下拉加载更多</p>
        </div>
        
        <!-- 没有更多数据 -->
        <div v-else-if="!hasMore && listeningList && listeningList.length > 0" class="no-more">
          <p>没有更多了</p>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && !error && (!filteredListening || filteredListening.length === 0)" class="empty-state">
        <el-icon size="48">
          <Document />
        </el-icon>
        <p>暂无听力内容</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.listening-container {
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
  padding: env(safe-area-inset-top) 1rem 0;
  /* 移除阴影，保持视觉一致性 */
  box-shadow: none;
  height: auto;
  min-height: 170px;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

/* 头部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin: 0 0 1rem;
  box-sizing: border-box;
  padding: 0.5rem 0;
}

.page-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
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
}

.header-placeholder {
  width: 40px;
}

/* 搜索栏样式 */
.search-bar {
  margin: 0 0 1rem;
  background-color: transparent;
  z-index: 10;
  width: 100%;
  box-sizing: border-box;
}

.search-input {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  height: 40px;
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  transition: all 0.3s ease;
}

/* 分类筛选 */
.category-filter {
  margin: 0 0 1rem;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

/* 自定义滚动容器 */
.category-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  -webkit-overflow-scrolling: touch; /* 流畅滚动 */
}

/* 隐藏滚动条 */
.category-scroll::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

.category-options {
  display: flex;
  gap: 0.5rem;
  padding: 0 0.5rem;
  min-width: min-content;
}

.category-button {
  white-space: nowrap;
  transition: all 0.3s ease;
}

/* 确保激活状态的按钮立即显示为深蓝色 */
.category-button.active {
  --el-button-bg-color: var(--el-color-primary) !important;
  --el-button-border-color: var(--el-color-primary) !important;
  --el-button-text-color: #ffffff !important;
}

/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.listening-scroll-container {
  /* 核心：设置明确的高度和位置，确保可以滚动 */
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有听力内容 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 增加顶部内边距，确保内容不被固定头部遮挡 */
  padding-top: 180px;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) var(--bg-primary);
  /* 确保滚动容器占满可用空间 */
  width: 100%;
  /* 添加左右内边距，确保卡片在手机屏幕上居中显示 */
  padding-left: 1rem;
  padding-right: 1rem;
  box-sizing: border-box;
}

/* 调整小屏幕上的内边距 */
@media (max-width: 768px) {
  .listening-scroll-container {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
}

/* 确保滚动条在所有浏览器中都能正常工作 */
.listening-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 transparent;
}

/* 滚动条样式 - 确保内部滚动条可见 */
.listening-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.listening-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.listening-scroll-container::-webkit-scrollbar-thumb {
  background-color: var(--border-color);
  border-radius: 3px;
}

/* 听力列表样式 */
.listening-list {
  width: 100%;
  box-sizing: border-box;
  /* 确保列表内容能正常扩展，但不会被拉伸 */
  overflow: visible;
  /* 添加额外的底部空间 */
  margin-bottom: 2rem;
  /* 确保布局在任何屏幕尺寸下都能正常工作 */
  min-width: 0;
  /* 移除左右内边距，使用滚动容器的内边距 */
  padding: 0;
}

/* 听力卡片样式 */
.listening-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

.listening-card:active {
  transform: scale(0.98);
}

/* 听力信息区域 */
.listening-info {
  flex: 1;
  min-width: 0;
}

/* 听力标题样式 */
.listening-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 听力时长样式 */
.listening-duration {
  font-size: 0.85rem;
  color: var(--text-tertiary);
}

/* 播放按钮样式 */
.play-button {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}



/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  width: 100%;
}

.error-container .el-button {
  margin-top: 0.5rem;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: var(--text-tertiary);
  width: 100%;
}

.empty-state .el-icon {
  margin-bottom: 1rem;
  color: var(--primary-color);
}

.empty-state p {
  margin-bottom: 1.5rem;
}

/* 加载更多状态 */
.load-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
  width: 100%;
}

.load-more p {
  margin: 0.5rem 0 0;
  color: var(--text-tertiary);
  font-size: 0.9rem;
}

/* 没有更多数据 */
.no-more {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
  color: var(--text-tertiary);
  font-size: 0.8rem;
  width: 100%;
}

/* 响应式设计 */

/* 小屏手机 */
@media (max-width: 480px) {
  .listening-container {
    padding: 4px;
  }
  
  .top-sticky-section {
    min-height: 190px; /* 增加最小高度，确保内容不会溢出 */
  }
  
  .header {
    margin-bottom: 8px;
  }
  
  .page-title {
    font-size: 20px; /* 增大页面标题 */
  }
  
  .search-bar {
    padding: 0 0 8px;
  }
  
  .search-input {
    height: 40px; /* 增大搜索框高度 */
    font-size: 16px; /* 增大搜索框字体 */
  }
  
  .listening-scroll-container {
    padding-left: 0.8rem;
    padding-right: 0.8rem;
    padding-top: 190px; /* 增加顶部内边距，确保内容不被固定头部遮挡 */
  }
  
  .listening-card {
    padding: 0.8rem 1.2rem;
  }
  
  .listening-title {
    font-size: 0.95rem; /* 增大听力标题字体 */
  }
  
  .play-button {
    padding: 0.4rem 0.8rem;
    font-size: 0.85rem;
  }
}

/* 超小屏手机 */
@media (max-width: 320px) {
  .listening-container {
    padding: 8px;
  }
  
  .listening-card {
    padding: 0.7rem 1rem;
  }
  
  .listening-title {
    font-size: 0.9rem;
  }
  
  .play-button {
    padding: 0.3rem 0.6rem;
    font-size: 0.8rem;
  }
}

/* 平板设备 */
@media (min-width: 768px) and (max-width: 1024px) {
  .listening-container {
    padding: 2rem;
  }
  
  .listening-scroll-container {
    max-width: 700px;
  }
  
  .listening-card {
    padding: 1.2rem;
  }
  
  .listening-title {
    font-size: 1.1rem;
  }
  
  .play-button {
    padding: 0.6rem 1.2rem;
    font-size: 1rem;
  }
}

/* 深色模式 */
.dark-mode .listening-container {
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

.dark-mode .top-sticky-section {
  background-color: var(--bg-primary);
}

.dark-mode .header {
  background-color: var(--bg-primary);
}

.dark-mode .back-button {
  color: var(--text-primary);
}

.dark-mode .back-button:hover {
  background-color: transparent;
}

.dark-mode .header-placeholder {
  background-color: var(--bg-primary);
}

.dark-mode .page-title {
  color: var(--text-primary);
}

.dark-mode .search-bar {
  background-color: var(--bg-primary);
}

.dark-mode .search-input {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

.dark-mode .search-input :deep(.el-input__wrapper) {
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  box-shadow: none !important;
}

.dark-mode .search-input :deep(.el-input__placeholder) {
  color: var(--text-tertiary) !important;
}

.dark-mode .category-filter {
  background-color: var(--bg-primary);
}

.dark-mode .category-button {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.dark-mode .category-button:hover {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.dark-mode .category-button.el-button--primary {
  background-color: #FF9800;
  border-color: #FF9800;
  color: white;
}

.dark-mode .listening-scroll-container {
  background-color: var(--bg-primary);
}

.dark-mode .listening-card {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
}

.dark-mode .listening-title {
  color: var(--text-primary);
}

.dark-mode .listening-duration {
  color: var(--text-tertiary);
}

.dark-mode .play-button {
  background-color: #FF9800;
  border-color: #FF9800;
}

.dark-mode .play-button:hover {
  background-color: #FF9800;
  border-color: #FF9800;
}

.dark-mode .empty-state {
  color: var(--text-tertiary);
}
</style>