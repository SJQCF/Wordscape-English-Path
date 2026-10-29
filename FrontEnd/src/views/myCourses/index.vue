<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Document, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getBoughtCourse } from '@/api/course'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

onMounted(() => {
  initTheme()
  fetchBoughtCourses()
  
  // 添加滚动事件监听
  const scrollContainer = document.querySelector('.course-scroll-container')
  if (scrollContainer) {
    scrollContainer.addEventListener('scroll', handleScroll)
  }
})

// 获取路由实例
const router = useRouter()

// 搜索查询
const searchQuery = ref('')
// 课程数据
const courses = ref([])
// 加载状态
const loading = ref(true)
// 错误信息
const error = ref('')
// 当前页码
const currentPage = ref(1)
// 是否还有更多数据
const hasMore = ref(true)

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取已购买课程
const fetchBoughtCourses = async (isLoadMore = false) => {
  // 记录当前滚动位置
  let scrollTop = 0
  if (isLoadMore) {
    const scrollContainer = document.querySelector('.course-scroll-container')
    if (scrollContainer) {
      scrollTop = scrollContainer.scrollTop
    }
  }
  
  loading.value = true
  error.value = ''
  try {
    // 假设用户ID为1
    const userId = getCurrentUserId()
    
    // 获取已购买课程
    const response = await getBoughtCourse(userId, currentPage.value)
    if (response.code && response.data) {
      const newCourses = response.data
      if (isLoadMore) {
        courses.value = [...courses.value, ...newCourses]
      } else {
        courses.value = newCourses
      }
      
      // 检查是否还有更多数据
      hasMore.value = newCourses.length >= 10
      if (hasMore.value) {
        currentPage.value++
      }
    } else {
      if (isLoadMore) {
        hasMore.value = false
      } else {
        courses.value = []
      }
    }
  } catch (err) {
    console.error('获取已购买课程失败:', err)
    error.value = '获取已购买课程失败，请稍后重试'
    if (isLoadMore) {
      hasMore.value = false
    }
  } finally {
    loading.value = false
    
    // 恢复滚动位置
    if (isLoadMore) {
      setTimeout(() => {
        const scrollContainer = document.querySelector('.course-scroll-container')
        if (scrollContainer) {
          scrollContainer.scrollTop = scrollTop
        }
      }, 0)
    }
  }
}

// 查看课程详情
const viewCourseDetail = (courseId) => {
  router.push(`/coursePlayer/${courseId}`)
}

// 处理滚动事件
const handleScroll = () => {
  const scrollContainer = document.querySelector('.course-scroll-container')
  if (!scrollContainer) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  
  // 当滚动到距离底部100px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value && !loading.value) {
    loadMore()
  }
}

// 加载更多数据
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  await fetchBoughtCourses(true)
}

// 格式化时长函数
const formatDuration = (minutes) => {
  if (!minutes) return '0分钟'
  
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours > 0) {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  } else {
    return `${mins}分钟`
  }
}

// 过滤后的课程列表
const filteredCourses = computed(() => {
  if (!searchQuery.value) {
    return courses.value
  }
  const query = searchQuery.value.toLowerCase()
  return courses.value.filter(course => 
    course.name.toLowerCase().includes(query)
  )
})



// 组件卸载时移除滚动监听
onUnmounted(() => {
  const scrollContainer = document.querySelector('.course-scroll-container')
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})
</script>

<template>
  <div class="course-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">我的课程</h1>
        <div class="header-placeholder"></div>
      </div>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程..."
          prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="course-scroll-container">
      <!-- 课程列表 -->
      <div class="course-list">
        <div
          v-for="course in filteredCourses"
          :key="course.id"
          class="course-item"
          @click="viewCourseDetail(course.id)"
        >
          <!-- 课程图片 -->
          <div class="course-image">
            <el-image
              :src="course.coverImage"
              :alt="course.name"
              fit="cover"
              :fallback="'/default-course.png'"
            />
          </div>
          
          <!-- 课程信息 -->
          <div class="course-info">
            <!-- 课程标题 -->
            <h3 class="course-title">{{ course.title || course.name }}</h3>
            
            <!-- 课程元信息 -->
            <div class="course-meta">
              <span class="meta-item">{{ course.teacher }}</span>
              <span class="meta-item">{{ formatDuration(course.duration) }}</span>
            </div>
            
            <!-- 价格 -->
            <div class="course-price">
              <span class="price-paid">¥{{ course.price }}</span>
            </div>
          </div>
          
          <!-- 体验按钮 -->
          <div class="course-action">
            <el-button type="primary" round class="experience-button">去学习</el-button>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <el-icon size="48"><Warning /></el-icon>
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchBoughtCourses">重试</el-button>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!loading && !error && (!filteredCourses || filteredCourses.length === 0)" class="empty-state">
        <el-icon size="48"><Document /></el-icon>
        <p>暂无已购买的课程</p>
        <el-button type="primary" @click="router.push('/shop')">去商店看看</el-button>
      </div>
      
      <!-- 加载更多状态 -->
      <div v-if="loading && courses && courses.length > 0" class="load-more">
        <el-loading v-if="loading" type="spinner" color="#409EFF" />
        <p v-else-if="!loading && hasMore">下拉加载更多</p>
      </div>
      
      <!-- 没有更多数据 -->
      <div v-if="!hasMore && courses && courses.length > 0" class="no-more">
        <p>没有更多了</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.course-container {
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

/* 顶部固定区域 - 使用fixed定位确保完全固定 */
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
  margin: 0 auto 1rem;
  box-sizing: border-box;
  padding: 0.5rem 0;
}

.page-title {
  font-size: 1.1rem;
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

/* 搜索栏 */
.search-bar {
  margin-bottom: 0;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  box-sizing: border-box;
}

.search-input {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color);
  height: 40px;
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  transition: all 0.3s ease;
}

/* 确保在黑夜模式下搜索框显示正确的背景色 */
:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color) !important;
}

:deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

:deep(.el-input__prefix-inner) {
  color: var(--text-tertiary) !important;
}

/* 课程列表 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 0 1rem;
}

/* 课程项 */
.course-item {
  display: flex;
  flex-direction: row;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  width: 100%;
  box-sizing: border-box;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  padding: 1rem;
}

.course-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.course-item:active {
  transform: translateY(0);
  transition: all var(--transition-fast);
}

/* 课程图片 */
.course-image {
  width: 120px;
  height: 120px;
  overflow: hidden;
  background-color: var(--bg-tertiary);
  position: relative;
  border-radius: var(--border-radius-md);
  margin-right: 1rem;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.course-item:hover .course-image img {
  transform: scale(1.05);
}

/* 课程信息 */
.course-info {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 课程标题 */
.course-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.5rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 课程元信息 */
.course-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.meta-item {
  font-size: 0.9rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

/* 价格 */
.course-price {
  font-weight: bold;
  display: flex;
  align-items: center;
}

.price-paid {
  color: var(--danger-color);
  font-size: 1.2rem;
  font-weight: 700;
  transition: all 0.3s ease;
}

/* 体验按钮 */
.course-action {
  margin-left: auto;
  align-self: center;
}

.experience-button {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 内部滚动容器 - 核心：确保这是唯一可以滚动的元素 */
.course-scroll-container {
  /* 核心：设置明确的高度和位置，确保可以滚动 */
  position: fixed;
  top: 150px; /* 调整位置以适应更大的头部 */
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距，确保可以看到所有课程 */
  padding-bottom: 5rem;
  /* 确保触摸事件能正常触发 */
  touch-action: pan-y;
  pointer-events: auto;
  /* 添加左右内边距 */
  padding-left: 1rem;
  padding-right: 1rem;
  /* 添加顶部内边距 */
  padding-top: 1rem;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) var(--bg-primary);
}

/* 确保滚动条在所有浏览器中都能正常工作 */
.course-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) transparent;
}

/* 滚动条样式 - 确保内部滚动条可见 */
.course-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.course-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.course-scroll-container::-webkit-scrollbar-thumb {
  background-color: var(--border-color);
  border-radius: 3px;
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
  gap: 1.5rem;
  transition: all 0.3s ease;
}

/* 加载更多 */
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
  transition: all 0.3s ease;
}

/* 没有更多数据 */
.no-more {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
  color: var(--text-tertiary);
  font-size: 0.8rem;
  width: 100%;
  transition: all 0.3s ease;
}

.empty-state .el-icon {
  margin-bottom: 1rem;
  color: var(--primary-color);
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .course-item {
    padding: 1.2rem;
  }
  
  .course-image {
    width: 100px;
    height: 100px;
  }
  
  .course-title {
    font-size: 1.2rem;
  }
}

@media (max-width: 768px) {
  .course-container {
    padding: 0.8rem;
  }
  
  .course-item {
    padding: 1rem;
  }
  
  .course-image {
    width: 90px;
    height: 90px;
  }
  
  .course-title {
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .course-container {
    padding: 0.6rem;
  }
  
  .page-title {
    font-size: 1.1rem;
  }
  
  .course-item {
    padding: 0.8rem;
  }
  
  .course-image {
    width: 80px;
    height: 80px;
    margin-right: 0.8rem;
  }
  
  .course-title {
    font-size: 1rem;
  }
  
  .meta-item {
    font-size: 0.8rem;
  }
  
  .price-paid {
    font-size: 1rem;
  }
  
  .experience-button {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
}
</style>