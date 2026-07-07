<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, Star, StarFilled, ShoppingCart } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { courseDetail, favoriteCourse, cancelFavoriteCourse, checkFavoriteCourse, buyCourse, checkBoughtCourse } from '@/api/course'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 课程数据
const course = ref(null)
// 加载状态
const loading = ref(true)
// 错误信息
const error = ref('')
// 收藏状态
const isFavorited = ref(false)
// 收藏加载状态
const favoriteLoading = ref(false)
// 购买加载状态
const buyLoading = ref(false)

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取课程详情
const fetchCourseDetail = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取路由参数中的课程ID
    const courseId = route.params.id || route.query.id
    if (!courseId) {
      throw new Error('缺少课程ID')
    }
    
    // 获取课程详情
    const response = await courseDetail(courseId)
    if (response.code && response.data) {
      course.value = response.data
      
      // 检查用户是否收藏了该课程
      try {
        const userId = getCurrentUserId()
        const favoriteResponse = await checkFavoriteCourse(userId, courseId)
        console.log('收藏状态检查结果:', favoriteResponse)
        if (favoriteResponse.code) {
          isFavorited.value = favoriteResponse.data || false
          console.log('设置收藏状态为:', isFavorited.value)
        } else {
          isFavorited.value = false
        }
      } catch (err) {
        console.error('检查收藏状态失败:', err)
        // 检查失败时默认设为未收藏
        isFavorited.value = false
      }
    } else {
      throw new Error('获取课程详情失败')
    }
  } catch (err) {
    console.error('获取课程详情失败:', err)
    error.value = err.message || '获取课程详情失败，请稍后重试'
    
    // 使用模拟数据作为备选
    course.value = {
      id: 1,
      title: '零基础学英语',
      teacher: '张老师',
      duration: '120分钟',
      price: 99,
      coverImage: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=english%20learning%20course%20cover&size=1024x1024',
      isFavorited: false,
      description: '本课程适合零基础学习者，从音标开始，逐步掌握英语基础语法和常用词汇，为后续学习打下坚实基础。'
    }
    isFavorited.value = false
  } finally {
    loading.value = false
  }
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

// 切换收藏状态
const toggleFavorite = async () => {
  if (!course.value) return
  
  try {
    favoriteLoading.value = true
    
    // 假设用户ID为1
    const userId = getCurrentUserId()
    const courseId = course.value.id
    
    if (isFavorited.value) {
      // 取消收藏
      await cancelFavoriteCourse(userId, courseId)
      isFavorited.value = false
    } else {
      // 收藏
      await favoriteCourse(userId, courseId)
      isFavorited.value = true
    }
  } catch (err) {
    console.error('操作收藏失败:', err)
    error.value = '操作收藏失败，请稍后重试'
  } finally {
    favoriteLoading.value = false
  }
}

// 购买课程
const handleBuyCourse = async () => {
  if (!course.value || buyLoading.value) return
  
  try {
    buyLoading.value = true
    
    // 假设用户ID为1
    const userId = getCurrentUserId()
    const courseId = course.value.id
    
    // 检查用户是否已购买此课程
    const checkResponse = await checkBoughtCourse(userId, courseId)
    if (checkResponse.code && checkResponse.data) {
      if (checkResponse.data) {
        // 已购买，显示提示
        ElMessage.warning('您已经购买过此课程，无需重复购买')
        return
      }
    }
    
    // 调用购买课程API
    await buyCourse(userId, courseId)
    
    // 购买成功提示
    ElMessage.success('购买成功！')
  } catch (err) {
    console.error('购买课程失败:', err)
    error.value = '购买课程失败，请稍后重试'
    ElMessage.error('购买失败，请稍后重试')
  } finally {
    buyLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  initTheme()
  fetchCourseDetail()
})
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="course-detail-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">课程详情</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 内部滚动容器 -->
    <div class="content-scroll-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchCourseDetail">重试</el-button>
      </div>
      
      <!-- 课程详情 -->
      <div v-else-if="course" class="course-content">
        <!-- 课程封面 -->
        <div class="course-cover">
          <img :src="course.coverImage" :alt="course.title" class="cover-image" />
        </div>
        
        <!-- 课程信息 -->
        <div class="course-info">
          <div class="course-header-info">
            <h2 class="course-name">{{ course.title }}</h2>
          </div>
          
          <div class="course-meta">
            <div class="meta-item">
              <span class="meta-label">授课老师：</span>
              <span class="meta-value">{{ course.teacher }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">课程时长：</span>
              <span class="meta-value">{{ formatDuration(course.duration) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">课程价格：</span>
              <span class="meta-value price">{{ course.price }}元</span>
            </div>
          </div>
          
          <!-- 课程描述 -->
          <div class="course-description">
            <h3 class="section-title">课程介绍</h3>
            <p class="description-content">{{ course.description || '暂无课程介绍' }}</p>
          </div>
          
          <!-- 操作按钮 -->
          <div class="course-actions">
            <el-button 
              type="default" 
              size="large" 
              :icon="isFavorited ? StarFilled : Star" 
              @click="toggleFavorite"
              class="action-button favorite-button"
              :class="{ 'is-favorite': isFavorited }"
              :loading="favoriteLoading"
              :disabled="favoriteLoading"
              icon-position="left"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button 
              type="primary" 
              size="large" 
              :icon="ShoppingCart" 
              @click="handleBuyCourse"
              class="action-button buy-button"
              :loading="buyLoading"
              :disabled="buyLoading"
              icon-position="left"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 主容器 */
.course-detail-container {
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

/* 内容滚动容器 */
.content-scroll-container {
  position: fixed;
  top: 70px;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 增加底部内边距 */
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

/* 课程内容 */
.course-content {
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 课程封面 */
.course-cover {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cover-image {
  width: 100%;
  height: 360px;
  object-fit: contain;
  background-color: var(--bg-primary);
  transition: all 0.3s ease;
}

/* 课程信息 */
.course-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 课程头部信息 */
.course-header-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.course-name {
  flex: 1;
  font-size: 1.4rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  transition: all 0.3s ease;
}

/* 操作按钮 */
.course-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

/* 操作按钮通用样式 */
.action-button {
  flex: 1;
  width: 100%;
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

/* 收藏按钮 */
.favorite-button {
  color: #606266; /* 默认文字颜色 */
  border-color: #dcdfe6; /* 默认边框颜色 */
}

/* 购买按钮 */
.buy-button {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  transition: all 0.3s ease;
}

.buy-button:hover {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  transition: all 0.3s ease;
}

.favorite-button:hover {
  background-color: #606266;
  border-color: #dcdfe6;
  color: #606266;
  transition: all 0.3s ease;
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

/* 强制重置Element Plus按钮的默认内边距差异 */
:deep(.el-button) {
  padding-left: 1rem !important;
  padding-right: 1rem !important;
}

/* 课程元信息 */
.course-meta {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding: 1.2rem;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meta-label {
  font-size: 1rem;
  color: var(--text-secondary);
  min-width: 80px;
  transition: all 0.3s ease;
}

.meta-value {
  font-size: 1rem;
  color: var(--text-primary);
  flex: 1;
  transition: all 0.3s ease;
}

.meta-value.price {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--danger-color);
  transition: all 0.3s ease;
}

/* 课程描述 */
.course-description {
  padding: 1.2rem;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.section-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  transition: all 0.3s ease;
}

.description-content {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .content-scroll-container {
    padding: 0.8rem;
  }
  
  .course-content {
    gap: 1.2rem;
  }
  
  .cover-image {
    height: 300px;
  }
  
  .course-name {
    font-size: 1.3rem;
  }
  
  .course-meta,
  .course-description {
    padding: 1rem;
  }
  
  .meta-label {
    min-width: 70px;
  }
  
  .meta-value {
    font-size: 0.95rem;
  }
  
  .meta-value.price {
    font-size: 1.1rem;
  }
  
  .section-title {
    font-size: 1.1rem;
  }
  
  .description-content {
    font-size: 0.95rem;
  }
  
  .course-actions {
    margin-top: 1.5rem;
  }
  
  .action-button {
    max-width: none;
    width: 100%;
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