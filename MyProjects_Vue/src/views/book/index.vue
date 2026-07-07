<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, StarFilled, Star } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getBookDetail, checkBookIsFavorite, favoriteBook, removeBookFromFavorites } from '@/api/book'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例和当前路由
const router = useRouter()
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.back()
}

// 图书ID
const bookId = computed(() => route.params.id)
// 图书数据
const book = ref(null)
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')
// 是否收藏
const isFavorite = ref(false)
// 收藏状态加载中
const checkingFavorite = ref(false)

// 检查图书是否被收藏
const checkBookFavoriteStatus = async (bookId) => {
  checkingFavorite.value = true
  // 实际开发中，userId应从登录信息获取，现在暂时使用固定值1
  const userId = getCurrentUserId()
  
  try {
    const result = await checkBookIsFavorite(userId, bookId)
    if (result.code) {
      isFavorite.value = Boolean(result.data)
    }
  } catch (error) {
    console.error('检查图书收藏状态失败:', error)
  } finally {
    checkingFavorite.value = false
  }
}

// 获取图书详情 - 使用真实API
const getBookById = async (id) => {
  try {
    loading.value = true
    error.value = ''
    
    // 调用真实API获取图书详情
    const result = await getBookDetail(id)
    
    if (result.code && result.data) {
      // 处理API返回的数据，确保字段完整
      const bookData = {
        ...result.data,
        // 确保price是字符串格式
        price: String(result.data.price || '0.00'),
        // 添加默认的出版信息（如果API没有返回）
        publisher: result.data.publisher || '未知出版社',
        publishDate: result.data.publishDate || '未知日期'
      }
      book.value = bookData
      
      // 检查图书是否被收藏
      await checkBookFavoriteStatus(id)
    } else {
      error.value = '未找到该图书'
    }
  } catch (err) {
    console.error('获取图书详情失败:', err)
    error.value = '获取图书详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 收藏/取消收藏
const toggleFavorite = async () => {
  // 实际开发中，userId应从登录信息获取，现在暂时使用固定值1
  const userId = getCurrentUserId()
  
  try {
    if (isFavorite.value) {
      // 取消收藏
      const result = await removeBookFromFavorites(userId, bookId.value)
      if (result.code) {
        isFavorite.value = false
      }
    } else {
      // 添加收藏
      const result = await favoriteBook(userId, bookId.value)
      if (result.code) {
        isFavorite.value = true
      }
    }
  } catch (error) {
    console.error('更新收藏状态失败:', error)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  initTheme()
  if (bookId.value) {
    getBookById(bookId.value)
  }
})
</script>

<template>
  <div class="book-detail-container">
    <!-- 头部导航 -->
    <div class="header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon size="24" class="back-icon"><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">图书详情</h1>
      <div class="header-placeholder"></div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="book-scroll-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#FF9800" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchBookDetail" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 图书详情 -->
      <div v-else-if="book" class="book-detail">
        <!-- 图书封面 -->
        <div class="book-cover">
          <el-image
            :src="book.cover || `https://picsum.photos/seed/book${book.id}/400/600`"
            :alt="book.name"
            fit="cover"
            :fallback="'/default-book.png'"
          />
        </div>
        
        <!-- 图书基本信息 -->
        <div class="book-basic-info">
          <h2 class="book-title">{{ book.name }}</h2>
          <div class="book-author">作者：{{ book.author }}</div>
          <div class="book-category">分类：{{ book.category === 1 ? '小说' : book.category === 2 ? '散文' : book.category === 3 ? '学术文章' : book.category === 4 ? '诗文' : book.category || '未知分类' }}</div>
          <div class="book-price">${{ book.price }}</div>
          
          <!-- 操作按钮 -->
          <div class="book-actions">
            <el-button type="primary" size="large" class="buy-button">
              立即购买
            </el-button>
            <el-button 
              type="default" 
              size="large" 
              :icon="isFavorite ? StarFilled : Star" 
              @click="toggleFavorite"
              class="favorite-button"
              :class="{ 'is-favorite': isFavorite }"
              :loading="checkingFavorite"
              :disabled="checkingFavorite"
              icon-position="left"
            >
              {{ isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
        
        <!-- 图书详细介绍 -->
        <div class="book-description">
          <h3 class="section-title">内容简介</h3>
          <p class="description-text">{{ book.description }}</p>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <p>暂无图书信息</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.book-detail-container {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-primary);
  /* 适配刘海屏 */
  padding-top: env(safe-area-inset-top);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

/* 头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 10;
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

.back-icon {
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.header-placeholder {
  width: 40px;
}

.page-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  transition: all 0.3s ease;
}

/* 内部滚动容器 */
.book-scroll-container {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 2rem;
}

/* 图书详情 */
.book-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 1.5rem;
}

/* 图书封面 */
.book-cover {
  width: 100%;
  max-width: 400px;
  margin: 0 auto 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: auto;
}

/* 图书基本信息 */
.book-basic-info {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.book-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.book-author {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.book-price {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--danger-color);
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.book-category {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.book-meta > div {
  font-size: 0.9rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

/* 操作按钮 */
.book-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.buy-button, .favorite-button {
  flex: 1;
  width: 100%;
  max-width: 200px;
  height: 48px;
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  padding-left: 1rem;
  padding-right: 1rem;
  margin: 0;
  border-radius: 4px;
  border: 1px solid transparent;
}

/* 强制重置Element Plus按钮的默认内边距差异 */
:deep(.el-button) {
  padding-left: 1rem !important;
  padding-right: 1rem !important;
}

/* 确保收藏按钮的内容居中对齐 */
.favorite-button {
  text-align: center;
}

/* 确保图标不会影响按钮的整体宽度 */
.favorite-button :deep(.el-icon) {
  width: 1rem;
  height: 1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 确保收藏按钮的图标和文字对齐 */
.favorite-button :deep(.el-button__icon) {
  margin-right: 0.3rem;
  margin-left: 0;
}

.favorite-button {
  color: #606266; /* 默认文字颜色 */
  border-color: #dcdfe6; /* 默认边框颜色 */
}

.favorite-button:hover {
  background-color: #606266;
  border-color: #dcdfe6;
  color: #606266;
}

/* 收藏状态样式 */
.favorite-button.is-favorite {
  background-color: rgba(230, 162, 60, 0.1); /* 微黄色背景 */
  color: var(--warning-color); /* 黄色文字 */
  border-color: var(--warning-color); /* 黄色边框 */
  transition: all 0.3s ease;
}

.favorite-button.is-favorite:hover {
  background-color: rgba(230, 162, 60, 0.1);
}

/* 图书详细介绍 */
.book-description {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.section-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.description-text {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.8;
  text-align: justify;
  transition: all 0.3s ease;
}

/* 加载和错误状态 */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  gap: 1rem;
  width: 100%;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .book-detail {
    padding: 1rem;
  }
  
  .book-basic-info {
    padding: 1rem;
  }
  
  .book-title {
    font-size: 1.3rem;
  }
  
  .book-price {
    font-size: 1.5rem;
  }
  
  .book-meta {
    grid-template-columns: 1fr;
  }
  
  .book-actions {
    flex-direction: column;
  }
  
  .buy-button, .favorite-button {
    max-width: none;
    width: 100%;
    flex: 1;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: 1rem;
    padding-right: 1rem;
    box-sizing: border-box;
  }
  
  /* 确保小屏幕上收藏按钮的图标对齐 */
  .favorite-button :deep(.el-button__icon) {
    margin-right: 0.3rem;
    margin-left: 0;
  }
}
</style>