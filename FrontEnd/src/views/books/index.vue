<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserFavoriteBooks, getBook } from '@/api/book.js'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例和当前路由
const router = useRouter()
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.back()
}

// 图书数据
const books = ref([])
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 当前页码
const pageNum = ref(1)
// 是否还有更多数据
const hasMore = ref(true)

// 初始化主题
const { initTheme } = useTheme()

// 组件挂载时获取数据
onMounted(() => {
  fetchBooks()
  
  // 添加滚动监听
  const scrollContainer = document.querySelector('.books-scroll-container')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
  
  // 初始化主题
  initTheme()
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  const scrollContainer = document.querySelector('.books-scroll-container')
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})
// 获取来源（从url参数中获取）
const from = computed(() => route.query.from || 'shop')
// 页面标题
const pageTitle = computed(() => from.value === 'user' ? '我的图书' : '学习资料')

// 搜索查询
const searchQuery = ref('')

// 过滤后的图书列表
const filteredBooks = computed(() => {
  if (!searchQuery.value) {
    return books.value
  }
  const query = searchQuery.value.toLowerCase()
  return books.value.filter(book => 
    book.name.toLowerCase().includes(query) ||
    book.description.toLowerCase().includes(query)
  )
})

// 获取书籍数据
const fetchBooks = async (isLoadMore = false) => {
  // 记录当前滚动位置
  let scrollTop = 0
  let scrollHeight = 0
  if (isLoadMore) {
    const scrollContainer = document.querySelector('.books-scroll-container')
    if (scrollContainer) {
      scrollTop = scrollContainer.scrollTop
      scrollHeight = scrollContainer.scrollHeight
    }
  }
  
  try {
    loading.value = true
    error.value = ''
    let response
    
    // 根据来源调用不同的API
    if (from.value === 'user') {
      // 从我的页面进入，获取用户收藏的书籍
      response = await getUserFavoriteBooks(1) // 假设用户ID为1
      hasMore.value = false // 收藏的书籍一次性获取完毕
    } else {
      // 从商店页面进入，获取书籍列表
      response = await getBook(pageNum.value)
    }
    
    // 检查API返回的数据格式
    if (response && response.code === 1 && Array.isArray(response.data)) {
      // 转换API返回的数据格式
      const newBooks = response.data.map(book => ({
        ...book,
        // 使用API返回的cover字段作为图片
        image: book.cover || `https://picsum.photos/seed/book${book.id}/200/300`
      }))
      
      // 如果是加载更多，则追加数据，否则替换数据
      if (isLoadMore) {
        books.value = [...books.value, ...newBooks]
      } else {
        books.value = newBooks
      }
      
      // 检查是否还有更多数据
      // 假设每页返回10条数据，如果返回的数据少于10条，则认为没有更多数据
      hasMore.value = newBooks.length >= 10
      
      // 无论是否是加载更多，只要有数据就增加页码
      if (newBooks.length > 0) {
        pageNum.value++
      }
    } else {
      if (isLoadMore) {
        hasMore.value = false
      } else {
        error.value = '获取书籍数据失败'
      }
    }
  } catch (err) {
    console.error('获取书籍数据失败:', err)
    if (isLoadMore) {
      hasMore.value = false
    } else {
      error.value = '获取书籍数据失败，请稍后重试'
    }
  } finally {
    loading.value = false
    
    // 恢复滚动位置
    if (isLoadMore) {
      setTimeout(() => {
        const scrollContainer = document.querySelector('.books-scroll-container')
        if (scrollContainer) {
          // 直接恢复原来的滚动位置
          scrollContainer.scrollTop = scrollTop
        }
      }, 0)
    }
  }
}

// 监听来源变化，重新获取数据
watch(from, () => {
  pageNum.value = 1
  hasMore.value = true
  fetchBooks()
})

// 加载更多数据
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  await fetchBooks(true)
}

// 监听滚动事件
const handleScroll = () => {
  const scrollContainer = document.querySelector('.books-scroll-container')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  
  // 当滚动到底部100px以内时，加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value && !loading.value) {
    loadMore()
  }
}
</script>

<template>
  <div class="books-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="header-placeholder"></div>
      </div>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索图书..."
          clearable
          class="search-input"
        />
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="books-scroll-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#FF9800" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchBooks" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 图书列表 -->
      <div v-else class="books-list">
        <router-link
          v-for="book in filteredBooks"
          :key="book.id"
          :to="`/book/${book.id}`"
          class="book-card-link"
        >
          <div class="book-card">
            <div class="book-image">
              <el-image
                :src="book.image"
                :alt="book.name"
                fit="cover"
                :fallback="'/default-book.png'"
              />
            </div>
            <div class="book-info">
              <div class="book-name">{{ book.name }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-description">{{ book.description }}</div>
              <div class="book-price">${{ book.price }}</div>
            </div>
          </div>
        </router-link>
        
        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <el-loading v-if="loading" type="spinner" color="#409EFF" />
          <p v-else-if="!loading && hasMore">下拉加载更多</p>
        </div>
        
        <!-- 没有更多数据 -->
        <div v-else-if="!hasMore && books.value && books.value.length > 0" class="no-more">
          <p>没有更多了</p>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && !error && (!filteredBooks || filteredBooks.length === 0)" class="empty-state">
        <el-icon size="48">
          <Document />
        </el-icon>
        <p>{{ from.value === 'user' ? '暂无收藏图书' : '暂无图书' }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.books-container {
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
  min-height: 120px;
  transition: all 0.3s ease;
}

/* 头部导航 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto 1rem;
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
  margin: 0 auto;
  background-color: transparent;
  z-index: 10;
  width: 100%;
  max-width: 600px;
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

/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.books-scroll-container {
  /* 核心：设置明确的高度和位置，确保可以滚动 */
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有图书 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 添加左右内边距 */
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  /* 增加顶部内边距，确保内容不被固定头部遮挡 */
  padding-top: 130px;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) var(--bg-primary);
  /* 确保滚动容器占满可用空间 */
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

/* 确保滚动条在所有浏览器中都能正常工作 */
.books-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 transparent;
}

/* 滚动条样式 - 确保内部滚动条可见 */
.books-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.books-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.books-scroll-container::-webkit-scrollbar-thumb {
  background-color: var(--border-color);
  border-radius: 3px;
}

/* 图书列表样式 */
.books-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  box-sizing: border-box;
  padding: 0 4px;
  /* 确保列表内容能正常扩展，但不会被拉伸 */
  overflow: visible;
  /* 添加额外的底部空间 */
  margin-bottom: 2rem;
  /* 确保布局在任何屏幕尺寸下都能正常工作 */
  min-width: 0;
}

/* 图书卡片样式 */
.book-card {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background-color: var(--bg-secondary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  position: relative;
  /* 设置最小高度，确保内容不会被截断 */
  height: auto;
  overflow: hidden;
}

.book-card:active {
  transform: scale(0.98);
}

/* 图书图片容器 */
.book-image {
  width: 100%;
  height: 0;
  padding-bottom: 100%; /* 使用padding-bottom实现精确的正方形比例 */
  position: relative;
  background-color: var(--bg-tertiary);
  overflow: hidden;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
}

.book-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

/* 图书信息区域 */
.book-info {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

/* 图书名称样式 */
.book-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 42px;
}

/* 图书作者样式 */
.book-author {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 图书描述样式 - 只显示1行 */
.book-description {
  font-size: 14px;
  color: var(--text-tertiary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* 图书价格样式 */
.book-price {
  font-size: 14px;
  font-weight: 600;
  color: var(--danger-color);
  margin-top: auto;
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

/* 图书卡片链接样式 */
.book-card-link {
  display: block;
  width: calc(50% - 4px); /* 两列布局，减去间隙 */
  text-decoration: none;
  color: inherit;
  box-sizing: border-box;
}

/* 响应式设计 */

/* 小屏手机 */
@media (max-width: 480px) {
  .books-container {
    padding: 4px;
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
  
  .books-scroll-container {
    height: calc(100vh - 130px);
    padding-left: 2px;
    padding-right: 2px;
  }
  
  .books-list {
    gap: 6px;
    padding: 0 2px;
    /* 确保在小屏幕上flex布局仍然正常工作 */
    display: flex !important;
    flex-wrap: wrap !important;
    width: 100% !important;
  }
  
  /* 确保图书卡片链接在小屏幕上能正确适应flex布局 */
  .book-card-link {
    width: calc(50% - 3px) !important; /* 两列布局，减去间隙 */
    box-sizing: border-box !important;
  }
  
  /* 确保图书卡片在小屏幕上能正确显示 */
  .book-card {
    width: 100% !important;
    min-width: 0 !important;
    box-sizing: border-box !important;
  }
  
  .book-info {
    padding: 6px;
  }
  
  .book-name {
    font-size: 14px; /* 增大图书名称字体 */
    min-height: 38px;
  }
  
  .book-description {
    font-size: 12px; /* 增大图书描述字体 */
  }
}

/* 超小屏手机 */
@media (max-width: 320px) {
  .books-container {
    padding: 8px;
  }
  
  .books-list {
    gap: 8px;
    padding: 0 4px;
  }
  
  .book-info {
    padding: 6px;
  }
  
  .book-name {
    font-size: 12px;
  }
  
  .book-description {
    font-size: 10px;
  }
}

/* 平板设备 */
@media (min-width: 768px) and (max-width: 1024px) {
  .books-container {
    padding: 2rem;
  }
  
  .books-scroll-container {
    max-width: 700px;
    height: calc(100vh - 160px);
  }
  
  .books-list {
    gap: 12px;
    padding: 0 8px;
  }
  
  /* 平板上显示3列 */
  .book-card-link {
    width: calc(33.333% - 8px); /* 三列布局，减去间隙 */
  }
  
  .book-info {
    padding: 12px;
  }
  
  .book-name {
    font-size: 16px;
  }
  
  .book-description {
    font-size: 14px;
  }
}
</style>