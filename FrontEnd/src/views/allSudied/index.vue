<!--定义js，控制组件的行为-->
<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js'
import { ArrowLeft, ArrowRight, Reading, Document, Message, Briefcase, Position } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getUserStudiedWords } from '@/api/word'
import { useTheme } from '@/composables/useTheme'

const { initTheme } = useTheme()

onMounted(() => {
  initTheme()
})

// 获取路由实例
const router = useRouter()

// 返回上一页
const goBack = () => {
  console.log('返回按钮被点击')
  router.push('/user')
}

// 已学分类数据
const categories = ref([
  {
    id: 1,
    name: '高中英语',
    description: '高中阶段必备英语词汇',
    count: 3500,
    icon: 'Reading'
  },
  {
    id: 2,
    name: '四级英语',
    description: '大学英语四级词汇',
    count: 4500,
    icon: 'Document'
  },
  {
    id: 3,
    name: '六级英语',
    description: '大学英语六级词汇',
    count: 6000,
    icon: 'Message'
  },
  {
    id: 4,
    name: '商务英语',
    description: '商务场景常用英语词汇',
    count: 3000,
    icon: 'Briefcase'
  },
  {
    id: 5,
    name: '出国英语',
    description: '出国留学旅游必备词汇',
    count: 2500,
    icon: 'Position'
  }
])

// 当前选中的分类
const selectedCategory = ref(null)
// 单词数据
const words = ref([])
// 加载状态
const loading = ref(false)
// 错误信息
const error = ref('')

// 图标映射
const iconMap = {
  Reading,
  Document,
  Message,
  Briefcase,
  Position
}

// 查看分类详情
const viewCategory = (category) => {
  selectedCategory.value = category
  fetchCategoryWords(category)
}

// 返回分类列表
const goBackToCategories = () => {
  console.log('返回分类列表')
  selectedCategory.value = null
  words.value = []
}

// 跳转到单词详情页
const goToWordDetail = (wordId) => {
  router.push(`/showWord?id=${wordId}`)
}

// 获取分类单词数据
const fetchCategoryWords = async (category) => {
  try {
    loading.value = true
    error.value = ''
    
    // 使用API获取单词数据
    const userId = getCurrentUserId()
    const pageNum = 1 // 默认第一页
    const type = category.id // 使用分类ID作为类型参数
    
    const response = await getUserStudiedWords(userId, pageNum, type)
    if (response.data && response.data.length > 0) {
      words.value = response.data
    } else {
      words.value = []
    }
  } catch (err) {
    console.error('获取单词数据失败:', err)
    error.value = '获取单词数据失败，请稍后重试'
    // 使用模拟数据作为备选
    words.value = [
      { id: 1, definition: '放弃，抛弃', phonetic: '/əˈbændən/' },
      { id: 2, definition: '不正常的，反常的', phonetic: '/æbˈnɔːrml/' },
      { id: 3, definition: '丰富的，充裕的', phonetic: '/əˈbʌndənt/' },
      { id: 4, definition: '学术的，教学的', phonetic: '/ˌækəˈdemɪk/' },
      { id: 5, definition: '加速，促进', phonetic: '/əkˈseləreɪt/' }
    ]
  } finally {
    loading.value = false
  }
}
</script>

<!--定义html，控制组件的结构-->
<template>
  <div class="all-studied-container">
    <!-- 顶部固定区域 -->
    <div class="top-sticky-section">
      <!-- 头部导航 -->
      <div class="course-header">
        <el-button type="text" @click="() => { console.log('按钮点击事件触发'); selectedCategory ? goBackToCategories() : goBack() }" class="back-button">
          <el-icon size="24"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">{{ selectedCategory ? selectedCategory.name : '全部已学' }}</h1>
        <div class="header-placeholder"></div>
      </div>
    </div>
    
    <!-- 分类列表容器 -->
    <div v-if="!selectedCategory" class="categories-container">
      <!-- 分类列表 -->
      <div class="categories-list">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          @click="viewCategory(category)"
        >
          <!-- 分类图标 -->
          <div class="category-icon">
            <component :is="iconMap[category.icon] || Reading" size="24" />
          </div>
          
          <!-- 分类信息 -->
          <div class="category-info">
            <h3 class="category-name">{{ category.name }}</h3>
            <p class="category-description">{{ category.description }}</p>
          </div>
          
          <!-- 箭头图标 -->
          <div class="category-arrow">
            <el-icon size="20"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 单词列表容器 -->
    <div v-else class="words-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading type="spinner" color="#409EFF" />
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <el-button type="primary" @click="fetchWords" style="background-color: #1E88E5; border-color: #1E88E5;">重试</el-button>
      </div>
      
      <!-- 单词列表 -->
      <div v-else class="words-list">
        <div
          v-for="word in words"
          :key="word.id"
          class="word-item"
          @click="goToWordDetail(word.id)"
        >
          <!-- 单词图标 -->
          <div class="word-icon">
            <component :is="iconMap[selectedCategory.icon] || Reading" size="24" />
          </div>
          
          <!-- 单词信息 -->
          <div class="word-info">
            <div class="word-definition">{{ word.definition }}</div>
            <div class="word-phonetic">{{ word.phonetic }}</div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="words.length === 0" class="empty-state">
          <p>暂无已学单词</p>
        </div>
      </div>
    </div>
  </div>
</template>

<!--定义css，控制组件的样式-->
<style scoped>
/* 整体容器 */
.all-studied-container {
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
  display: flex;
  flex-direction: column;
  align-items: center;
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

/* 分类列表容器 */
.categories-container {
  flex: 1;
  width: 100%;
  max-width: 600px;
  padding: 0 1rem;
  box-sizing: border-box;
  margin-top: 80px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 单词列表容器 */
.words-container {
  flex: 1;
  width: 100%;
  max-width: 600px;
  padding: 1rem;
  box-sizing: border-box;
  margin-top: 80px;
  overflow-y: auto;
}

/* 单词列表 */
.words-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding-bottom: 1rem;
}

/* 单词项 */
.word-item {
  display: flex;
  align-items: flex-start;
  padding: 1rem;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.word-item:active {
  transform: scale(0.98);
}

/* 单词图标 */
.word-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: rgba(255, 152, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
  color: var(--primary-color);
  transition: all 0.3s ease;
}

/* 单词信息 */
.word-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.word-definition {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 0.2rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
  max-height: 1.4rem;
  transition: all 0.3s ease;
}

.word-phonetic {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  font-style: italic;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 0;
  transition: all 0.3s ease;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: #909399;
  width: 100%;
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
  color: #f56c6c;
  width: 100%;
}

/* 分类列表 */
.categories-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-bottom: 2rem;
}

/* 分类项 */
.category-item {
  display: flex;
  align-items: center;
  padding: 1.2rem;
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-item:active {
  transform: scale(0.98);
}

/* 分类图标 */
.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: rgba(255, 152, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
  color: var(--primary-color);
  transition: all 0.3s ease;
}

.category-icon :deep(.el-icon) {
  color: var(--primary-color);
  transition: all 0.3s ease;
}

/* 分类信息 */
.category-info {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.3rem 0;
  transition: all 0.3s ease;
}

.category-description {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  margin: 0 0 0.3rem 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.category-count {
  font-size: 0.8rem;
  color: var(--success-color);
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 箭头图标 */
.category-arrow {
  color: var(--text-tertiary);
  margin-left: 0.5rem;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .categories-container,
  .words-container {
    margin-top: 80px;
    padding: 0 0.8rem;
  }
  
  .category-item {
    padding: 1rem;
  }
  
  .category-icon {
    width: 40px;
    height: 40px;
    margin-right: 0.8rem;
  }
  
  .category-name {
    font-size: 1.1rem;
  }
  
  .category-description {
    font-size: 0.8rem;
  }
  
  .category-count {
    font-size: 0.7rem;
  }
  
  /* 单词项响应式 */
  .word-item {
    padding: 1rem;
  }
  
  .word-icon {
    width: 40px;
    height: 40px;
    margin-right: 0.8rem;
  }
  
  .word-definition {
    font-size: 1rem;
  }
  
  .word-phonetic {
    font-size: 0.8rem;
  }
  
  /* 调整头部导航 */
  .top-sticky-section {
    padding: env(safe-area-inset-top) 0.8rem 0.8rem;
  }
  
  .course-header {
    padding: 0.5rem 0;
  }
  
  .page-title {
    font-size: 1rem;
  }
  
  .back-button {
    width: 40px;
    height: 40px;
  }
  
  .header-placeholder {
    width: 40px;
  }
}
</style>