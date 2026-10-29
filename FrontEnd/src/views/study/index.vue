<script setup> 
 import { ref, onMounted, computed, watch } from 'vue'
import { getCurrentUserId } from '@/utils/auth.js' 
import { Search, Reading, ArrowRight, Headset } from '@element-plus/icons-vue' 
import { useStudyData } from '@/composables/useStudyData' // 导入组合式函数
import { useRouter, useRoute } from 'vue-router' // 导入路由
import { ElMessage } from 'element-plus'
import { useTheme } from '@/composables/useTheme'

// 获取路由实例
const router = useRouter()
// 获取当前路由
const route = useRoute()

// 搜索查询 
const searchQuery = ref('') 
// 提示框显示状态
const isShowingMessage = ref(false)

// 使用组合式函数 - 获取所有学习数据
const { studyDays, totalStudyDays, userType, newWords, reviewedWords, studiedWords, studyPlan, reviewStudyPlan, refresh } = useStudyData() 


 // 学习类型配置 - 根据ID定义不同学习内容 
 const studyTypeConfig = { 
  1: { title: '高中英语', totalWords: 3500, subtitle: '高中核心词汇' }, 
  2: { title: '四级英语', totalWords: 739, subtitle: '四级高频词汇' }, 
  3: { title: '六级英语', totalWords: 1575, subtitle: '六级高频词汇' }, 
  4: { title: '商务英语', totalWords: 2000, subtitle: '商务核心词汇' }, 
  5: { title: '出国英语', totalWords: 5000, subtitle: '出国常用词汇' } 
 } 


 // 当前学习类型的配置 - 使用computed响应式更新
 const currentStudyConfig = computed(() => studyTypeConfig[userType.value] || studyTypeConfig[2]) 


 // 初始化主题
const { initTheme } = useTheme()

// 钩子函数 
onMounted(() => { 
  // 数据已经通过组合式函数自动加载，这里可以添加其他初始化逻辑
  // 检查是否需要刷新数据
  if (route.query.refresh === 'true') {
    refresh(true) // 强制刷新，忽略缓存
  }
  // 初始化主题
  initTheme()
}) 

// 监听路由变化，当刷新参数变化时刷新数据
watch(() => route.query.refresh, (newValue) => {
  if (newValue === 'true') {
    refresh(true) // 强制刷新，忽略缓存
  }
})

// 手动刷新数据的方法
const refreshData = () => {
  // 如果需要手动刷新，可以调用组合式函数提供的refresh方法
  // refresh()
}

// 复习单词
const reviewWords = async () => {
  // 如果提示框正在显示，直接返回
  if (isShowingMessage.value) {
    return
  }
  
  try {
    // 假设用户ID为1
    const userId = getCurrentUserId()
    // 计算需要复习的单词数量：计划量减去已完成量
    const reviewNum = Math.max(0, (reviewStudyPlan.value || 10) - reviewedWords.value)
    
    // 如果复习数量为0，提示用户当天已完成复习计划
    if (reviewNum === 0) {
      isShowingMessage.value = true
      ElMessage({
        message: '您已完成今日复习计划',
        type: 'success',
        duration: 3000,
        onClose: () => {
          isShowingMessage.value = false
        }
      })
      return
    }
    
    // 导入API
    const { getUserReviewWord } = await import('@/api/study')
    // 获取复习单词
    const response = await getUserReviewWord(userId, reviewNum)
    
    if (response.code && response.data && response.data.length > 0) {
      // 跳转到单词输入界面，并传递单词数据
      router.push({
        path: '/inputWord',
        query: {
          words: JSON.stringify(response.data),
          mode: 'review'
        }
      })
    } else {
      // 如果没有复习单词，提示用户
      isShowingMessage.value = true
      ElMessage({
        message: '暂无需要复习的单词',
        type: 'success',
        duration: 3000,
        onClose: () => {
          isShowingMessage.value = false
        }
      })
    }
  } catch (error) {
    console.error('获取复习单词失败:', error)
    isShowingMessage.value = true
    ElMessage({
      message: '获取复习单词失败，请稍后重试',
      type: 'error',
      duration: 3000,
      onClose: () => {
        isShowingMessage.value = false
      }
    })
  }
}

// 学习单词
const studyWords = async () => {
  // 如果提示框正在显示，直接返回
  if (isShowingMessage.value) {
    return
  }
  
  try {
    // 计算需要学习的单词数量：计划量减去已完成量
    const studyNum = Math.max(0, studyPlan.value - newWords.value)
    
    // 如果学习数量小于或等于0，提示用户当天已完成学习计划
    if (studyNum <= 0) {
      isShowingMessage.value = true
      ElMessage({
        message: '您已完成今日学习计划',
        type: 'success',
        duration: 3000,
        onClose: () => {
          isShowingMessage.value = false
        }
      })
      return
    }
    
    // 跳转到单词展示页面，让用户先看一遍单词
    router.push('/showWord')
  } catch (error) {
    console.error('学习单词失败:', error)
    isShowingMessage.value = true
    ElMessage({
      message: '学习单词失败，请稍后重试',
      type: 'error',
      duration: 3000,
      onClose: () => {
        isShowingMessage.value = false
      }
    })
  }
}
 </script>

<!--定义js，控制组件的行为-->
<template>
  <div class="study-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索学习内容..."
        prefix-icon="Search"
        clearable
        @click="router.push('/search')"
        class="search-input"
        readonly
      />
    </div>
    
    <!-- 学习内容卡片 -->
    <div class="content-card" style="animation-delay: 0.1s;">
      <div class="content-header">
        <div class="content-icon">
          <el-icon size="32">
            <Reading />
          </el-icon>
        </div>
        <div class="content-info">
          <div class="content-title-row">
            <div class="content-title">{{ currentStudyConfig.title }}</div>
            <router-link to="/switch">
              <el-button type="text" size="small" class="switch-button">
                切换 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </router-link>
          </div>
          <div class="content-subtitle">{{ currentStudyConfig.subtitle }}</div>
          <div class="content-progress"> {{ studiedWords }} / {{ currentStudyConfig.totalWords }}</div>
        </div>
      </div>
    </div>
    
    <!-- 今日学习统计 -->
    <div class="today-stats" style="animation-delay: 0.2s;">
      <div class="stats-title">今日计划</div>
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-label">已新学</div>
          <div class="stat-count">{{ newWords }} / {{ studyPlan }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已复习</div>
          <div class="stat-count">{{ reviewedWords }} / {{ reviewStudyPlan }}</div>
        </div>
      </div>
      
      <!-- 学习按钮 -->
      <div class="action-buttons">
        <el-button type="primary" round class="action-button" @click="studyWords">学习</el-button>
        <el-button type="primary" round class="action-button" @click="reviewWords">复习</el-button>
      </div>
    </div>
    
    <!-- 学习天数统计 -->
    <div class="days-stats" style="animation-delay: 0.3s;">
      <div class="days-container">
        <div class="days-item">
          <div class="days-label">累计学习</div>
          <div class="days-count">{{ totalStudyDays }}</div>
          <div class="days-unit">天</div>
        </div>
        <div class="days-divider"></div>
        <div class="days-item">
          <div class="days-label">连续学习</div>
          <div class="days-count">{{ studyDays }}</div>
          <div class="days-unit">天</div>
        </div>
      </div>
    </div>
    
    <!-- 听力训练模块 -->
    <div class="listening-module" style="animation-delay: 0.4s;">
      <div class="module-header">
        <div class="module-icon">
          <el-icon size="32">
            <Headset />
          </el-icon>
        </div>
        <div class="module-title">听力训练</div>
        <el-button type="primary" round class="module-button" @click="router.push('/listening')">开始训练</el-button>
      </div>
    </div>
    
    <!-- 空白区域 -->
    <div class="blank-area"></div>
  </div>
</template>

<style scoped>
.study-container {
  width: 100%;
  padding: 1rem;
  box-sizing: border-box;
  min-height: 100vh;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #e6f0ff 50%, #4a6fa5 100%);
  color: var(--text-primary);
  transition: all 0.3s ease;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  touch-action: pan-y;
  padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  animation: pageFadeIn 0.8s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  margin: 0;
}

/* 黑夜模式样式 */
html.dark .study-container {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 50%, #121212 100%);
  color: var(--text-primary);
}

/* 搜索栏样式 - 仿照成就页面 */
.search-bar {
  padding: 0 1rem 1rem;
  background-color: var(--bg-primary);
  z-index: 10;
  border-bottom: 1px solid var(--border-color);
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.search-input {
  width: 100%;
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color) !important;
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

/* 内容卡片样式 */
.content-card {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 2.5rem; /* 增大模块间距 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
  animation: studyModuleFadeIn 0.5s ease-out forwards;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.content-icon {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 152, 0, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.content-icon .el-icon {
  color: var(--primary-color);
}

.content-info {
  flex: 1;
}

.content-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.2rem;
}

.content-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
}

.switch-button {
  font-size: 0.8rem;
  color: var(--primary-color);
  padding: 0;
  margin: 0;
  height: auto;
}

.content-subtitle {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin-bottom: 0.2rem;
}

.content-progress {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 0.2rem;
}

/* 移除剩余天数样式 */

/* 学习天数统计 */
.days-stats {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 0.8rem 1rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
  animation: studyModuleFadeIn 0.5s ease-out forwards;
}

.days-container {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.days-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.days-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.3rem;
}

.days-count {
  font-size: 1.4rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 0.1rem;
}

.days-unit {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.days-divider {
  width: 1px;
  height: 35px;
  background-color: var(--border-color);
}

/* 今日学习统计 */
.today-stats {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 2.5rem; /* 增大模块间距 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
  animation: studyModuleFadeIn 0.5s ease-out forwards;
}

.stats-title {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.stats-container {
  display: flex;
  justify-content: space-around;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.stat-count {
  font-size: 2rem;
  font-weight: bold;
  color: var(--text-primary);
}

/* 学习按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.action-button {
  flex: 1;
  max-width: 200px;
  height: 50px;
  font-size: 1rem;
  font-weight: 500;
}

/* 听力训练模块 */
.listening-module {
  background-color: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.2rem;
  margin-bottom: 2.5rem; /* 保持与其他模块一致的间距 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
  animation: studyModuleFadeIn 0.5s ease-out forwards;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  justify-content: space-between;
}

.module-icon {
  width: 48px;
  height: 48px;
  background-color: rgba(103, 194, 58, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.module-icon .el-icon {
  color: var(--success-color);
}

.module-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
}

.module-button {
  padding: 0.5rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 空白区域 */
.blank-area {
  height: 100px;
  background-color: transparent;
}

/* 响应式设计 - 基础平板适配 */
@media (max-width: 1024px) {
  .study-container {
    padding: 1rem;
    padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  }
  
  .content-card,
  .days-stats,
  .today-stats,
  .listening-module {
    margin-bottom: 2rem;
  }
  
  .stat-count {
    font-size: 1.9rem;
  }
}

/* 响应式设计 - 小型平板/大屏手机 */
@media (max-width: 768px) {
  .study-container {
    padding: 0.8rem;
    padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  }
  
  .content-title {
    font-size: 0.95rem;
  }
  
  .stat-count {
    font-size: 1.8rem;
  }
  
  .days-count {
    font-size: 1.3rem;
  }
  
  .content-card,
  .days-stats,
  .today-stats,
  .listening-module {
    margin-bottom: 1.8rem;
    padding: 0.9rem;
  }
  
  .action-button {
    height: 48px;
    font-size: 0.95rem;
  }
}

/* 响应式设计 - 标准手机 */
@media (max-width: 480px) {
  .study-container {
    padding: 0.5rem;
    padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  }
  
  .search-bar {
    padding: 0 0.5rem 0.8rem;
  }
  
  .content-header {
    gap: 0.8rem;
  }
  
  .content-icon {
    width: 40px;
    height: 40px;
  }
  
  .content-icon .el-icon {
    font-size: 24px;
  }
  
  .content-title {
    font-size: 0.9rem;
  }
  
  .content-subtitle {
    font-size: 0.75rem;
  }
  
  .content-progress {
    font-size: 0.8rem;
  }
  
  .stat-count {
    font-size: 1.6rem;
  }
  
  .action-button {
    height: 45px;
    font-size: 0.9rem;
  }
  
  .content-card,
  .days-stats,
  .today-stats,
  .listening-module {
    margin-bottom: 1.5rem;
    padding: 0.8rem;
  }
  
  .stats-container {
    gap: 0.5rem;
  }
  
  .days-label,
  .days-unit {
    font-size: 0.75rem;
  }
}

/* 响应式设计 - 小屏手机 */
@media (max-width: 360px) {
  .study-container {
    padding: 0.4rem;
    padding-bottom: 3rem; /* 为底部导航栏留出空间 */
  }
  
  .content-card,
  .days-stats,
  .today-stats,
  .listening-module {
    margin-bottom: 1.2rem;
    padding: 0.7rem;
  }
  
  .content-title {
    font-size: 0.85rem;
  }
  
  .stat-count {
    font-size: 1.4rem;
  }
  
  .days-count {
    font-size: 1.2rem;
  }
  
  .action-button {
    height: 40px;
    font-size: 0.85rem;
  }
  
  .module-button {
    padding: 0.3rem 0.8rem;
    font-size: 0.8rem;
  }
}

/* 响应式设计 - iPhone SE等极小屏设备 (280px-320px) */
@media (max-width: 320px) {
  html, body {
    font-size: 14px; /* 整体缩小字体基准 */
  }
  
  .study-container {
    padding: 0.3rem;
    padding-bottom: 3rem; /* 为底部导航栏留出空间 */
    height: 100vh;
    min-height: 100vh;
  }
  
  .search-bar {
    padding: 0 0.3rem 0.6rem;
    margin-bottom: 0.8rem;
  }
  
  .content-card,
  .days-stats,
  .today-stats,
  .listening-module {
    margin-bottom: 0.8rem;
    padding: 0.5rem;
    border-radius: 8px; /* 减小圆角 */
  }
  
  /* 内容卡片优化 */
  .content-header {
    gap: 0.6rem;
  }
  
  .content-icon {
    width: 35px;
    height: 35px;
  }
  
  .content-icon .el-icon {
    font-size: 20px;
  }
  
  .content-title {
    font-size: 0.8rem;
  }
  
  .switch-button {
    font-size: 0.7rem;
  }
  
  .content-subtitle {
    font-size: 0.7rem;
  }
  
  .content-progress {
    font-size: 0.75rem;
  }
  
  /* 学习天数统计优化 */
  .days-container {
    gap: 0.5rem;
  }
  
  .days-label,
  .days-unit {
    font-size: 0.7rem;
  }
  
  .days-count {
    font-size: 1rem;
    margin-bottom: 0;
  }
  
  .days-divider {
    height: 25px;
  }
  
  /* 今日计划优化 */
  .stats-title {
    font-size: 0.8rem;
    margin-bottom: 0.6rem;
  }
  
  .stats-container {
    gap: 0.3rem;
    margin-bottom: 1rem;
  }
  
  .stat-label {
    font-size: 0.75rem;
    margin-bottom: 0.3rem;
  }
  
  .stat-count {
    font-size: 1.2rem;
  }
  
  /* 按钮优化 */
  .action-buttons {
    gap: 0.8rem;
  }
  
  .action-button {
    height: 38px;
    font-size: 0.8rem;
    padding: 0 0.6rem;
  }
  
  /* 听力训练优化 */
  .module-icon {
    width: 35px;
    height: 35px;
  }
  
  .module-icon .el-icon {
    font-size: 20px;
  }
  
  .module-title {
    font-size: 0.9rem;
  }
  
  .module-button {
    padding: 0.3rem 0.8rem;
    font-size: 0.75rem;
    min-width: unset;
  }
  
  /* 空白区域 */
  .blank-area {
    height: 80px; /* 固定高度，确保底部导航可见 */
  }
}

/*ai辅助生成 Doubao-Seed-1.8,
2026年1月5日 8：30-9：30*/

/* 动画效果 */
@keyframes studyModuleFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 页面加载动画 */
@keyframes pageFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 按钮交互动画 */
.action-button {
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 模块悬停动画 */
.content-card,
.today-stats,
.days-stats,
.listening-module {
  transition: all 0.3s ease;
}

.content-card:hover,
.today-stats:hover,
.days-stats:hover,
.listening-module:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 页面加载动画 */
@keyframes pageFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 按钮交互动画 */
.action-button {
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 模块悬停动画 */
.content-card,
.today-stats,
.days-stats,
.listening-module {
  transition: all 0.3s ease;
}

.content-card:hover,
.today-stats:hover,
.days-stats:hover,
.listening-module:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}
</style>